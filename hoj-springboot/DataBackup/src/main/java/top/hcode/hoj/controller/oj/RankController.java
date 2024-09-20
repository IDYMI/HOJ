package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.annotation.AnonApi;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.user.UserCloc;
import top.hcode.hoj.service.oj.RankService;

import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 20:53
 * @Description: 处理排行榜数据
 */
@RestController
@RequestMapping("/api")
@AnonApi
public class RankController {

    @Autowired
    private RankService rankService;

    /**
     * @MethodName get-rank-list
     * @Params * @param null
     * @Description 获取排行榜数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-rank-list")
    public CommonResult<IPage> getRankList(@RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "searchUser", required = false) String searchUser,
            @RequestParam(value = "type", required = true) Integer type) {
        return rankService.getRankList(limit, currentPage, searchUser, type);
    }

    /**
     * @MethodName get-user-code
     * @Description 获取用户指定日期间提交记录
     * @Return CommonResult
     */
    @PostMapping("/get-user-code-record")
    public CommonResult<List<UserCloc>> getUserCodeRecord(@RequestBody Map<String, Object> params) {
        return rankService.getUserCodeRecord((List<String>) params.get("uidList"), (String) params.get("startTime"),
                (String) params.get("endTime"));
    }
}