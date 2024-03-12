package top.hcode.hoj.crawler.multiOj;

import top.hcode.hoj.pojo.dto.MultiOjDto;

public abstract class MultiOjStrategy {

    public abstract MultiOjDto getMultiOjInfo(String uid, String username, String multiOjUsername) throws Exception;

    public MultiOjDto getMultiOjInfoByLogin(String uid, String username, String multiOjUsername, String loginUsername,
            String loginPassword)
            throws Exception {
        return null;
    }

}
