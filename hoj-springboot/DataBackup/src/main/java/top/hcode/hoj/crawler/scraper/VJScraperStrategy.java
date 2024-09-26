package top.hcode.hoj.crawler.scraper;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Date;

import org.jsoup.Connection.Response;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import top.hcode.hoj.pojo.vo.ACMContestRankVO;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

@Slf4j(topic = "hoj")
@Component
public class VJScraperStrategy extends ScraperStrategy {

    @Autowired
    private RedisUtils redisUtils;

    private static final String LOGIN_URL = "https://vjudge.net/user/login";
    private static final String CONTEST_RANK_URL = "https://vjudge.net/contest/rank/single/%s";
    private static final String RANK_URL = "https://vjudge.net/contest/%s#rank";
    private static final String CAPTCHA_URL = "https://vjudge.net/util/captcha?%s";

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Accept", "*/*")
            .put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .put("Origin", "https://vjudge.net")
            .put("Referer", "https://vjudge.net/")
            .put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36")
            .put("X-Requested-With", "XMLHttpRequest")
            .map();

    @Override
    public List<ACMContestRankVO> getScraperInfoByLogin(String cid, String loginUsername, String loginPassword,
            String captcha, String keyword, Map<String, String> usernameToUidMap) throws Exception {

        List<ACMContestRankVO> rankDatas = new ArrayList<>();
        String cookies_key = Constants.Account.VJ_LOGIN_COOKIES.getCode() + loginUsername;
        String time_key = Constants.Account.VJ_LOGIN_TIME.getCode() + loginUsername;
        String captcha_key = Constants.Account.VJ_CAPTCHA_UID.getCode() + loginUsername;

        String cookies = (String) redisUtils.get(cookies_key);
        String time = (String) redisUtils.get(time_key);

        Map<String, String> cookies_ = (cookies != null) ? stringToMap(cookies) : null;

        Connection.Response loginResponse = login(loginUsername, loginPassword, captcha, cookies_);

        if (loginResponse.body().contains("success")) {
            Map<String, String> get_cookies = (cookies_ != null) ? cookies_ : loginResponse.cookies();

            // 获取响应信息
            String html = getRankInfo(cid, get_cookies);

            // 获取并处理排名数据
            rankDatas = dealRank(html, usernameToUidMap);

            // 删除保存的redis
            redisUtils.del(cookies_key);
            redisUtils.del(time_key);
            redisUtils.del(captcha_key);

            // 保存登录后的cookies
            redisUtils.set(cookies_key, mapToString(get_cookies), 60 * 60); // 保存60分钟
        } else {
            // 登录不成功，保存cookies和time备用
            if (cookies == null) {
                // 保存登录后的cookies
                redisUtils.set(cookies_key, mapToString(loginResponse.cookies()), 60 * 10); // 保存10分钟
                time = getCurrentTimestamp();

                redisUtils.set(time_key, time, 60 * 10); // 十分钟不尝试，该限制会自动清空消失
            }

            handleLoginError(loginUsername, cookies_, loginResponse.body(), time);
        }

        return rankDatas;
    }

    @Override
    public List<ACMContestRankVO> getScraperInfo(String cid, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {
        return null;
    }

    public static Connection.Response login(String username, String password, String captcha,
            Map<String, String> cookies) throws IOException {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        // 构建请求
        Connection connection = Jsoup.connect(LOGIN_URL)
                .headers(headers)
                .data("username", username)
                .data("password", password);

        // 只有在 captcha 不为 null 时才添加 captcha 数据
        if (!StringUtils.isEmpty(captcha)) {
            connection.data("captcha", captcha);
        }

        if (cookies != null && !cookies.isEmpty()) {
            connection.cookies(cookies);
        }

        // 执行请求
        Connection.Response loginResponse = connection.method(Connection.Method.POST).execute();

        return loginResponse;
    }

    public static String getCurrentTimestamp() {
        // 获取当前时间戳（以毫秒为单位）
        long timestampMs = System.currentTimeMillis();

        // 将时间戳转换为秒并保留一位小数
        double timestampDecimal = timestampMs * 1.0;
        String formattedTimestamp = String.format("%.1f", timestampDecimal);

        return formattedTimestamp;
    }

    public static void downloadCaptcha(String fileName, String timestamp, Map<String, String> cookies)
            throws IOException, Exception {
        String captchaUrl = String.format(CAPTCHA_URL, timestamp);

        // 创建连接并设置请求方法和 Cookies
        Connection connection = Jsoup.connect(captchaUrl)
                .method(Connection.Method.GET);

        if (cookies != null && !cookies.isEmpty()) {
            connection.cookies(cookies);
        }

        // 执行请求并获取响应
        Response response = connection.execute();

        String filePath = Constants.File.CAPTCHA_FOLDER.getPath() + File.separator + fileName;

        // 检查响应状态码
        if (response.statusCode() == 200) {
            try (InputStream inputStream = response.bodyStream();
                    OutputStream outputStream = new FileOutputStream(new File(filePath))) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } else {
            String msg = "[VJ] Scraper Download Captcha Error";
            throw new Exception(msg);
        }
    }

    public static String getRankInfo(String cid, Map<String, String> cookies) throws IOException {
        // 构造请求的 URL
        String urlString = String.format(CONTEST_RANK_URL, cid);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置请求方法和头信息
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/html, application/xhtml+xml, */*");

        // 设置 cookies
        StringJoiner cookieHeader = new StringJoiner("; ");
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            cookieHeader.add(cookie.getKey() + "=" + cookie.getValue());
        }
        connection.setRequestProperty("Cookie", cookieHeader.toString());

        // 读取响应，指定编码为 UTF-8
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        // 关闭连接和缓冲读取器
        in.close();
        connection.disconnect();

        return content.toString();
    }

    public static List<ACMContestRankVO> dealRank(String html, Map<String, String> usernameToUidMap) throws Exception {

        if (StringUtils.isEmpty(html)) {
            throw new Exception("Vj 爬取的html为空！");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rankJson = mapper.readTree(html);

        // 比赛ID
        String cid = rankJson.get("id").asText();
        String title = rankJson.get("title").asText();
        String link = String.format(RANK_URL, cid);
        Date startTime = getContestDate(rankJson.get("begin").asLong());

        // 比赛时长（秒）
        double duration = rankJson.get("length").asDouble() / 1000;
        // 参赛选手信息
        JsonNode participants = rankJson.get("participants");

        // 初始化用户统计数据
        Map<String, Map<String, Object>> userStats = new HashMap<>();

        // 对提交记录按时间进行排序
        List<JsonNode> submissions = new ArrayList<>();
        rankJson.get("submissions").forEach(submissions::add);
        submissions.sort(Comparator.comparingLong(submission -> submission.get(3).asLong()));

        for (JsonNode submission : submissions) {
            String uid = submission.get(0).asText();
            String pid = submission.get(1).asText();
            int status = submission.get(2).asInt();
            long costTime = submission.get(3).asLong();

            userStats.putIfAbsent(uid, new HashMap<>());
            Map<String, Object> userStat = userStats.get(uid);
            userStat.putIfAbsent("ac_count", 0);
            userStat.putIfAbsent("penalty_time", 0L);
            userStat.putIfAbsent("attempts", new HashMap<String, Long>());
            userStat.putIfAbsent("accepted", new HashMap<String, Boolean>());

            Map<String, Long> attempts = (Map<String, Long>) userStat.get("attempts");
            attempts.putIfAbsent(pid, 0L);
            Map<String, Boolean> accepted = (Map<String, Boolean>) userStat.get("accepted");
            accepted.putIfAbsent(pid, false);

            // 不包含赛后提交
            if (costTime <= duration) {
                if (accepted.get(pid)) { // 被标记过的已ac的题目
                    continue;
                }
                if (status == 1) { // 如果提交状态是正确的
                    userStat.put("ac_count", (int) userStat.get("ac_count") + 1);
                    long penaltyTime = costTime + attempts.getOrDefault(pid, 0L);
                    userStat.put("penalty_time", (long) userStat.get("penalty_time") + penaltyTime);
                    accepted.put(pid, true);// 标记该题目已经正确提交
                } else { // 如果提交状态是错误的
                    attempts.put(pid, attempts.getOrDefault(pid, 0L) + 1200L);
                }
            }
        }

        // 生成排名数据
        List<ACMContestRankVO> rankDataList = new ArrayList<>();
        participants.fields().forEachRemaining(entry -> {
            String uid = entry.getKey();

            JsonNode participant = entry.getValue();
            String username = participant.get(0).asText();
            String realname = participant.get(1).asText();

            // 检查是否已有对应的 UID
            String uid_ = usernameToUidMap.computeIfAbsent(username, k -> IdUtil.fastSimpleUUID());

            if (userStats.containsKey(uid)) {
                Map<String, Object> stats = userStats.get(uid);

                int acCount = (int) stats.getOrDefault("ac_count", 0);
                long penaltyTime = (long) stats.getOrDefault("penalty_time", 0);

                ACMContestRankVO vo = new ACMContestRankVO()
                        .setCid(cid)
                        .setTitle(title)
                        .setStartTime(startTime)
                        .setLink(link)
                        .setSynchronous(true)
                        .setAc((double) acCount)
                        .setTotalTime((double) penaltyTime)
                        .setUsername(username)
                        .setRealname(realname.split("（")[0].split("\\(")[0].trim())
                        .setUid(uid_);

                rankDataList.add(vo);
            }
        });

        // 按照规定的排序方式对rankDatas排序
        rankDataList.sort(Comparator.comparing(ACMContestRankVO::getAc).reversed()
                .thenComparing(ACMContestRankVO::getTotalTime));

        // 填充ranking
        for (int i = 0; i < rankDataList.size(); i++) {
            rankDataList.get(i).setRank(i + 1);
        }

        return rankDataList;
    }

    public static String mapToString(Map<String, String> map) {
        StringJoiner stringJoiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            stringJoiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return stringJoiner.toString();
    }

    public static Map<String, String> stringToMap(String str) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = str.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            }
        }
        return map;
    }

    private void handleLoginError(String loginUsername, Map<String, String> cookies_, String error, String time)
            throws Exception {
        String msg = "[VJ] Scraper Login Error：" + error;

        if (error.contains("Captcha is wrong")) {
            String fileName = handleCaptchaError(time, loginUsername, cookies_);
            msg = "[VJ] Your Captcha :" + fileName;
        }

        throw new Exception(msg);
    }

    public String handleCaptchaError(String time, String loginUsername, Map<String, String> cookies_) throws Exception {
        String captcha_key = Constants.Account.VJ_CAPTCHA_UID.getCode() + loginUsername;

        String fileName = IdUtil.fastSimpleUUID() + ".png";

        downloadCaptcha(fileName, time, cookies_);

        redisUtils.set(captcha_key, fileName, 60 * 5); // 五分钟不尝试，该限制会自动清空消失

        return Constants.File.IMG_API.getPath() + fileName;
    }

}
