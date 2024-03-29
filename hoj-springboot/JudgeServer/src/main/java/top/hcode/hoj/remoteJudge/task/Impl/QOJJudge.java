package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class QOJJudge extends RemoteJudgeStrategy {
    public static final String HOST = "https://qoj.ac";
    public static final String LOGIN_URL = "/login";
    public static final String SUBMIT_URL = "/problem/%s";
    public static final String CONTEST_SUBMIT_URL = "/contest/%s/problem/%s";
    public static final String SUBMISSION_RESULT_URL = "/submission/%s";

    private static final Map<String, Constants.Judge> statusTypeMap = new HashMap<>();

    private static Map<String, String> languageMap = new HashMap<>();

    static {
        languageMap.put("C++ 98", "C++98");
        languageMap.put("C++ 11", "C++11");
        languageMap.put("C++ 14", "C++14");
        languageMap.put("C++ 17", "C++17");
        languageMap.put("C++ 20", "C++20");
        languageMap.put("C++ 23", "C++23");
        languageMap.put("C 89", "C89");
        languageMap.put("C 99", "C99");
        languageMap.put("C 11", "C11");
        languageMap.put("D", "D");
        languageMap.put("Java 8", "Java8");
        languageMap.put("Java 11", "Java11");
        languageMap.put("Python 3", "Python3");
        languageMap.put("Pascal", "Pascal");
        languageMap.put("Rust", "Rust");

        // result返回结果初始化
        statusTypeMap.put("Waiting", Constants.Judge.STATUS_PENDING);
        statusTypeMap.put("Judging", Constants.Judge.STATUS_JUDGING);
        statusTypeMap.put("AC", Constants.Judge.STATUS_ACCEPTED);
        statusTypeMap.put("WA", Constants.Judge.STATUS_WRONG_ANSWER);
        statusTypeMap.put("Compile", Constants.Judge.STATUS_COMPILE_ERROR);
        statusTypeMap.put("RE", Constants.Judge.STATUS_RUNTIME_ERROR);
        statusTypeMap.put("TL", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        statusTypeMap.put("ML", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
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

        String submit_url = null;
        if (!remoteJudgeDTO.getContestId().equals("0")) {
            submit_url = String.format(HOST + CONTEST_SUBMIT_URL, remoteJudgeDTO.getContestId(),
                    remoteJudgeDTO.getProblemNum());
        } else {
            submit_url = String.format(HOST + SUBMIT_URL, remoteJudgeDTO.getProblemNum());
        }

        String submissionId = null;

        try {
            Connection.Response response = Jsoup.connect(submit_url)
                    .header("Cookie", cookie_)
                    .header("Connection", "keep-alive")
                    .data("_token", remoteJudgeDTO.getCsrfToken())
                    .data("answer_answer_language", getLanguage(remoteJudgeDTO.getLanguage()))
                    .data("answer_answer_upload_type", "editor")
                    .data("answer_answer_editor", remoteJudgeDTO.getUserCode())
                    .data("answer_answer_file", "")
                    .data("submit-answer", "answer")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();

            String html = response.body();

            Pattern pattern = Pattern.compile(".*?update_judgement_status_details\\((\\d+)\\).*?");
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
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ignore) {
                }
                Connection.Response response2 = Jsoup.connect(submit_url)
                        .header("Cookie", cookie_)
                        .header("Connection", "keep-alive")
                        .data("_token", remoteJudgeDTO.getCsrfToken())
                        .data("answer_answer_language", getLanguage(remoteJudgeDTO.getLanguage()))
                        .data("answer_answer_upload_type", "editor")
                        .data("answer_answer_editor", remoteJudgeDTO.getUserCode())
                        .data("answer_answer_file", "")
                        .data("submit-answer", "answer")
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .execute();

                String html2 = response2.body();

                Pattern pattern2 = Pattern.compile(".*?update_judgement_status_details\\((\\d+)\\).*?");
                Matcher matcher2 = pattern2.matcher(html2);

                if (matcher2.find()) {
                    submissionId = matcher2.group(1);
                }
                if (StringUtils.isNotBlank(submissionId)) {
                    remoteJudgeDTO.setSubmitId(submissionId);
                } else {
                    throw new RuntimeException("[QOJ] Failed to submit!");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "[QOJ] Failed to submit! The msg is " + e);
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
        Document document = Jsoup.parse(html);

        if (document == null) {
            return RemoteJudgeRes.builder()
                    .status(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                    .build();
        }

        String result = null;
        Integer time = null;
        Integer memory = null;

        Element table = document.select("table.table.table-bordered.table-text-center").first();

        if (table != null) {
            Element tbody = table.select("tbody").first();
            if (tbody != null) {
                Element row = tbody.select("tr").first();
                if (row != null) {
                    Elements tds = row.select("td");
                    if (tds.size() >= 5) {
                        String text = tds.get(3).text().trim();
                        int spaceIndex = text.indexOf(" ");
                        result = spaceIndex != -1 ? text.substring(0, spaceIndex) : text;
                        time = extractValueFromTd(row, 4);
                        memory = extractValueFromTd(row, 5);
                    }
                }
            }
        } else {
            return RemoteJudgeRes.builder()
                    .status(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                    .build();
        }

        // 如果是整数，oi题目; 如果是字符串，acm题目
        Constants.Judge judgeResult = null;
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            int score = Integer.parseInt(matcher.group());
            if (score < 100 && score > 0) {
                judgeResult = Constants.Judge.STATUS_PARTIAL_ACCEPTED;
            } else if (score == 0) {
                judgeResult = Constants.Judge.STATUS_WRONG_ANSWER;
            } else {
                judgeResult = Constants.Judge.STATUS_ACCEPTED;
            }
        } else {
            judgeResult = statusTypeMap.get(result);
        }

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(judgeResult.getStatus())
                .memory(memory)
                .time(time)
                .build();

        if (judgeResult == Constants.Judge.STATUS_COMPILE_ERROR) {
            Pattern pattern2 = Pattern.compile("<pre>(.*?)</pre>", Pattern.DOTALL | Pattern.MULTILINE);
            Matcher matcher2 = pattern2.matcher(html);
            String lastDetail = "";

            while (matcher2.find()) {
                lastDetail = matcher2.group(1);
            }

            return remoteJudgeRes.setErrorInfo(lastDetail);
        }

        Elements cardElements = document.select(".card[class*=card-uoj-]");

        if (cardElements.size() == 0) {
            return remoteJudgeRes;
        }

        Integer testcaseNum = remoteJudgeDTO.getTestcaseNum();
        Integer maxTime = remoteJudgeDTO.getMaxTime();
        Integer maxMemory = remoteJudgeDTO.getMaxMemory();
        if (testcaseNum == null) {
            testcaseNum = 1;
            maxTime = 0;
            maxMemory = 0;
        }
        List<JudgeCase> judgeCaseList = new ArrayList<>();
        for (Element card : cardElements) {
            try {
                String title = card.select("h4").first().text();
                String score = card.select(".col-sm-2").get(1).text();
                String status = card.select(".col-sm-2").get(2).text();

                String input = card.selectFirst("h4:contains(input)").nextElementSibling().text();
                String output = card.selectFirst("h4:contains(output)").nextElementSibling().text();
                String testCaseResult = card.selectFirst("h4:contains(result)").nextElementSibling().text();

                Long titleNumber = extractNumber(title, "#(\\d+)", Long::parseLong);
                Integer scoreNumber = extractNumber(score, "(\\d+)", Integer::parseInt);
                Integer memoryNumber = extractNumber(card.select(".col-sm-3").get(1).text().trim(), "(\\d+)",
                        Integer::parseInt);
                Integer timeNumber = extractNumber(card.select(".col-sm-3").get(0).text().trim(), "(\\d+)",
                        Integer::parseInt);

                judgeCaseList.add(new JudgeCase()
                        .setSubmitId(remoteJudgeDTO.getJudgeId())
                        .setPid(remoteJudgeDTO.getPid())
                        .setUid(remoteJudgeDTO.getUid())
                        .setCaseId(titleNumber)
                        .setScore(scoreNumber)
                        .setStatus(getStatus(status))
                        .setTime(timeNumber)
                        .setMemory(memoryNumber)
                        .setInputData(String.valueOf(titleNumber) + ".in")
                        .setInputContent(input)
                        .setOutputData(String.valueOf(titleNumber) + ".out")
                        .setOutputContent(output)
                        .setUserOutput(testCaseResult));

                if (timeNumber > maxTime) {
                    maxTime = timeNumber;
                }
                if (memoryNumber > maxMemory) {
                    maxMemory = memoryNumber;
                }
                testcaseNum += 1;
            } catch (Exception e) {
            }
        }

        remoteJudgeDTO.setTestcaseNum(testcaseNum);
        remoteJudgeDTO.setMaxMemory(maxMemory);
        remoteJudgeDTO.setMaxTime(maxTime);
        remoteJudgeRes.setJudgeCaseList(judgeCaseList);

        return remoteJudgeRes;
    }

    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        String username = remoteJudgeDTO.getUsername();
        String password = remoteJudgeDTO.getPassword();
        String _token = null;

        HttpResponse response = HttpRequest.get(HOST + LOGIN_URL)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
                .timeout(5000)
                .execute();

        String html = response.body();
        Pattern pattern = Pattern.compile("\\b_token\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            _token = matcher.group(1);
        }

        String token = String.format(
                "_token=%s&login=&username=%s&password=%s", _token, username, SecureUtil.md5(password));

        String cookie_ = response.getCookies().stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining("; ", "", ";"));

        // 发送POST请求
        HttpResponse response2 = HttpRequest.post(HOST + LOGIN_URL)
                .header("authority", "qoj.ac")
                .header("Cookie", cookie_)
                .header("x-requested-with", "XMLHttpRequest")
                .body(token)
                .execute();

        // 解析响应内容
        String status = response2.body();

        List<HttpCookie> cookies = response2.getCookies();

        if (!status.equals("ok") || response2.getStatus() != 200) {
            throw new RuntimeException(
                    "[QOJ] Failed to login! The possible cause is connection failure, and the returned status code is "
                            + response2.getStatus() + ", and status is " + status);
        }

        remoteJudgeDTO.setCsrfToken(_token);
        remoteJudgeDTO.setCookies(cookies);
    }

    @Override
    public String getLanguage(String language) {
        return languageMap.get(language).trim();
    }

    public static <T extends Number> T extractNumber(String input, String pattern, Function<String, T> mapper) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        if (m.find()) {
            return mapper.apply(m.group(1));
        }
        return null;
    }

    public Integer getStatus(String status) {
        switch (status) {
            case "Accepted":
                return Constants.Judge.STATUS_ACCEPTED.getStatus();
            case "Wrong Answer":
                return Constants.Judge.STATUS_WRONG_ANSWER.getStatus();
            case "Runtime Error":
                return Constants.Judge.STATUS_RUNTIME_ERROR.getStatus();
            case "Memory Limit Exceeded":
                return Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus();
            case "Time Limit Exceeded":
                return Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus();
            case "Output Limit Exceeded":
                return Constants.Judge.STATUS_PRESENTATION_ERROR.getStatus();
            case "Dangerous Syscalls":
                return Constants.Judge.STATUS_SYSTEM_ERROR.getStatus();
            case "Judgement Failed":
                return Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus();
            default:
                return Constants.Judge.STATUS_NULL.getStatus();
        }
    }

    private static int extractValueFromTd(Element row, int index) {
        Element td = row.select("td").get(index);
        String text = td.text().trim().replaceAll("[^\\d]", "");
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }
}
