package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.HonorVO;

public interface HonorService {

	public CommonResult<IPage<HonorVO>> getHonorList(Integer limit, Integer currentPage,
			String keyword);

}