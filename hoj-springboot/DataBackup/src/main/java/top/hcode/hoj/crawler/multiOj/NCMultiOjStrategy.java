package top.hcode.hoj.crawler.multiOj;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.utils.JsoupUtils;

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
public class NCMultiOjStrategy extends MultiOjStrategy {

    @Resource
    private ApplicationContext applicationContext;

    public static final String OJ = "nowcoder";
    public static final String HOST = "https://ac.nowcoder.com";
    public static final String USERHOMEAPI = HOST + "/acm/contest/profile/%s/practice-coding";

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

            // 检测用户名是否符合格式
            if (!multiOjUsername.matches("\\d+")) {
                String msg = String.format("'%s' Don't Have Such user_id: '%s'", OJ, multiOjUsername);
                return multiOjDto.setUid(uid).setMsg(msg);
            }

            // 格式化api
            String acAPI = String.format(USERHOMEAPI, multiOjUsername);

            Connection connection = JsoupUtils.getConnectionFromUrl(acAPI, null, headers, false);
            Document document = JsoupUtils.getDocument(connection, null);

            // 使用选择器查找具有指定 src 属性的 img 元素
            Elements imgElements = document.select("img[src=\"//static.nowcoder.com//images/404-new.png\"]");
            // 如果找到匹配元素，说明没有该用户
            if (!imgElements.isEmpty()) {
                String msg = String.format("'%s' Don't Have Such user_id: '%s'", OJ, multiOjUsername);
                return multiOjDto.setUid(uid).setMsg(msg);
            }

            Integer ranking = null, solved = null;
            Elements elements = document.getElementsByClass("state-num");
            if (!elements.isEmpty()) {
                String pageContent = elements.text();

                // 使用正则表达式匹配数字
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(pageContent);

                if (matcher.find()) {
                    ranking = Integer.parseInt(matcher.group());
                    matcher.find();
                    if (matcher.find()) {
                        solved = Integer.parseInt(matcher.group());
                    }
                }
            }

            return multiOjDto
                    .setUid(uid)
                    .setRanking(ranking)
                    .setResolved(solved);
        } catch (Exception e) {
            log.error("爬虫爬取Nowcoder异常----------------------->{}", e.getMessage());
            return multiOjDto.setMsg(e.getMessage());
        }
    }

}