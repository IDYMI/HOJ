package top.hcode.hoj.crawler.multiOj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.utils.JsoupUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;

@Slf4j(topic = "hoj")
public class CFMultiOjStrategy extends MultiOjStrategy {

    @Resource
    private ApplicationContext applicationContext;

    public static final String OJ = "codeforces";
    public static final String HOST = "https://codeforces.com";
    public static final String USERINFOAPI = HOST + "/api/user.info?handles=%s";
    public static final String USERHOMEAPI = HOST + "/profile/%s";

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    @Override
    public MultiOjDto getMultiOjInfo(String uid, String username, String multiOjUsername)
            throws Exception, StatusFailException {
        MultiOjDto multiOjDto = new MultiOjDto()
                .setMultiOj(OJ)
                .setUsername(username)
                .setMultiOjUsername(multiOjUsername);
        try {
            // 格式化api
            String ratingAPI = String.format(USERINFOAPI, multiOjUsername);
            String acAPI = String.format(USERHOMEAPI, multiOjUsername);

            // 连接api，获取json格式对象
            JSONObject resultObject = JsoupUtils
                    .getJsonFromConnection(JsoupUtils.getConnectionFromUrl(ratingAPI, null, null, false));

            // 获取状态码
            String status = resultObject.getStr("status");

            // 如果查无此用户，则跳过
            if ("FAILED".equals(status)) {
                String msg = String.format("'%s' Don't Have Such user_id: '%s'", OJ, multiOjUsername);
                return multiOjDto.setUid(uid).setMsg(msg);
            }
            // 用户信息存放在result列表中的第0个
            JSONObject cfUserInfo = resultObject.getJSONArray("result").getJSONObject(0);

            // 获取cf的分数
            Integer ranking = cfUserInfo.getInt("rating", null);
            // 获取cf的最高分数
            Integer maxRanking = cfUserInfo.getInt("maxRating", null);
            // 获取cf的AC数
            Integer solved = getUserHomeAc(acAPI);

            return multiOjDto
                    .setUid(uid)
                    .setRanking(ranking)
                    .setMaxRanking(maxRanking)
                    .setResolved(solved);
        } catch (Exception e) {
            log.error("爬虫爬取Codeforces异常----------------------->{}", e.getMessage());
            return multiOjDto.setMsg(e.getMessage());
        }
    }

    private Integer getUserHomeAc(String acAPI) throws IOException {
        Connection connection = JsoupUtils.getConnectionFromUrl(acAPI, null, headers, false);
        Document document = JsoupUtils.getDocument(connection, null);

        Elements elements = document.getElementsByClass("_UserActivityFrame_counterValue");
        if (!elements.isEmpty()) {
            String pageContent = elements.first().text();

            // 使用正则表达式匹配数字
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(pageContent);

            // 提取匹配到的第一个数字
            if (matcher.find()) {
                String acString = matcher.group();
                // 将提取的字符串转换为整数并返回
                return Integer.parseInt(acString);
            }
        }
        return null;
    }

}