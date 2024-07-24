package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.honor.Honor;
import top.hcode.hoj.service.admin.honor.AdminHonorService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/admin/honor")
@RequiresRoles(value = { "root", "admin", "problem_admin" }, logical = Logical.OR)
public class AdminHonorController {

    @Resource
    private AdminHonorService adminHonorService;

    @GetMapping("/get-honor-list")
    @RequiresAuthentication
    public CommonResult<IPage<Honor>> getHonorList(@RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "year", required = false) String year) {
        return adminHonorService.getHonorList(limit, currentPage, keyword, type, year);
    }

    @GetMapping("")
    @RequiresAuthentication
    public CommonResult<Honor> getHonor(@RequestParam("hid") Long hid) {
        return adminHonorService.getHonor(hid);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    public CommonResult<Void> deleteHonor(@RequestParam("hid") Long hid) {
        return adminHonorService.deleteHonor(hid);
    }

    @PostMapping("")
    @RequiresAuthentication
    public CommonResult<Void> addHonor(@RequestBody Honor honor) {
        return adminHonorService.addHonor(honor);
    }

    @PutMapping("")
    @RequiresAuthentication
    public CommonResult<Void> updateHonor(@RequestBody Honor honor) {
        return adminHonorService.updateHonor(honor);
    }

    @PutMapping("/change-honor-status")
    @RequiresAuthentication
    public CommonResult<Void> changeHonorStatus(@RequestParam(value = "hid", required = true) Long hid,
            @RequestParam(value = "author", required = true) String author,
            @RequestParam(value = "status", required = true) Boolean status) {
        return adminHonorService.changeHonorStatus(hid, author, status);
    }

}