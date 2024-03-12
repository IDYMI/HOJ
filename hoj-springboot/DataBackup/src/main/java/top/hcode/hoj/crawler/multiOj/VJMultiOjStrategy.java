package top.hcode.hoj.crawler.multiOj;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;

import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.utils.JsoupUtils;

import javax.annotation.Resource;

import org.jsoup.HttpStatusException;
import org.springframework.context.ApplicationContext;

@Slf4j(topic = "hoj")
public class VJMultiOjStrategy extends MultiOjStrategy {

    @Resource
    private ApplicationContext applicationContext;

    public static final String OJ = "vjudge";
    public static final String HOST = "https://vjudge.csgrandeur.cn";
    public static final String USERACAPI = HOST + "/user/solveDetail/%s";

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

            // 连接api，获取json格式对象
            JSONObject resultObject = JsoupUtils
                    .getJsonFromConnection(JsoupUtils.getConnectionFromUrl(acAPI, null, null, false));

            JSONObject acRecords = resultObject.getJSONObject("acRecords");
            int totalSolved = acRecords.values().stream().mapToInt(item -> ((JSONArray) item).size()).sum();


            return multiOjDto
                    .setUid(uid)
                    .setResolved(totalSolved);
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                String msg = String.format("'%s' Don't Have Such user_id: '%s'", OJ, multiOjUsername);
                return multiOjDto.setUid(uid).setMsg(msg);
            } else {
                log.error("爬虫爬取Vjudge异常----------------------->{}", e.getMessage());
                return multiOjDto.setMsg(e.getMessage());
            }
        } catch (Exception e) {
            log.error("爬虫爬取Vjudge异常----------------------->{}", e.getMessage());
            return multiOjDto.setMsg(e.getMessage());
        }
    }

}