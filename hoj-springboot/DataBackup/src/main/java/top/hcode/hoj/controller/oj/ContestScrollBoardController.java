package top.hcode.hoj.controller.oj;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.annotation.AnonApi;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.ContestResolverOnlineVO;
import top.hcode.hoj.pojo.vo.ContestScrollBoardInfoVO;
import top.hcode.hoj.pojo.vo.ContestScrollBoardSubmissionVO;
import top.hcode.hoj.service.oj.ContestScrollBoardService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/3
 */
@RestController
@RequestMapping("/api")
@AnonApi
public class ContestScrollBoardController {

    @Resource
    private ContestScrollBoardService contestScrollBoardService;

    @GetMapping("/get-contest-scroll-board-info")
    public CommonResult<ContestScrollBoardInfoVO> getContestScrollBoardInfo(@RequestParam(value = "cid") Long cid) {
        return contestScrollBoardService.getContestScrollBoardInfo(cid);
    }

    @GetMapping("/get-contest-scroll-board-submission")
    public CommonResult<List<ContestScrollBoardSubmissionVO>> getContestScrollBoardSubmission(
            @RequestParam(value = "cid", required = true) Long cid,
            @RequestParam(value = "removeStar", defaultValue = "false") Boolean removeStar) {
        return contestScrollBoardService.getContestScrollBoardSubmission(cid, removeStar);
    }

    @GetMapping("/get-contest-resolver-online-info")
    public CommonResult<ContestResolverOnlineVO> getContestResolverOnlineInfo(
            @RequestParam(value = "cid", required = true) Long cid,
            @RequestParam(value = "removeStar", defaultValue = "false") Boolean removeStar) {
        return contestScrollBoardService.getContestResolverOnlineInfo(cid, removeStar);
    }

}
