package top.hcode.hoj.service.admin.honor.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.admin.honor.AdminHonorManager;
import top.hcode.hoj.pojo.entity.honor.Honor;
import top.hcode.hoj.service.admin.honor.AdminHonorService;

@Service
public class AdminHonorServiceImpl implements AdminHonorService {

    @Autowired
    private AdminHonorManager adminHonorManager;

    @Override
    public CommonResult<IPage<Honor>> getHonorList(Integer limit, Integer currentPage, String keyword, String type,
            String year) {
        return CommonResult.successResponse(adminHonorManager.getHonorList(limit, currentPage, keyword, type, year));
    }

    @Override
    public CommonResult<Honor> getHonor(Long hid) {
        try {
            Honor training = adminHonorManager.getHonor(hid);
            return CommonResult.successResponse(training);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteHonor(Long hid) {
        try {
            adminHonorManager.deleteHonor(hid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> addHonor(Honor honor) {
        try {
            adminHonorManager.addHonor(honor);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> updateHonor(Honor honor) {
        try {
            adminHonorManager.updateHonor(honor);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> changeHonorStatus(Long hid, String author, Boolean status) {
        try {
            adminHonorManager.changeHonorStatus(hid, author, status);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}