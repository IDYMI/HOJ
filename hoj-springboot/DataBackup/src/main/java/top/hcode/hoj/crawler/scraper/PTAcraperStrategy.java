package top.hcode.hoj.crawler.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import top.hcode.hoj.pojo.vo.ACMContestRankVO;

@Slf4j(topic = "hoj")
public class PTAcraperStrategy extends ScraperStrategy {

    private static final String JSON_RANK_URL = "https://pintia.cn/api/competitions/%s/xcpc-rankings";
    private static final String RANK_URL = "https://pintia.cn/rankings/%s";

    @Override
    public List<ACMContestRankVO> getScraperInfoByLogin(String cid, String loginUsername, String loginPassword,
            String captcha, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {
        return null;
    }

    @Override
    public List<ACMContestRankVO> getScraperInfo(String cid, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {

        List<ACMContestRankVO> rankDatas = new ArrayList<>();

        // 获取排名信息
        String rankHtml = getRankInfo(cid);

        // 处理排名数据
        rankDatas = dealRank(rankHtml, cid, usernameToUidMap);

        return rankDatas;
    }

    public static String getRankInfo(String cid) throws IOException {
        String urlString = String.format(JSON_RANK_URL, cid);

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

    public static List<ACMContestRankVO> dealRank(String html, String cid, Map<String, String> usernameToUidMap)
            throws Exception {
        List<ACMContestRankVO> rankDataList = new ArrayList<>();

        // 解析 JSON 数据
        JSONObject jsonObject = new JSONObject(html);
        JSONObject competitionBasicInfo = jsonObject.getJSONObject("competitionBasicInfo");
        JSONObject dataObject = jsonObject.getJSONObject("xcpcRankings");
        JSONArray rankDataArray = dataObject.getJSONArray("rankings");

        String title = competitionBasicInfo.optString("name");
        // 将字符串解析为 Instant
        Instant instant = Instant.parse(competitionBasicInfo.optString("startAt"));
        // 将 Instant 转换为 java.sql.Date
        Date startTime = getContestDate(instant.toEpochMilli());

        String link = String.format(RANK_URL, cid);

        for (int i = 0; i < rankDataArray.length(); i++) {
            try {
                JSONObject data = rankDataArray.getJSONObject(i);
                JSONObject teamInfo = data.getJSONObject("teamInfo");

                int ranking = data.optInt("rank");
                int acceptedCount = data.optInt("solvedCount");
                long penaltyTimeInMinutes = data.optLong("solvingTime") * 60;

                String school = teamInfo.optString("schoolName");
                String username = teamInfo.optString("teamName");
                JSONArray memberNames = teamInfo.getJSONArray("memberNames");

                List<String> handles = new ArrayList<>();
                memberNames.forEach(item -> {
                    // 转换为 String，并去除多余的引号
                    String handle = ((String) item).replace("\"", "");
                    handles.add(handle);
                });

                String realname = getOrderNumber(handles);

                // 检查是否已有对应的 UID
                String uid = usernameToUidMap.computeIfAbsent(username, k -> IdUtil.fastSimpleUUID());

                rankDataList.add(new ACMContestRankVO()
                        .setTitle(title)
                        .setStartTime(startTime)
                        .setCid(cid)
                        .setLink(link)
                        .setSynchronous(true)
                        .setAc((double) acceptedCount)
                        .setTotalTime((double) penaltyTimeInMinutes)
                        .setRank(ranking)
                        .setUsername(username)
                        .setRealname(realname)
                        .setUid(uid)
                        .setSchool(school));

            } catch (Exception e) {
                // 将异常封装为 RuntimeException 以便抛出
                throw new RuntimeException(e);
            }
        }

        return rankDataList;
    }

}
