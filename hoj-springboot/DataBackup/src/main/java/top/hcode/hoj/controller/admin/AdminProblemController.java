package top.hcode.hoj.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.dto.ProblemResDTO;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.service.admin.problem.AdminProblemService;

import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/11 21:45
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/problem")
@RequiresRoles(value = { "root", "admin", "problem_admin" }, logical = Logical.OR)
public class AdminProblemController {

    @Autowired
    private AdminProblemService adminProblemService;

    @GetMapping("/get-problem-list")
    @RequiresAuthentication
    public CommonResult<IPage<ProblemResDTO>> getProblemList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "auth", required = false) Integer auth,
            @RequestParam(value = "oj", required = false) String oj,
            @RequestParam(value = "difficulty", required = false) Integer difficulty,
            @RequestParam(value = "type", required = false) Integer type) {
        return adminProblemService.getProblemList(limit, currentPage, keyword, auth, oj, difficulty, type);
    }

    @GetMapping("")
    @RequiresAuthentication
    public CommonResult<ProblemResDTO> getProblem(@RequestParam("pid") Long pid,
            @RequestParam(value = "peid", required = false) Long peid) {
        return adminProblemService.getProblem(pid, peid);
    }

    @DeleteMapping("")
    @RequiresAuthentication
    public CommonResult<Void> deleteProblem(@RequestParam("pid") Long pid) {
        return adminProblemService.deleteProblem(pid);
    }

    @PostMapping("")
    @RequiresAuthentication
    public CommonResult<Void> addProblem(@RequestBody ProblemDTO problemDto) {
        return adminProblemService.addProblem(problemDto);
    }

    @PutMapping("")
    @RequiresAuthentication
    public CommonResult<Void> updateProblem(@RequestBody ProblemDTO problemDto) {
        return adminProblemService.updateProblem(problemDto);
    }

    @GetMapping("/get-problem-cases")
    @RequiresAuthentication
    public CommonResult<List<ProblemCase>> getProblemCases(
            @RequestParam("pid") Long pid, @RequestParam(value = "peid", required = false) Long peid,
            @RequestParam(value = "isUpload", defaultValue = "true") Boolean isUpload) {
        return adminProblemService.getProblemCases(pid, peid, isUpload);
    }

    @PostMapping("/compile-spj")
    @RequiresAuthentication
    public CommonResult compileSpj(@RequestBody CompileDTO compileDTO) {
        return adminProblemService.compileSpj(compileDTO);
    }

    @PostMapping("/compile-interactive")
    @RequiresAuthentication
    public CommonResult compileInteractive(@RequestBody CompileDTO compileDTO) {
        return adminProblemService.compileInteractive(compileDTO);
    }

    @GetMapping("/import-remote-oj-problem")
    @RequiresAuthentication
    public CommonResult<Void> importRemoteOJProblem(@RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "problemId", required = true) String problemId,
            @RequestParam(value = "gid", required = false) Long gid) {
        return adminProblemService.importRemoteOJProblem(name, problemId, gid);
    }

    @PutMapping("/change-problem-auth")
    @RequiresAuthentication
    public CommonResult<Void> changeProblemAuth(@RequestBody ProblemResDTO problem) {
        return adminProblemService.changeProblemAuth(problem);
    }

}