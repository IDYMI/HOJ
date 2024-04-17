package top.hcode.hoj.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import top.hcode.hoj.pojo.entity.school.ExaminationRoom;
import top.hcode.hoj.pojo.vo.ExaminationRoomVO;

@Mapper
@Repository
public interface ExaminationRoomMapper extends BaseMapper<ExaminationRoom> {

	List<ExaminationRoomVO> getExaminationRoomList(
			@Param("eid") Long eid,
			@Param("keyword") String keyword);

	Integer getExaminationUsed(
			@Param("cid") Long cid,
			@Param("eid") Long eid);

	List<ExaminationRoomVO> getEidList(
			@Param("keyword") String keyword);

	List<ExaminationRoomVO> getContestEidList(
			@Param("cid") Long cid,
			@Param("keyword") String keyword);
}
