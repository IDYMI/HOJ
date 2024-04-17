package top.hcode.hoj.controller.oj;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;

import top.hcode.hoj.annotation.AnonApi;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ExaminationSeatDTO;
import top.hcode.hoj.pojo.vo.ExaminationRoomVO;
import top.hcode.hoj.pojo.vo.SchoolVO;
import top.hcode.hoj.service.oj.ExaminationService;

import java.util.List;

/**
 *
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class ExaminationController {

    @Autowired
    private ExaminationService examinationService;

    @GetMapping("/get-school-list")
    @AnonApi
    public CommonResult<List<SchoolVO>> getSchoolList() {
        return examinationService.getSchoolList();
    }

    @GetMapping("/get-examination-room-list")
    @RequiresAuthentication
    public CommonResult<IPage<ExaminationRoomVO>> getExaminationRoomList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "eid", required = false) Long eid,
            @RequestParam(value = "cid", required = false) Long cid) {
        return examinationService.getExaminationRoomList(limit, currentPage, keyword, eid, cid);
    }

    @GetMapping("/examination-room")
    @RequiresAuthentication
    public CommonResult<ExaminationRoomVO> getExaminationRoom(
            @RequestParam(value = "eid", required = true) Long eid) {
        return examinationService.getExaminationRoom(eid);
    }

    @PostMapping("/examination-room")
    @RequiresAuthentication
    public CommonResult<Void> addExaminationRoom(@RequestBody ExaminationRoomVO examinationRoomVo) {
        return examinationService.addExaminationRoom(examinationRoomVo);
    }

    @PutMapping("/examination-room")
    @RequiresAuthentication
    public CommonResult<Void> updateExaminationRoom(@RequestBody ExaminationRoomVO examinationRoomVo) {
        return examinationService.updateExaminationRoom(examinationRoomVo);
    }

    @PostMapping("/examination-seat")
    @RequiresAuthentication
    public CommonResult<Void> adminExaminationSeat(@RequestBody ExaminationSeatDTO examinationSeatDTo) {
        return examinationService.adminExaminationSeat(examinationSeatDTo);
    }

    @GetMapping("/get-contest-examination-room-list")
    @AnonApi
    public CommonResult<IPage<ExaminationRoomVO>> getExaminationSeatList(
            @RequestParam(value = "cid", required = true) Long cid,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = true) String keyword) {
        return examinationService.getExaminationSeatList(cid, limit, currentPage, keyword);
    }

    @GetMapping("/examination-seat")
    @AnonApi
    public CommonResult<ExaminationRoomVO> getExaminationSeat(
            @RequestParam(value = "eid", required = false) Long eid,
            @RequestParam(value = "cid", required = false) Long cid) {
        return examinationService.getExaminationSeat(eid, cid);
    }

}
