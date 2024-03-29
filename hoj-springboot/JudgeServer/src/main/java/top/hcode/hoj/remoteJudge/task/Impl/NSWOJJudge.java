package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class NSWOJJudge extends RemoteJudgeStrategy {
    public static final String HOST = "https://acm.nyist.edu.cn";
    public static final String LOGIN_URL = "/login";
    public static final String SUBMIT_URL = "/p/%s/submit";
    public static final String SUBMISSION_RESULT_URL = "/record/%s";

    private static Map<String, String> languageMap = new HashMap<>();

    static {
        languageMap.put("C++", "cc");
        languageMap.put("C++ 98", "cc.std98");
        languageMap.put("C++ 11", "cc.std11");
        languageMap.put("C++ 14", "cc.std14");
        languageMap.put("C++ 17", "cc.std17");
        languageMap.put("C", "c");
        languageMap.put("Pascal", "pas");
        languageMap.put("Java", "java");
        languageMap.put("PHP", "php");
        languageMap.put("Golang", "go");
        languageMap.put("Ruby", "rb");
        languageMap.put("C#", "cs");
        languageMap.put("Javascript", "js");
        languageMap.put("Python", "py");
        languageMap.put("Python 2", "py.py2");
        languageMap.put("Python 3", "py.py3");
        languageMap.put("Rust", "rs");

    }

    public enum JudgeStatus {
        STATUS_WAITING(0, Constants.Judge.STATUS_PENDING),
        STATUS_ACCEPTED(1, Constants.Judge.STATUS_ACCEPTED),
        STATUS_WRONG_ANSWER(2, Constants.Judge.STATUS_WRONG_ANSWER),
        STATUS_TIME_LIMIT_EXCEEDED(3, Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED),
        STATUS_MEMORY_LIMIT_EXCEEDED(4, Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED),
        STATUS_OUTPUT_LIMIT_EXCEEDED(5, Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED),
        STATUS_RUNTIME_ERROR(6, Constants.Judge.STATUS_RUNTIME_ERROR),
        STATUS_COMPILE_ERROR(7, Constants.Judge.STATUS_COMPILE_ERROR),
        STATUS_SYSTEM_ERROR(8, Constants.Judge.STATUS_SYSTEM_ERROR),
        STATUS_CANCELED(9, Constants.Judge.STATUS_CANCELLED),
        STATUS_ETC(10, Constants.Judge.STATUS_CANCELLED),
        STATUS_HACKED(11, Constants.Judge.STATUS_CANCELLED),
        STATUS_JUDGING(20, Constants.Judge.STATUS_JUDGING),
        STATUS_COMPILING(21, Constants.Judge.STATUS_COMPILING),
        STATUS_FETCHED(22, Constants.Judge.STATUS_PENDING),
        STATUS_IGNORED(30, Constants.Judge.STATUS_NULL),
        STATUS_FORMAT_ERROR(31, Constants.Judge.STATUS_PRESENTATION_ERROR),
        STATUS_HACK_SUCCESSFUL(32, Constants.Judge.STATUS_CANCELLED),
        STATUS_HACK_UNSUCCESSFUL(33, Constants.Judge.STATUS_CANCELLED);

        private final int key;
        private final Constants.Judge value;

        JudgeStatus(int key, Constants.Judge value) {
            this.key = key;
            this.value = value;
        }

        public int getKey() {
            return key;
        }

        public Constants.Judge getValue() {
            return value;
        }

        public static Constants.Judge getValueByKey(int key) {
            for (JudgeStatus status : JudgeStatus.values()) {
                if (status.getKey() == key) {
                    return status.getValue();
                }
            }
            return null;
        }

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

        String submit_url = String.format(HOST + SUBMIT_URL, remoteJudgeDTO.getProblemNum());

        String submissionId = null;

        try {
            Connection.Response response = Jsoup.connect(submit_url)
                    .header("Cookie", cookie_)
                    .header("Connection", "keep-alive")
                    .data("lang", getLanguage(remoteJudgeDTO.getLanguage()))
                    .data("code", remoteJudgeDTO.getUserCode())
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .execute();

            String html = response.body();

            String regex = (HOST + SUBMISSION_RESULT_URL).replace("%s", "") + "(.*?)\"";

            Pattern pattern = Pattern.compile(regex);
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
                        .data("lang", getLanguage(remoteJudgeDTO.getLanguage()))
                        .data("code", remoteJudgeDTO.getUserCode())
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .execute();

                String html2 = response2.body();

                Pattern pattern2 = Pattern.compile(regex);
                Matcher matcher2 = pattern2.matcher(html2);

                if (matcher2.find()) {
                    submissionId = matcher2.group(1);
                }

                if (StringUtils.isNotBlank(submissionId)) {
                    remoteJudgeDTO.setSubmitId(submissionId);
                } else {
                    throw new RuntimeException("[NSWOJ] Failed to submit!");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "[NSWOJ] Failed to submit! The msg is " + e);
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
        JSONObject jsonObject = new JSONObject(html);

        Integer result = null;
        Integer time = null;
        Integer memory = null;

        Integer testcaseNum = remoteJudgeDTO.getTestcaseNum();
        Integer maxTime = remoteJudgeDTO.getMaxTime();
        Integer maxMemory = remoteJudgeDTO.getMaxMemory();
        if (testcaseNum == null) {
            testcaseNum = 1;
            maxTime = 0;
            maxMemory = 0;
        }
        List<JudgeCase> judgeCaseList = new ArrayList<>();
        if (jsonObject.containsKey("rdoc")) {
            JSONObject rdoc = jsonObject.getJSONObject("rdoc");
            memory = rdoc.getInt("memory");
            time = (int) Math.round(rdoc.getDouble("time"));
            result = rdoc.getInt("status");

            // 提取测试数据列表
            if (rdoc.containsKey("testCases")) {
                JSONArray testCasesArray = rdoc.getJSONArray("testCases");

                for (int i = 0; i < testCasesArray.size(); i++) {
                    JSONObject testCase = testCasesArray.getJSONObject(i);
                    Long number = Long.valueOf(i);

                    Integer timeNumber = (int) Math.round(testCase.getDouble("time"));
                    Integer memoryNumber = testCase.getInt("memory");
                    judgeCaseList.add(new JudgeCase()
                            .setSubmitId(remoteJudgeDTO.getJudgeId())
                            .setPid(remoteJudgeDTO.getPid())
                            .setUid(remoteJudgeDTO.getUid())
                            .setCaseId(number)
                            .setScore(testCase.getInt("score"))
                            .setStatus(JudgeStatus.getValueByKey(testCase.getInt("status")).getStatus())
                            .setTime(timeNumber)
                            .setMemory(memoryNumber)
                            .setInputData(String.valueOf(number) + ".in")
                            .setOutputData(String.valueOf(number) + ".out")
                            .setUserOutput(testCase.getStr("message")));

                    if (timeNumber > maxTime) {
                        maxTime = timeNumber;
                    }
                    if (memoryNumber > maxMemory) {
                        maxMemory = memoryNumber;
                    }
                    testcaseNum += 1;
                }
            }
        } else {
            return RemoteJudgeRes.builder()
                    .status(Constants.Judge.STATUS_NULL.getStatus())
                    .build();
        }

        Constants.Judge judgeResult = JudgeStatus.getValueByKey(result);

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(judgeResult.getStatus())
                .memory(memory)
                .time(time)
                .build();

        if (judgeResult == Constants.Judge.STATUS_COMPILE_ERROR) {
            JSONObject rdoc = jsonObject.getJSONObject("rdoc");

            JSONArray compilerTextsArray = rdoc.getJSONArray("compilerTexts");
            String ceMsg = IntStream.range(0, compilerTextsArray.size())
                    .mapToObj(compilerTextsArray::getStr)
                    .collect(Collectors.joining("\n"));

            return remoteJudgeRes.setErrorInfo(ceMsg);
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

        HttpRequest request = HttpRequest.post(HOST + LOGIN_URL)
                .header("authority", "acm.nyist.edu.cn")
                .header("user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0")
                .form("uname", username)
                .form("password", password)
                .form("tfa", "")
                .form("authnChallenge", "");

        HttpResponse response = request.execute();

        String body = response.body();

        List<HttpCookie> cookies = response.getCookies();

        JSONObject jsonObject = new JSONObject(body);
        if (jsonObject.containsKey("error")) {
            JSONObject errorObject = jsonObject.getJSONObject("error");
            String errorMessage = errorObject.getStr("message");
            throw new RuntimeException(
                    "[NSWOJ] Failed to login! The possible cause is connection failure, and the returned status code is "
                            + errorMessage);
        } else if (!jsonObject.containsKey("UiContext")) {
            throw new RuntimeException(
                    "[NSWOJ] Failed to login! The possible cause is connection failure, and the returned status is "
                            + response.getStatus());
        }

        remoteJudgeDTO.setCookies(cookies);
    }

    @Override
    public String getLanguage(String language) {
        return languageMap.get(language).trim();
    }

}
