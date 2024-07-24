package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import top.hcode.hoj.pojo.entity.honor.Honor;

import java.util.List;

@Mapper
@Repository
public interface HonorMapper extends BaseMapper<Honor> {

	List<Honor> getAdminHonorList(IPage page,
			@Param("keyword") String keyword,
			@Param("type") String type,
			@Param("year") String year);
}