package top.hcode.hoj.crawler.multiOj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.utils.JsoupUtils;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.springframework.context.ApplicationContext;

@Slf4j(topic = "hoj")
public class ATMultiOjStrategy extends MultiOjStrategy {

    @Resource
    private ApplicationContext applicationContext;

    public static final String OJ = "atcode";
    public static final String HOST = "https://kenkoooo.com/atcoder";
    public static final String USERACAPI = HOST + "/atcoder-api/v3/user/ac_rank?user=%s";

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

            Connection connection = JsoupUtils.getConnectionFromUrl(acAPI, null, null, false);

            // 连接api，获取json格式对象
            JSONObject resultObject = JsoupUtils.getJsonFromConnection(connection);

            Integer solved = Integer.valueOf(resultObject.getStr("count"));

            return multiOjDto
                    .setUid(uid)
                    .setResolved(solved);
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                String msg = String.format("'%s' Don't Have Such user_id: '%s'", OJ, multiOjUsername);
                return multiOjDto.setUid(uid).setMsg(msg);
            } else {
                log.error("爬虫爬取Atcode异常----------------------->{}", e.getMessage());
                return multiOjDto.setMsg(e.getMessage());
            }
        } catch (Exception e) {
            log.error("爬虫爬取Atcode异常----------------------->{}", e.getMessage());
            return multiOjDto.setMsg(e.getMessage());
        }
    }

}