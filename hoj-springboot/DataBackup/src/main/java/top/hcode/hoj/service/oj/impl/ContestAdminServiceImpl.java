package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.ContestAdminManager;
import top.hcode.hoj.pojo.dto.CheckACDTO;
import top.hcode.hoj.pojo.entity.contest.ContestPrint;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.contest.ContestSign;
import top.hcode.hoj.pojo.vo.ContestSignVO;
import top.hcode.hoj.pojo.vo.SessionVO;
import top.hcode.hoj.service.oj.ContestAdminService;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 19:54
 * @Description:
 */

@Service
public class ContestAdminServiceImpl implements ContestAdminService {

    @Resource
    private ContestAdminManager contestAdminManager;

    @Override
    public CommonResult<IPage<ContestRecord>> getContestACInfo(Long cid, Integer currentPage, Integer limit) {
        try {
            return CommonResult.successResponse(contestAdminManager.getContestACInfo(cid, currentPage, limit));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> checkContestACInfo(CheckACDTO checkACDto) {
        try {
            contestAdminManager.checkContestACInfo(checkACDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<IPage<ContestPrint>> getContestPrint(Long cid, Integer currentPage, Integer limit) {
        try {
            return CommonResult.successResponse(contestAdminManager.getContestPrint(cid, currentPage, limit));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> checkContestPrintStatus(Long id, Long cid) {
        try {
            contestAdminManager.checkContestPrintStatus(id, cid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<IPage<ContestSign>> getContestSign(Long cid, Integer currentPage, Integer limit,
            Boolean type, Boolean gender, Integer status, String keyword) {
        try {
            return CommonResult
                    .successResponse(
                            contestAdminManager.getContestSign(cid, currentPage, limit, type, gender, status, keyword));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ContestSignVO> getContestSignInfo(Long cid, Long id) {
        try {
            return CommonResult.successResponse(contestAdminManager.getContestSignInfo(cid, id));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> checkContestSignStatus(Map<String, Object> params) {
        try {
            contestAdminManager.checkContestSignStatus(params);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> updateContestSign(ContestSignVO contestSign) {
        try {
            contestAdminManager.updateContestSign(contestSign);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<IPage<SessionVO>> getContestSession(Long cid, Integer currentPage, Integer limit,
            String keyword, String unkeyword) {
        try {
            return CommonResult.successResponse(
                    contestAdminManager.getContestSession(cid, currentPage, limit, keyword, unkeyword));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<List<SessionVO>> getContestIpList(Long cid) {
        try {
            return CommonResult.successResponse(contestAdminManager.getContestIpList(cid));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> rejudgeContestIp(Long cid, String uid) {
        try {
            contestAdminManager.rejudgeContestIp(cid, uid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}