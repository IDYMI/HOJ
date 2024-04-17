package top.hcode.hoj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import top.hcode.hoj.pojo.entity.school.ContestSeat;

@Mapper
@Repository
public interface ContestSeatMapper extends BaseMapper<ContestSeat> {

}
