package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.oj.HonorManager;
import top.hcode.hoj.pojo.vo.HonorVO;
import top.hcode.hoj.service.oj.HonorService;

import javax.annotation.Resource;

@Service
public class HonorServiceImpl implements HonorService {

    @Resource
    private HonorManager honorManager;

    @Override
    public CommonResult<IPage<HonorVO>> getHonorList(Integer limit, Integer currentPage, String keyword) {
        return CommonResult
                .successResponse(honorManager.getHonorList(limit, currentPage, keyword));
    }

}