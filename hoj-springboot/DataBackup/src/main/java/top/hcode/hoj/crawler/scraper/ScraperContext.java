package top.hcode.hoj.crawler.scraper;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.pojo.vo.ACMContestRankVO;

@Slf4j(topic = "hoj")
@Component
public class ScraperContext {

    @Autowired
    private ScraperStrategy scraperStrategy;

    public ScraperContext(ScraperStrategy scraperStrategy) {
        this.scraperStrategy = scraperStrategy;
    }

    // 上下文接口
    public List<ACMContestRankVO> getScraperInfo(String cid, String keyword, Map<String, String> usernameToUidMap)
            throws Exception {
        return scraperStrategy.getScraperInfo(cid, keyword, usernameToUidMap);
    }

    // 上下文接口
    public List<ACMContestRankVO> getScraperInfoByLogin(String cid, String keyword, String loginUsername,
            String loginPassword, String captcha, Map<String, String> usernameToUidMap)
            throws Exception {
        return scraperStrategy.getScraperInfoByLogin(cid, loginUsername, loginPassword, captcha, keyword,
                usernameToUidMap);
    }

}