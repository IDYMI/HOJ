package top.hcode.hoj.crawler.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.util.StringUtils;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import top.hcode.hoj.pojo.vo.ACMContestRankVO;

@Slf4j(topic = "hoj")
public class NCScraperStrategy extends ScraperStrategy {

    private static final String JSON_RANK_URL = "https://ac.nowcoder.com/acm-heavy/acm/contest/real-time-rank-data";
    private static final String RANK_URL = "https://ac.nowcoder.com/acm/contest/%s#rank";

    @Override
    public List<ACMContestRankVO> getScraperInfoByLogin(String cid, String loginUsername, String loginPassword,
            String captcha, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {
        return null;
    }

    @Override
    public List<ACMContestRankVO> getScraperInfo(String cid, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {

        return fetchAllRankData(cid, usernameToUidMap);
    }

    public static List<ACMContestRankVO> fetchAllRankData(String cid, Map<String, String> usernameToUidMap)
            throws IOException, InterruptedException, ExecutionException {
        List<ACMContestRankVO> allRankData = new ArrayList<>();
        int maxPage = getMaxPage(getRankInfo(cid, null, 1));

        ExecutorService executor = Executors.newFixedThreadPool(10); // 创建一个包含10个线程的线程池
        List<Future<List<ACMContestRankVO>>> futures = new ArrayList<>();

        // 提交任务以获取每个页面的数据
        for (int page = 1; page <= maxPage; page++) {
            int currentPage = page; // 为了在 lambda 表达式中使用
            futures.add(executor.submit(() -> {
                String jsonResponse = getRankInfo(cid, null, currentPage);
                return dealRank(jsonResponse, cid, usernameToUidMap);
            }));
        }

        // 等待所有任务完成并收集所有表格行
        for (Future<List<ACMContestRankVO>> future : futures) {
            allRankData.addAll(future.get()); // 获取每个任务的结果并合并
        }

        executor.shutdown(); // 关闭线程池

        return allRankData;
    }

    public static String getRankInfo(String cid, String keyword, int page) throws IOException {
        String urlString = String.format("%s?token=&id=%s%s&page=%d&limit=0",
                JSON_RANK_URL, cid,
                StringUtils.isEmpty(keyword) ? "" : "&searchUserName=" + URLEncoder.encode(keyword, "UTF-8"),
                page);

        // Open connection and set up request
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        // Read response
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> usernameToUidMap = new HashMap<>();

        NCScraperStrategy ncScraperStrategy = new NCScraperStrategy();
        ncScraperStrategy.getScraperInfo("81598", null, usernameToUidMap);
    }

    public static List<ACMContestRankVO> dealRank(String html, String cid, Map<String, String> usernameToUidMap)
            throws IOException {
        List<ACMContestRankVO> rankDataList = new ArrayList<>();

        // 解析 JSON 数据
        JSONObject jsonObject = new JSONObject(html);
        JSONObject dataObject = jsonObject.getJSONObject("data");

        JSONObject basicInfo = dataObject.getJSONObject("basicInfo");
        JSONArray rankDataArray = dataObject.getJSONArray("rankData");

        String link = String.format(RANK_URL, cid);

        // 获取比赛标题
        String title = getContestTilte(cid);
        Date startTime = getContestDate(basicInfo.optLong("contestBeginTime"));

        for (int i = 0; i < rankDataArray.length(); i++) {
            JSONObject data = rankDataArray.getJSONObject(i);

            String username = data.optString("userName");
            String school = data.optString("school");
            int acceptedCount = data.optInt("acceptedCount");
            long penaltyTimeInMinutes = data.optLong("penaltyTime") / 10000; // 转换为秒
            int ranking = data.optInt("ranking");

            // 检查是否已有对应的 UID
            String uid = usernameToUidMap.computeIfAbsent(username, k -> IdUtil.fastSimpleUUID());

            rankDataList.add(new ACMContestRankVO()
                    .setCid(cid)
                    .setTitle(title)
                    .setStartTime(startTime)
                    .setLink(link)
                    .setSynchronous(true)
                    .setAc((double) acceptedCount)
                    .setTotalTime((double) penaltyTimeInMinutes)
                    .setRank(ranking)
                    .setUsername(username)
                    .setUid(uid)
                    .setSchool(school));
        }

        return rankDataList;
    }

    public static String getContestTilte(String cid) throws IOException {
        // 复制 cookies
        Connection.Response response = Jsoup.connect(String.format(RANK_URL, cid))
                .execute();
        String html = response.body();

        String regex = "\"competitionName_var\":\"([^\"]*)\"";

        Matcher matcher = Pattern.compile(regex).matcher(html);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static Integer getMaxPage(String html) {
        JSONObject jsonObject = new JSONObject(html);
        JSONObject dataObject = jsonObject.getJSONObject("data");

        JSONObject basicInfo = dataObject.getJSONObject("basicInfo");

        return basicInfo.optInt("pageCount");

    }
}
