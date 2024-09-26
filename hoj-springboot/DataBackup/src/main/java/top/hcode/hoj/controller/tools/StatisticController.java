package top.hcode.hoj.controller.tools;

import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.annotation.AnonApi;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.StatisticDTO;
import top.hcode.hoj.pojo.entity.contest.StatisticContest;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.tools.StatisticService;

@RestController
@RequestMapping("/api")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    // 开放
    @GetMapping("/get-statistic-list")
    @AnonApi
    public CommonResult<IPage<StatisticContest>> getStatisticList(
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "keyword", required = false) String keyword) {

        return statisticService.getStatisticList(currentPage, limit, keyword);
    }

    @GetMapping("/get-statistic-rank")
    @AnonApi
    public CommonResult<IPage<ACMContestRankVO>> getStatisticRank(
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "cids", required = true) String cids,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return statisticService.getStatisticRank(currentPage, limit, cids, keyword);
    }

    @GetMapping("/get-statistic-rank-cids")
    @AnonApi
    public CommonResult<String> getStatisticRankCids(
            @RequestParam(value = "scid", required = true) String scid) {
        return statisticService.getStatisticRankCids(scid);
    }

    // 后台

    @GetMapping("/admin/get-statistic-list")
    @RequiresAuthentication
    @RequiresRoles(value = { "root", "admin", "problem_admin" }, logical = Logical.OR)
    public CommonResult<IPage<StatisticContest>> getAdminStatisticList(
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "keyword", required = false) String keyword) {

        return statisticService.getAdminStatisticList(currentPage, limit, keyword);
    }

    /**
     * @Description 生成系列比赛
     * @Return
     * @Since
     */
    @PostMapping("admin/statistic-rank")
    @RequiresAuthentication
    @RequiresRoles(value = { "root", "admin", "problem_admin" }, logical = Logical.OR)
    public CommonResult<IPage<ACMContestRankVO>> addStatisticRank(
            @RequestBody StatisticDTO statisticDto) {

        return statisticService.addStatisticRank(statisticDto);
    }

    /**
     * @Description 删除对应的系列比赛
     * @Return
     * @Since
     */
    @DeleteMapping("/admin/statistic-rank")
    @RequiresAuthentication
    @RequiresRoles(value = { "root", "admin", "problem_admin" }, logical = Logical.OR)
    public CommonResult<Void> deleteStatisticRank(
            @RequestParam(value = "scid", required = true) String scid) {
        return statisticService.deleteStatisticRank(scid);
    }

    /**
     * @Description 更新是否显示
     * @Return
     * @Since
     */
    @PutMapping("/admin/change-statistic-visible")
    @RequiresAuthentication
    @RequiresRoles(value = { "root", "admin", "problem_admin" }, logical = Logical.OR)
    public CommonResult<Void> changeStatisticVisible(
            @RequestParam(value = "scid", required = true) String scid,
            @RequestParam(value = "show", required = true) Boolean show) {

        return statisticService.changeStatisticVisible(scid, show);
    }
}