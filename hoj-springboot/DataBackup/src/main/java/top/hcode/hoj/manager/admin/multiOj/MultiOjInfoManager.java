package top.hcode.hoj.manager.admin.multiOj;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import top.hcode.hoj.crawler.multiOj.*;
import top.hcode.hoj.dao.multiOj.UserMultiOjEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.pojo.vo.UserRolesVO;

@Component
public class MultiOjInfoManager {

    @Resource
    private UserRoleEntityService userRoleEntityService;

    @Resource
    private UserMultiOjEntityService userMultOjEntityService;

    public MultiOjDto getMultiOjProblemInfo(String username, String multiOj,
            String multiOjUsername)
            throws Exception {

        MultiOjStrategy problemStrategy;
        switch (multiOj) {
            case "CF":
                problemStrategy = new CFMultiOjStrategy();
                break;
            case "NC":
                problemStrategy = new NCMultiOjStrategy();
                break;
            case "VJ":
                problemStrategy = new VJMultiOjStrategy();
                break;
            case "PK":
                problemStrategy = new PKMultiOjStrategy();
                break;
            case "AT":
                problemStrategy = new ATMultiOjStrategy();
                break;
            case "LC":
                problemStrategy = new LCMultiOjStrategy();
                break;
            default:
                throw new Exception("未知的OJ的名字，暂时不支持！");
        }

        MultiOjContext remoteOjContext = new MultiOjContext(problemStrategy);
        try {
            // 获取对应 username 对应的 uid
            UserRolesVO userRolesVo = userRoleEntityService.getUserRoles(null, username);
            if (userRolesVo == null) {
                String msg = String.format("Don't Have Such username: '%s'", username);

                return new MultiOjDto()
                        .setMultiOj(multiOjUsername)
                        .setUsername(username)
                        .setMultiOjUsername(multiOjUsername)
                        .setMsg(msg);
            }

            return remoteOjContext.getMultiOjInfo(userRolesVo.getUid(), username, multiOjUsername);
        } catch (Exception e) {
            return new MultiOjDto()
                    .setMultiOj(multiOjUsername)
                    .setUsername(username)
                    .setMultiOjUsername(multiOjUsername);
        }
    }

}