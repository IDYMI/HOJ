package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.user.Session;
import top.hcode.hoj.pojo.vo.SessionVO;

@Mapper
@Repository
public interface SessionMapper extends BaseMapper<Session> {

    IPage<SessionVO> getContestSessionList(IPage<SessionVO> iPage,
            @Param("cid") Long cid,
            @Param("keyword") String keyword,
            @Param("unkeyword") String unkeyword);
}
