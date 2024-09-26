package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.vo.ProblemVO;
import top.hcode.hoj.pojo.dto.ProblemResDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface ProblemMapper extends BaseMapper<Problem> {
    List<ProblemVO> getProblemList(IPage page,
            @Param("pid") Long pid,
            @Param("keyword") String keyword,
            @Param("difficulty") Integer difficulty,
            @Param("type") Integer type,
            @Param("tid") List<Long> tid,
            @Param("tagListSize") Integer tagListSize,
            @Param("oj") String oj);

    List<ProblemResDTO> getAdminProblemList(
            @Param("keyword") String keyword,
            @Param("auth") Integer auth,
            @Param("oj") String oj,
            @Param("difficulty") Integer difficulty,
            @Param("type") Integer type,
            @Param("isRemote") Boolean isRemote);

    List<ProblemResDTO> getAdminGroupProblemList(
            @Param("keyword") String keyword,
            @Param("gid") Long gid);

    List<ProblemResDTO> getAdminContestProblemList(
            @Param("keyword") String keyword,
            @Param("cid") Long cid,
            @Param("problemType") Integer problemType,
            @Param("oj") String oj,
            @Param("difficulty") Integer difficulty,
            @Param("type") Integer type,
            @Param("gid") Long gid,
            @Param("isRemote") Boolean isRemote,
            @Param("contestGid") Long contestGid,
            @Param("pidList") List<Long> pidList);

    List<ProblemResDTO> getAdminTrainingProblemList(
            @Param("keyword") String keyword,
            @Param("queryExisted") Boolean queryExisted,
            @Param("tid") Long tid,
            @Param("pidList") List<Long> pidList);

    List<ProblemResDTO> getRecentUpdatedProblemList();

}
