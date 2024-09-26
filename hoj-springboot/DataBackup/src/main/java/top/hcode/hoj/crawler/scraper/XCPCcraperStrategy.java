package top.hcode.hoj.crawler.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.sql.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;

@Slf4j(topic = "hoj")
public class XCPCcraperStrategy extends ScraperStrategy {

    private static final String JSON_URL = "https://board.xcpcio.com/data/%s/";
    private static final String RANK_JSON = "run.json";
    private static final String TEAM_JSON = "team.json";
    private static final String CONFIG_JSON = "config.json";
    private static final String RANK_URL = "https://board.xcpcio.com/%s";

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
        String rankHtml = getRankInfo(cid, RANK_JSON);

        // 获取团队信息
        String teamHtml = getRankInfo(cid, TEAM_JSON);

        String configHtml = getRankInfo(cid, CONFIG_JSON);

        // 处理排名数据
        rankDatas = dealRank(rankHtml, teamHtml, configHtml, cid, usernameToUidMap);

        return rankDatas;
    }

    public static String getRankInfo(String cid, String json) throws IOException {
        String urlString = String.format(JSON_URL, cid) + json;

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

    public static List<ACMContestRankVO> dealRank(String rankHtml, String teamHtml, String configHtml, String cid,
            Map<String, String> usernameToUidMap) throws Exception {
        List<ACMContestRankVO> rankDataList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        // 参赛选手信息
        JsonNode participants = objectMapper.readTree(teamHtml);

        JSONObject configJson = new JSONObject(configHtml);

        String link = String.format(RANK_URL, cid);
        String title = configJson.optString("contest_name");
        Date startTime = getContestDate(configJson.optLong("start_time"));

        // 解析为 JSONArray 对象
        JSONArray jsonArray = new JSONArray(rankHtml);
        // 将JSONArray转换为List<JSONObject>
        List<JSONObject> submissions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            submissions.add(jsonArray.getJSONObject(i));
        }

        // 对List<JSONObject>进行排序
        submissions.sort(Comparator.comparingLong(obj -> obj.getLong("timestamp")));

        // 初始化用户统计数据
        Map<String, Map<String, Object>> userStats = new HashMap<>();

        for (JSONObject submission : submissions) {
            String uid = submission.optString("team_id");
            String pid = submission.optString("problem_id");
            String status = submission.optString("status");
            long costTime = submission.optInt("timestamp") / 1000;

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

            if (accepted.get(pid)) { // 被标记过的已ac的题目
                continue;
            }
            if (status.equals("ACCEPTED")) { // 如果提交状态是正确的
                userStat.put("ac_count", (int) userStat.get("ac_count") + 1);
                long penaltyTime = costTime + attempts.getOrDefault(pid, 0L);
                userStat.put("penalty_time", (long) userStat.get("penalty_time") + penaltyTime);
                accepted.put(pid, true);// 标记该题目已经正确提交
            } else { // 如果提交状态是错误的
                attempts.put(pid, attempts.getOrDefault(pid, 0L) + 1200L);
            }
        }

        // 生成排名数据
        participants.fields().forEachRemaining(entry -> {
            String uid = entry.getKey();
            JsonNode participant = entry.getValue();
            String username = participant.get("name").asText();
            String school = participant.get("organization").asText();

            // 检查是否已有对应的 UID
            String uid_ = usernameToUidMap.computeIfAbsent(username, k -> IdUtil.fastSimpleUUID());

            if (userStats.containsKey(uid)) {
                Map<String, Object> stats = userStats.get(uid);

                int acCount = (int) stats.getOrDefault("ac_count", 0);
                long penaltyTime = (long) stats.getOrDefault("penalty_time", 0);

                rankDataList.add(new ACMContestRankVO()
                        .setTitle(title)
                        .setStartTime(startTime)
                        .setCid(cid)
                        .setLink(link)
                        .setSynchronous(true)
                        .setAc((double) acCount)
                        .setTotalTime((double) penaltyTime)
                        .setUsername(username)
                        .setSchool(school)
                        .setUid(uid_));
            }
        });

        // 按照规定的排序方式对rankDatas排序
        rankDataList.sort(Comparator.comparing(ACMContestRankVO::getAc).reversed()
                .thenComparing(ACMContestRankVO::getTotalTime));

        return rankDataList;

    }

}
