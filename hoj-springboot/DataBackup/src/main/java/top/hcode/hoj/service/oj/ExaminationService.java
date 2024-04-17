package top.hcode.hoj.service.oj;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ExaminationSeatDTO;
import top.hcode.hoj.pojo.vo.ExaminationRoomVO;
import top.hcode.hoj.pojo.vo.SchoolVO;

public interface ExaminationService {

        public CommonResult<List<SchoolVO>> getSchoolList();

        public CommonResult<IPage<ExaminationRoomVO>> getExaminationRoomList(Integer limit, Integer currentPage,
                        String keyword, Long eid, Long cid);

        public CommonResult<ExaminationRoomVO> getExaminationRoom(Long eid);

        public CommonResult<Void> addExaminationRoom(ExaminationRoomVO examinationRoomVo);

        public CommonResult<Void> updateExaminationRoom(ExaminationRoomVO examinationRoomVo);

        public CommonResult<Void> adminExaminationSeat(ExaminationSeatDTO examinationSeatDTo);

        public CommonResult<IPage<ExaminationRoomVO>> getExaminationSeatList(Long cid, Integer limit,
                        Integer currentPage, String keyword);

        public CommonResult<ExaminationRoomVO> getExaminationSeat(Long eid, Long cid);

}
