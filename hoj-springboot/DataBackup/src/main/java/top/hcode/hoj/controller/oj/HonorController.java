package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.annotation.AnonApi;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.oj.HonorService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class HonorController {

    @Resource
    private HonorService honorService;

    /**
     * @param limit
     * @param currentPage
     * @param keyword
     * @MethodName getHonorList
     * @Description 获取荣誉列表，可根据关键词过滤
     * @Return
     */
    @GetMapping("/get-honor-list")
    @AnonApi
    public CommonResult<IPage<HonorVO>> getHonorList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "keyword", required = false) String keyword) {

        return honorService.getHonorList(limit, currentPage, keyword);
    }

}