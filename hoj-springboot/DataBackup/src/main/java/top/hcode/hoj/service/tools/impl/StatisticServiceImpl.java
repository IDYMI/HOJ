package top.hcode.hoj.service.tools.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.tools.StatisticManager;
import top.hcode.hoj.pojo.dto.StatisticDTO;
import top.hcode.hoj.pojo.entity.contest.StatisticContest;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.tools.StatisticService;

import javax.annotation.Resource;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Resource
    private StatisticManager statisticManager;

    @Override
    public CommonResult<IPage<StatisticContest>> getStatisticList(Integer currentPage, Integer limit,
            String keyword) {
        try {
            return CommonResult
                    .successResponse(statisticManager.getStatisticList(currentPage, limit, keyword));
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<IPage<ACMContestRankVO>> getStatisticRank(Integer currentPage, Integer limit, String cids,
            String keyword) {
        try {
            return CommonResult
                    .successResponse(statisticManager.getStatisticRank(currentPage, limit, cids, keyword));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<String> getStatisticRankCids(String scid) {
        try {
            return CommonResult.successResponse(statisticManager.getStatisticRankCids(scid));
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<IPage<StatisticContest>> getAdminStatisticList(Integer currentPage, Integer limit,
            String keyword) {
        try {
            return CommonResult
                    .successResponse(statisticManager.getAdminStatisticList(currentPage, limit, keyword));
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<IPage<ACMContestRankVO>> addStatisticRank(StatisticDTO statisticDto) {
        try {
            return CommonResult.successResponse(statisticManager.addStatisticRank(statisticDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> deleteStatisticRank(String scid) {
        try {
            statisticManager.deleteStatisticRank(scid);
            return CommonResult.successResponse();
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> changeStatisticVisible(String scid, Boolean show) {
        try {
            statisticManager.changeStatisticVisible(scid, show);
            return CommonResult.successResponse();
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

}