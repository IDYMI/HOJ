package top.hcode.hoj.crawler.scraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;
import top.hcode.hoj.utils.CodeForcesUtils;

@Slf4j(topic = "hoj")
public class CFScraperStrategy extends ScraperStrategy {

    private static final String IMAGE_HOST = "https://codeforces.com";
    private static final String LOGIN_API = "/enter";
    private static String RANK_API = "/api/contest.standings?contestId=%s&showUnofficial=true";
    private static final String CONTEST_RANK_URL = "/contest/%s/standings";
    private static final String GYM_RANK_URL = "/gym/%s/standings";
    private String RANK_URL;

    private List<HttpCookie> cookies = null;

    public CFScraperStrategy(String scraperType) {
        if ("cf".equals(scraperType)) {
            this.RANK_URL = IMAGE_HOST + CONTEST_RANK_URL;
        } else {
            this.RANK_URL = IMAGE_HOST + GYM_RANK_URL;
        }
    }

    @Override
    public List<ACMContestRankVO> getScraperInfoByLogin(String cid, String loginUsername, String loginPassword,
            String captcha, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {
        List<ACMContestRankVO> rankDatas = new ArrayList<>();
        // 登录
        Connection.Response loginResponse = login(loginUsername, loginPassword);

        if (loginResponse.statusCode() != 200) {
            // 获取排名信息
            String rankHtml = getRankInfo(cid, keyword, loginResponse);

            // 处理排名数据
            rankDatas = dealRank(rankHtml, cid, usernameToUidMap, false);

        } else {
            String msg = "[CF] Scraper Login Error：" + loginResponse.body();
            throw new Exception(msg);
        }

        return rankDatas;
    }

    @Override
    public List<ACMContestRankVO> getScraperInfo(String cid, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {
        List<ACMContestRankVO> rankDatas = new ArrayList<>();

        // 获取排名信息
        String rankHtml = getRankInfo(cid, keyword, null);

        // 处理排名数据
        rankDatas = dealRank(rankHtml, cid, usernameToUidMap, true);

        return rankDatas;
    }

    public Connection.Response login(String username, String password) throws IOException {
        String csrfToken = getCsrfToken(IMAGE_HOST + LOGIN_API, false);

        if (csrfToken != null) {
            // 将 List<HttpCookie> 转换为 Map<String, String>
            Map<String, String> cookieMap = cookies.stream()
                    .collect(Collectors.toMap(HttpCookie::getName, HttpCookie::getValue));

            Connection.Response loginResponse = Jsoup.connect(IMAGE_HOST + LOGIN_API)
                    .data("csrf_token", csrfToken)
                    .data("action", "enter")
                    .data("handleOrEmail", username)
                    .data("password", password)
                    .data("remember", "on")
                    .cookies(cookieMap)
                    .method(Connection.Method.POST)
                    .execute();

            // 检查登录是否成功
            if (!loginResponse.body().contains("Enter")) {
                return loginResponse;
            }
        }

        return null;
    }

    public String getRankInfo(String cid, String keyword, Connection.Response loginResponse) throws IOException {
        if (loginResponse == null) {
            String url = IMAGE_HOST + RANK_API;

            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(url, cid))
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            } finally {
                connection.disconnect();
            }
        } else {
            // 复制 cookies
            Connection.Response response = Jsoup.connect(String.format(RANK_URL, cid))
                    .cookies(loginResponse.cookies()) // 使用登录时获得的 cookies
                    .execute();
            return response.body();
        }
    }

    public List<ACMContestRankVO> dealRank(String html, String cid, Map<String, String> usernameToUidMap,
            Boolean isApi) {
        List<ACMContestRankVO> rankDataList = new ArrayList<>();

        if (isApi) {
            // 解析 JSON 数据
            JSONObject jsonObject = new JSONObject(html);
            JSONObject resultObject = jsonObject.getJSONObject("result");

            JSONArray rankDataArray = resultObject.getJSONArray("rows");
            JSONObject contestData = resultObject.getJSONObject("contest");
            String title = contestData.optString("name");
            Date startTime = getContestDate(contestData.optLong("startTimeSeconds"));
            String link = String.format(RANK_URL, cid);

            for (int i = 0; i < rankDataArray.length(); i++) {
                try {
                    JSONObject data = rankDataArray.getJSONObject(i);
                    JSONObject party = data.getJSONObject("party");

                    int ranking = data.optInt("rank");
                    int acceptedCount = data.optInt("points");
                    long penaltyTimeInMinutes = data.optLong("penalty") * 60;

                    JSONArray memberNames = party.getJSONArray("members");
                    List<String> handles = new ArrayList<>();

                    memberNames.forEach(item -> handles.add(((JSONObject) item).optString("handle")));
                    String username = getOrderNumber(handles);

                    // 检查是否已有对应的 UID
                    String uid = usernameToUidMap.computeIfAbsent(username, k -> IdUtil.fastSimpleUUID());

                    rankDataList.add(new ACMContestRankVO()
                            .setTitle(title)
                            .setCid(cid)
                            .setStartTime(startTime)
                            .setLink(link)
                            .setSynchronous(true)
                            .setAc((double) acceptedCount)
                            .setTotalTime((double) penaltyTimeInMinutes)
                            .setRank(ranking)
                            .setUsername(username)
                            .setUid(uid));

                } catch (Exception e) {
                    // 将异常封装为 RuntimeException 以便抛出
                    throw new RuntimeException(e);
                }
            }

        } else {
            Document doc = Jsoup.parse(html);

            String title = null;
            // 获取标题元素
            Element titleElement = doc.selectFirst("title");
            if (titleElement != null) {
                title = titleElement.text();
                // 使用正则表达式提取中间内容
                Pattern pattern = Pattern.compile("Dashboard - (.*?) - Codeforces");
                Matcher matcher = pattern.matcher(title);
                if (matcher.find()) {
                    title = matcher.group(1);
                }
            }

            // 找到表格
            Element table = doc.selectFirst("table");

            if (table == null) {
                return rankDataList;
            }

            // 从每一行提取数据
            Elements rows = table.select("tr");

            String link = String.format(RANK_URL, cid);

            for (Element row : rows.subList(1, rows.size() - 1)) { // 跳过表头和表尾
                Elements columns = row.select("th, td");
                String ranking = columns.get(0).text().trim();
                String username = columns.get(1).text().trim();
                String ac = columns.get(2).text().trim();
                String penaltyTime = columns.get(3).text().trim();

                // 检查是否已有对应的 UID
                String uid = usernameToUidMap.computeIfAbsent(username, k -> IdUtil.fastSimpleUUID());

                rankDataList.add(new ACMContestRankVO()
                        .setTitle(title)
                        .setCid(cid)
                        .setLink(link)
                        .setSynchronous(true)
                        .setAc(Double.valueOf(ac))
                        .setTotalTime(Double.valueOf(penaltyTime) * 60)
                        .setRank(Integer.parseInt(ranking))
                        .setUsername(username)
                        .setUid(uid));
            }

        }

        // for (int i = 0; i < rankDataList.size() && i < 10; i++) {
        // System.out.println(rankDataList.get(i));
        // }

        return rankDataList;
    }

    public String getCsrfToken(String url, boolean needTTA) {

        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        HttpRequest request = HttpUtil.createGet(url).header("accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                .header("cache-control", "max-age=0")
                .header("priority", "u=0, i")
                .header("referer", "https://codeforces.com/enter")
                .header("sec-ch-ua", "\"Not)A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"127\", \"Chromium\";v=\"127\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "document")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0");

        if (cookies == null) {
            request.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());
        } else {
            request.cookie(cookies);
        }

        HttpResponse response = request.execute();

        String body = response.body();

        if (body.contains("Redirecting... Please, wait.")) {
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", body, 0, new ArrayList<>());

            CodeForcesUtils.updateRCPC(list);

            request.removeHeader("cookie");

            request.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());
            response = request.execute();
            body = response.body();
        }

        cookies = response.getCookies();

        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", body, 1);

        return csrfToken;
    }

}
