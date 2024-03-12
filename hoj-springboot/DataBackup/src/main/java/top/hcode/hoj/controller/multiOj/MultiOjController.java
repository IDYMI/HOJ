package top.hcode.hoj.controller.multiOj;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.service.admin.multi.MultiOjService;

@RestController
@RequestMapping("/api/multi")

public class MultiOjController {

    @Autowired
    private MultiOjService multiOjService;

    @GetMapping("/get-multi-oj-info")
    @RequiresRoles(value = { "root", "admin", "problem_admin" }, logical = Logical.OR)
    @RequiresAuthentication
    public CommonResult<MultiOjDto> getMultiOjInfo(
            @RequestParam("username") String username,
            @RequestParam("multiOj") String multiOj,
            @RequestParam("multiOjUsername") String multiOjUsername) {
        return multiOjService.getMultiOjInfo(username, multiOj, multiOjUsername);
    }

}