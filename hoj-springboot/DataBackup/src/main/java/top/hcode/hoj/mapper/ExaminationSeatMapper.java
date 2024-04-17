package top.hcode.hoj.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import top.hcode.hoj.pojo.entity.school.ExaminationSeat;
import top.hcode.hoj.pojo.vo.ExaminationSeatVO;

@Mapper
@Repository
public interface ExaminationSeatMapper extends BaseMapper<ExaminationSeat> {

    List<ExaminationSeatVO> getSeatList(@Param("cid") Long cid, @Param("eidList") List<Long> eidList);

    List<ExaminationSeatVO> getContestSeatList(@Param("eid") Long eid, @Param("cid") Long cid);

}
