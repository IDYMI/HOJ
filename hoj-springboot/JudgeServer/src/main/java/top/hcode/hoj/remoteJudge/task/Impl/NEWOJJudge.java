package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

public class NEWOJJudge extends RemoteJudgeStrategy {
    public static final String HOST = "http://oj.ecustacm.cn";
    public static final String LOGIN_URL = "/login.php";
    public static final String SUBMIT_URL = "/submit.php";
    public static final String SUBMISSION_RESULT_URL = "/status.php?top=%s";

    private static Map<String, String> languageMap = new HashMap<>();
    private static final Map<String, Constants.Judge> statusTypeMap = new HashMap<>();

    static {
        languageMap.put("C++", "1");
        languageMap.put("C", "0");
        languageMap.put("Java", "3");
        languageMap.put("Python", "6");

        // result返回结果初始化
        statusTypeMap.put("等待", Constants.Judge.STATUS_PENDING);
        statusTypeMap.put("等待重判", Constants.Judge.STATUS_PENDING);
        statusTypeMap.put("编译中", Constants.Judge.STATUS_COMPILING);
        statusTypeMap.put("运行并评判", Constants.Judge.STATUS_COMPILING);
        statusTypeMap.put("正确", Constants.Judge.STATUS_ACCEPTED);
        statusTypeMap.put("答案错误", Constants.Judge.STATUS_WRONG_ANSWER);
        statusTypeMap.put("格式错误", Constants.Judge.STATUS_PRESENTATION_ERROR);
        statusTypeMap.put("编译错误", Constants.Judge.STATUS_COMPILE_ERROR);
        statusTypeMap.put("运行错误", Constants.Judge.STATUS_RUNTIME_ERROR);
        statusTypeMap.put("时间超限", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        statusTypeMap.put("内存超限", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
        statusTypeMap.put("输出超限", Constants.Judge.STATUS_WRONG_ANSWER);
        statusTypeMap.put("编译成功", Constants.Judge.STATUS_NOT_SUBMITTED);
        statusTypeMap.put("运行完成", Constants.Judge.STATUS_NOT_SUBMITTED);
        statusTypeMap.put("自动 评测通过，等待人工确认", Constants.Judge.STATUS_NOT_SUBMITTED);

    }

    @Override
    public void submit() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        if (remoteJudgeDTO.getProblemNum() == null || remoteJudgeDTO.getUserCode() == null) {
            return;
        }
        try {
            login();
        } catch (cn.hutool.core.io.IORuntimeException e) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            // 超时重试
            login();
        }
        String cookie_ = remoteJudgeDTO.getCookies().stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining("; ", "", ";"));

        String submit_url = String.format(HOST + SUBMIT_URL);

        String submissionId = null;

        try {
            Connection.Response response = Jsoup.connect(submit_url)
                    .header("Cookie", cookie_)
                    .header("Connection", "keep-alive")
                    .data("id", remoteJudgeDTO.getProblemNum())
                    .data("language", getLanguage(remoteJudgeDTO.getLanguage()))
                    .data("source", remoteJudgeDTO.getUserCode())
                    .data("vcode", "")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();

            String html = response.body();

            Pattern pattern = Pattern.compile("prevtop=(\\d+)");
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                submissionId = matcher.group(1);
            }

            // 提交成功
            if (StringUtils.isNotBlank(submissionId)) {
                remoteJudgeDTO.setSubmitId(submissionId);
            } else {
                // 再试一次
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ignore) {
                }

                Connection.Response response2 = Jsoup.connect(submit_url)
                        .header("Cookie", cookie_)
                        .header("Connection", "keep-alive")
                        .data("id", remoteJudgeDTO.getProblemNum())
                        .data("language", getLanguage(remoteJudgeDTO.getLanguage()))
                        .data("source", remoteJudgeDTO.getUserCode())
                        .data("vcode", "")
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .execute();

                String html2 = response2.body();

                Pattern pattern2 = Pattern.compile("prevtop=(\\d+)");
                Matcher matcher2 = pattern2.matcher(html2);
                if (matcher2.find()) {
                    submissionId = matcher2.group(1);
                }

                if (StringUtils.isNotBlank(submissionId)) {
                    remoteJudgeDTO.setSubmitId(submissionId);
                } else {
                    throw new RuntimeException("[NEWOJ] Failed to submit!");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "[NEWOJ] Failed to submit! The msg is " + e);
        }
    }

    @Override
    public RemoteJudgeRes result() {
        login();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        List<HttpCookie> cookies_list = remoteJudgeDTO.getCookies();
        String cookie_ = cookies_list.stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining("; ", "", ";"));

        String submisson_url = String.format(HOST + SUBMISSION_RESULT_URL, remoteJudgeDTO.getSubmitId());

        HttpResponse response = HttpRequest.get(submisson_url)
                .cookie(cookie_)
                .execute();

        String html = response.body();

        String result = null;
        Integer time = null;
        Integer memory = null;

        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("tr");

        for (Element tr : trs) {
            Element firstTd = tr.selectFirst("td");
            if (firstTd != null && firstTd.text().equals(remoteJudgeDTO.getSubmitId())) {
                Elements tds = tr.select("td");

                result = tds.get(4).text();
                memory = extractInteger(tds.get(5).text());
                time = extractInteger(tds.get(6).text());

                break;
            }
        }

        final String result_ = result;

        // 获取状态码
        Map<String, Constants.Judge> resultValues = statusTypeMap.entrySet().stream()
                .filter(entry -> result_.startsWith(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (resultValues.isEmpty()) {
            return RemoteJudgeRes.builder()
                    .status(Constants.Judge.STATUS_NULL.getStatus())
                    .build();
        } else {
            List<Constants.Judge> resultList = new ArrayList<>(resultValues.values());

            return RemoteJudgeRes.builder()
                    .status(resultList.get(0).getStatus())
                    .memory(memory)
                    .time(time)
                    .build();

        }

    }

    private static Integer extractInteger(String input) {
        if (StringUtils.isEmpty(input)) {
            return null;
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(input);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }

    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        String username = remoteJudgeDTO.getUsername();
        String password = remoteJudgeDTO.getPassword();

        HttpRequest request = HttpRequest.post(HOST + LOGIN_URL)
                .header("user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0")
                .form("user_id", username)
                .form("password", SecureUtil.md5(password))
                .form("submit", "");

        HttpResponse response = request.execute();

        String body = response.body();

        List<HttpCookie> cookies = response.getCookies();

        if (body.contains("alert")) {
            Pattern pattern = Pattern.compile("alert\\('([^']+)'\\);");
            Matcher matcher = pattern.matcher(body);

            if (matcher.find()) {
                String errorMessage = matcher.group(1);
                throw new RuntimeException(
                        "[NEWOJ] Failed to login! The possible cause is connection failure, and the returned status code is "
                                + errorMessage);
            }
        } else if (!body.contains("setTimeout")) {
            throw new RuntimeException(
                    "[NEWOJ] Failed to login! The possible cause is connection failure, and the returned status is "
                            + response.getStatus());
        }

        remoteJudgeDTO.setCookies(cookies);
    }

    @Override
    public String getLanguage(String language) {
        return languageMap.get(language).trim();
    }

}
