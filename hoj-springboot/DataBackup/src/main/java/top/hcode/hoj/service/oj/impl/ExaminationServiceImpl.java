package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.oj.ExaminationManager;
import top.hcode.hoj.pojo.dto.ExaminationSeatDTO;
import top.hcode.hoj.pojo.vo.ExaminationRoomVO;
import top.hcode.hoj.pojo.vo.SchoolVO;
import top.hcode.hoj.service.oj.ExaminationService;

import java.util.List;

import javax.annotation.Resource;

/**
 *
 * @Description:
 */
@Service
public class ExaminationServiceImpl implements ExaminationService {

    @Resource
    private ExaminationManager examinationManager;

    @Override
    public CommonResult<List<SchoolVO>> getSchoolList() {
        return CommonResult.successResponse(examinationManager.getSchoolList());
    }

    @Override
    public CommonResult<IPage<ExaminationRoomVO>> getExaminationRoomList(Integer limit, Integer currentPage,
            String keyword, Long eid, Long cid) {
        try {
            return CommonResult
                    .successResponse(examinationManager.getExaminationRoomList(limit, currentPage, keyword, eid, cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<ExaminationRoomVO> getExaminationRoom(Long eid) {
        try {
            return CommonResult.successResponse(examinationManager.getExaminationRoom(eid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> addExaminationRoom(ExaminationRoomVO examinationRoomVo) {
        try {
            examinationManager.addExaminationRoom(examinationRoomVo);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateExaminationRoom(ExaminationRoomVO examinationRoomVo) {
        try {
            examinationManager.updateExaminationRoom(examinationRoomVo);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> adminExaminationSeat(ExaminationSeatDTO examinationSeatDTo) {
        try {
            examinationManager.adminExaminationSeat(examinationSeatDTo);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<ExaminationRoomVO> getExaminationSeat(Long eid, Long cid) {
        try {
            return CommonResult.successResponse(examinationManager.getExaminationSeat(eid, cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<IPage<ExaminationRoomVO>> getExaminationSeatList(Long cid, Integer limit,
            Integer currentPage, String keyword) {
        try {
            return CommonResult.successResponse(
                    examinationManager.getExaminationSeatList(cid, limit, currentPage, keyword));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

}