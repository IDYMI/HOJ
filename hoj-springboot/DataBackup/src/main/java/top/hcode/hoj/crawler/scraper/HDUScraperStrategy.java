package top.hcode.hoj.crawler.scraper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import top.hcode.hoj.pojo.vo.ACMContestRankVO;

@Slf4j(topic = "hoj")
public class HDUScraperStrategy extends ScraperStrategy {

    private static final String LOGIN_URL = "http://acm.hdu.edu.cn/contest/login?cid=%s";
    private static final String RANK_URL = "http://acm.hdu.edu.cn/contest/rank?cid=%s";

    @Override
    public List<ACMContestRankVO> getScraperInfoByLogin(String cid, String loginUsername, String loginPassword,
            String captcha, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {

        List<ACMContestRankVO> rankDatas = new ArrayList<>();
        // 登录
        Connection.Response loginResponse = login(cid, loginUsername, loginPassword);

        // 检查登录是否成功
        if (loginResponse.statusCode() == HttpURLConnection.HTTP_OK) {
            // 获取排名信息
            Elements elements = getRankInfo(cid, keyword, loginResponse);

            // 处理排名数据
            rankDatas = dealRank(elements, cid, usernameToUidMap);

        } else {
            String msg = "[HDU] Scraper Login Error：" + loginResponse.body();
            throw new Exception(msg);
        }

        return rankDatas;
    }

    @Override
    public List<ACMContestRankVO> getScraperInfo(String cid, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {
        return null;
    }

    public static Connection.Response login(String cid, String username, String password) throws IOException {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        Connection connection = getLoginConnection(cid, username, password);

        // 获取第一页内容
        return connection.execute();
    }

    public static Elements getRankInfo(String cid, String keyword, Connection.Response loginResponse)
            throws IOException, InterruptedException, ExecutionException {
        String url = String.format(RANK_URL, cid);
        Connection connection = getRankConnection(url, loginResponse, keyword);

        // 获取第一页内容，解析出最大页数
        Document doc = Jsoup.parse(connection.execute().body());
        int maxPage = getMaxPage(doc);

        // 创建线程池，线程数量可以根据需要进行调整
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(maxPage, 10));

        // 保存每个页面处理的 Future 结果
        List<Future<Elements>> futures = new ArrayList<>();

        // 使用多线程请求每一页的内容
        for (int i = 1; i <= maxPage; i++) {
            final int pageIndex = i;
            futures.add(executor.submit(() -> {
                String pageUrl = url + (pageIndex > 1 ? "&page=" + pageIndex : "");
                Connection pageConnection = getRankConnection(pageUrl, loginResponse, keyword);
                Document pageDoc = Jsoup.parse(pageConnection.execute().body());
                return pageDoc.select("tr.page-card-row");
            }));
        }

        // 等待所有任务完成并收集所有表格行
        Elements allRows = new Elements();
        for (Future<Elements> future : futures) {
            allRows.addAll(future.get()); // 获取每个任务的结果并合并
        }

        // 关闭线程池
        executor.shutdown();

        return allRows;
    }

    public static List<ACMContestRankVO> dealRank(Elements elements, String cid, Map<String, String> usernameToUidMap)
            throws IOException {
        List<ACMContestRankVO> rankDataList = new ArrayList<>();

        Connection connection = getLoginConnection(cid, null, null);
        String contesthtml = connection.execute().body();
        // 解析 HTML
        Document contsetDoc = Jsoup.parse(contesthtml);
        // 提取比赛标题
        String title = contsetDoc.select("div.contest-info h2").text();
        // 提取比赛开始时间
        Elements contestTimeElements = contsetDoc.select("div.info-pair:contains(Contest Time) .info-value");
        String contestStartTime = contestTimeElements.text().split("~")[0].trim();

        Date startTime = null;
        try {
            // 定义日期格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy HH:mm:ss", java.util.Locale.ENGLISH);
            // 将字符串解析为 java.util.Date
            java.util.Date parsedDate = dateFormat.parse(contestStartTime);
            // 将 java.util.Date 转换为 java.sql.Date
            startTime = new Date(parsedDate.getTime());
        } catch (ParseException e) {
        }

        String link = String.format(RANK_URL, cid);

        for (Element row : elements) {
            Elements cols = row.select("td");
            String ranking = cols.get(0).text().trim();
            String username_school = cols.get(1).text();
            String ac = cols.get(2).text().trim();
            String penaltyTime = cols.get(3).text().trim();

            String[] splitUsername = username_school.split(" "); // 按照换行符分割字符串
            String username = splitUsername[0];

            // 检查是否已有对应的 UID
            String uid = usernameToUidMap.computeIfAbsent(username, k -> IdUtil.fastSimpleUUID());

            rankDataList.add(new ACMContestRankVO()
                    .setCid(cid)
                    .setTitle(title)
                    .setStartTime(startTime)
                    .setLink(link)
                    .setSynchronous(true)
                    .setAc((double) Integer.parseInt(ac))
                    .setTotalTime((double) timeToMinutes(penaltyTime))
                    .setRank(Integer.parseInt(ranking))
                    .setUsername(username)
                    .setUid(uid)
                    .setSchool(splitUsername[1]));
        }

        return rankDataList;
    }

    // 获取最大页码
    private static int getMaxPage(Document doc) {
        Elements pageItems = doc.select("div.page-card-pagination ul.pagination li.page-item a.page-link");
        return pageItems.stream()
                .map(Element::text)
                .filter(text -> text.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(1);
    }

    private static Long timeToMinutes(String time) {
        if (time == null || time.isEmpty()) {
            return 0L; // 空字符串或 null 返回 0 分钟
        }

        String[] parts = time.split(":");
        if (parts.length != 3) {
            return 0L; // 时间格式不正确，返回 0 分钟
        }

        try {
            int hours = Integer.parseInt(parts[0].trim());
            int minutes = Integer.parseInt(parts[1].trim());
            int second = Integer.parseInt(parts[2].trim());
            return (long) hours * 3600 + minutes * 60 + second;
        } catch (NumberFormatException e) {
            return 0L; // 格式错误，返回 0 分钟
        }
    }

    public static Connection getLoginConnection(String cid, String username, String password) {
        String url = String.format(LOGIN_URL, cid);

        Connection connection = Jsoup.connect(url)
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0")
                .header("Referer", url)

                .method(Connection.Method.POST);

        if (!StringUtils.isEmpty(username)) {
            connection.data("username", username);
        }

        if (!StringUtils.isEmpty(password)) {
            connection.data("password", password);
        }

        return connection;
    }

    // 创建连接，封装公共逻辑
    private static Connection getRankConnection(String url, Connection.Response loginResponse, String keyword) {
        Connection connection = Jsoup.connect(url)
                .cookies(loginResponse.cookies())
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0")
                .header("Referer", url)
                .data("group", "all");

        if (!StringUtils.isEmpty(keyword)) {
            connection.data("search", keyword);
        }
        return connection;
    }

}
