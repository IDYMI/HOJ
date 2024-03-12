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
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j(topic = "hoj")
public class LCMultiOjStrategy extends MultiOjStrategy {

    @Resource
    private ApplicationContext applicationContext;

    public static final String OJ = "leetcode";
    public static final String HOST = "https://leetcode.cn";
    public static final String USERHOMEAPI = HOST + "/graphql/noj-go/";

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .put("content-type", "application/json")
            .put("Connection", "keep-alive")
            .map();

    @Override
    public MultiOjDto getMultiOjInfo(String uid, String username, String multiOjUsername)
            throws Exception, StatusFailException {
        MultiOjDto multiOjDto = new MultiOjDto()
                .setMultiOj(OJ)
                .setUsername(username)
                .setMultiOjUsername(multiOjUsername);
        try {

            String query = "{\"query\":\"\\n    query languageStats($userSlug: String!) {\\n  userLanguageProblemCount(userSlug: $userSlug) {\\n    languageName\\n    problemsSolved\\n  }\\n}\\n    \",\"variables\":{\"userSlug\":\"%s\"},\"operationName\":\"languageStats\"}";
            String query_ = String.format(query, multiOjUsername);

            Connection connection = JsoupUtils.getConnectionFromUrl(USERHOMEAPI, null, headers, true);
            // 设置忽略请求类型
            connection.ignoreContentType(true);
            connection.requestBody(query_);

            String body = connection.execute().body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);

            JsonNode userLanguageProblemCount = jsonNode.get("data").get("userLanguageProblemCount");

            if (userLanguageProblemCount.isNull()) {
                String msg = String.format("'%s' Don't Have Such user_id: '%s'", OJ, multiOjUsername);
                return multiOjDto.setUid(uid).setMsg(msg);
            }

            int totalProblemsSolved = 0;
            for (JsonNode node : userLanguageProblemCount) {
                totalProblemsSolved += node.get("problemsSolved").asInt();
            }

            return multiOjDto
                    .setUid(uid)
                    .setResolved(totalProblemsSolved);
        } catch (Exception e) {
            log.error("爬虫爬取LeetCode异常----------------------->{}", e.getMessage());
            return multiOjDto.setMsg(e.getMessage());
        }
    }

}