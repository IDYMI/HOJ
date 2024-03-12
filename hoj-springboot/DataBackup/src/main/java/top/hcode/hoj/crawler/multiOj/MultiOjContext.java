package top.hcode.hoj.crawler.multiOj;

import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.pojo.dto.MultiOjDto;

@Slf4j(topic = "hoj")
public class MultiOjContext {

    MultiOjStrategy globalStrategy;

    public MultiOjContext(MultiOjStrategy globalStrategy) {
        this.globalStrategy = globalStrategy;
    }

    // 上下文接口
    public MultiOjDto getMultiOjInfo(String uid, String username, String multiOjUsername) throws Exception {
        try {
            return globalStrategy.getMultiOjInfo(uid, username, multiOjUsername);
        } catch (Exception e) {
            log.error("获取Oj数据失败---------------->{}", e);
            throw e;
        }
    }

    // 上下文接口
    public MultiOjDto getMultiOjInfoByLogin(String uid, String username, String multiOjUsername, String loginUsername,
            String loginPassword)
            throws Exception {
        try {
            return globalStrategy.getMultiOjInfoByLogin(uid, username, multiOjUsername, loginUsername, loginPassword);
        } catch (Exception e) {
            log.error("获取Oj数据失败---------------->{}", e);
        }
        return null;
    }

}