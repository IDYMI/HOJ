package top.hcode.hoj.service.admin.multi.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.admin.multiOj.MultiOjInfoManager;
import top.hcode.hoj.pojo.dto.MultiOjDto;
import top.hcode.hoj.service.admin.multi.MultiOjService;

@Service
public class MultiOjServiceImpl implements MultiOjService {

    @Autowired
    private MultiOjInfoManager multiOjInfoManager;

    @Override
    public CommonResult<MultiOjDto> getMultiOjInfo(String username, String multiOj, String multiOjUsername) {
        try {
            MultiOjDto multiOjDto = multiOjInfoManager.getMultiOjProblemInfo(username, multiOj, multiOjUsername);
            return CommonResult.successResponse(multiOjDto);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (Exception e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }

}