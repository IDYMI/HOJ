package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.ResultStatus;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.oj.RankManager;
import top.hcode.hoj.pojo.entity.user.UserCloc;
import top.hcode.hoj.service.oj.RankService;

import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 20:56
 * @Description:
 */
@Service
public class RankServiceImpl implements RankService {

    @Resource
    private RankManager rankManager;

    @Override
    public CommonResult<IPage> getRankList(Integer limit, Integer currentPage, String searchUser, Integer type) {
        try {
            return CommonResult.successResponse(rankManager.getRankList(limit, currentPage, searchUser, type));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<List<UserCloc>> getUserCodeRecord(List<String> uidList, String startTime, String endTime) {
        try {
            return CommonResult.successResponse(rankManager.getUserCodeRecord(uidList, startTime, endTime));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (IOException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}