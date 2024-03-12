package top.hcode.hoj.crawler.multiOj;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.utils.JsoupUtils;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;

@Slf4j(topic = "hoj")
public class PKMultiOjStrategy extends MultiOjStrategy {

    @Resource
    private ApplicationContext applicationContext;

    public static final String OJ = "poj";
    public static final String HOST = "http://poj.org";
    public static final String USERACAPI = HOST + "/userstatus?user_id=%s";

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
            String acAPI = String.format(USERACAPI, multiOjUsername);

            Connection connection = JsoupUtils.getConnectionFromUrl(acAPI, null, headers, false);
            Document document = JsoupUtils.getDocument(connection, null);

            Element element = document.select("body").first();
            String errorMsg = String.format("Sorry,%s doesn't exist", username);
            if (element.text().contains(errorMsg)) {
                String msg = String.format("'%s' Don't Have Such user_id: '%s'", OJ, multiOjUsername);
                return multiOjDto.setUid(uid).setMsg(msg);
            } else {
                Elements elements = document.select("a[href~=status\\?result=\\d+&user_id=]");

                Integer solved = elements.isEmpty() ? 0 : Integer.valueOf(elements.first().text());

                return multiOjDto
                        .setUid(uid)
                        .setResolved(solved);
            }
        } catch (Exception e) {
            log.error("爬虫爬取Poj异常----------------------->{}", e.getMessage());
            return multiOjDto.setMsg(e.getMessage());
        }
    }

}