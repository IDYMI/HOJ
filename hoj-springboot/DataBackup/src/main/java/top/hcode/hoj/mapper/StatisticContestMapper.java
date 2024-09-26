package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.contest.StatisticContest;

@Mapper
@Repository
public interface StatisticContestMapper extends BaseMapper<StatisticContest> {

    IPage<StatisticContest> getAdminStatisticContestList(IPage page, @Param("keyword") String keyword);

    IPage<StatisticContest> getStatisticContestList(IPage page, @Param("keyword") String keyword);
}
