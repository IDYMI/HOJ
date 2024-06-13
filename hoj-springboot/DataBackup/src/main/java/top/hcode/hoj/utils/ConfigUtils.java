package top.hcode.hoj.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.vo.ConfigVO;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 23:17
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class ConfigUtils {

    @Autowired
    private ConfigVO configVo;

    public String getConfigContent() {
        return buildYamlStr(configVo);
    }

    public String buildYamlStr(ConfigVO configVo) {
        return "hoj:\n" +
                "  jwt:\n" +
                "    secret: " + configVo.getTokenSecret() + "\n" +
                "    expire: " + configVo.getTokenExpire() + "\n" +
                "    checkRefreshExpire: " + configVo.getCheckRefreshExpire() + "\n" +
                "    header: token\n" +
                "  judge:\n" +
                "    token: " + configVo.getJudgeToken() + "\n" +
                "  db:\n" +
                "    host: " + configVo.getMysqlHost() + "\n" +
                "    port: " + configVo.getMysqlPort() + "\n" +
                "    public-host: " + configVo.getMysqlPublicHost() + "\n" +
                "    public-port: " + configVo.getMysqlPublicPort() + "\n" +
                "    name: " + configVo.getMysqlDBName() + "\n" +
                "    username: " + configVo.getMysqlUsername() + "\n" +
                "    password: " + configVo.getMysqlPassword() + "\n" +
                "  redis:\n" +
                "    host: " + configVo.getRedisHost() + "\n" +
                "    port: " + configVo.getRedisPort() + "\n" +
                "    password: " + configVo.getRedisPassword();
    }

}