package top.hcode.hoj.manager.admin.problem;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.manager.oj.ProblemManager;
import top.hcode.hoj.pojo.dto.ChangeGroupProblemProgressDTO;
import top.hcode.hoj.pojo.dto.ProblemResDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;

import javax.annotation.Resource;

/**
 * @Author Himit_ZH
 * @Date 2022/4/13
 */
@Component
public class AdminGroupProblemManager {

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private ProblemManager problemManager;

    public IPage<ProblemResDTO> list(Integer currentPage, Integer limit, String keyword, Long gid) {
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;
        IPage<ProblemResDTO> iPage = new Page<>(currentPage, limit);
        return problemEntityService.getAdminGroupProblemList(iPage, keyword, gid);
    }

    public void changeProgress(ChangeGroupProblemProgressDTO changeGroupProblemProgressDto) throws StatusFailException {
        Long pid = changeGroupProblemProgressDto.getPid();
        String problem_id = changeGroupProblemProgressDto.getProblemId();
        Integer progress = changeGroupProblemProgressDto.getProgress();
        if (pid == null || progress == null) {
            throw new StatusFailException("请求参数pid或者progress不能为空！");
        }
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.select("id", "problem_id", "is_group", "gid", "apply_public_progress")
                .eq("id", pid);
        Problem problem = problemEntityService.getOne(problemQueryWrapper);
        if (problem == null) {
            throw new StatusFailException("错误：当前题目已不存在！");
        }

        // 更改 problem 的 problem_id
        String lastProblemId = problemManager.getProblemLastId(null).getProblemLastId();

        problem.setApplyPublicProgress(progress);
        switch (progress) {
            case 1:
            case 3:
                problem.setIsGroup(true);
                break;
            case 2:
                // 加入公开题库
                problem.setIsGroup(false);
                if (!problem.getIsRemote()) {
                    problem.setProblemId(lastProblemId);
                } else {
                    QueryWrapper<Problem> problemQueryWrapper2 = new QueryWrapper<>();
                    problemQueryWrapper2.select("id", "problem_id")
                            .eq("problem_id", problem_id);
                    Problem problem2 = problemEntityService.getOne(problemQueryWrapper2);

                    if (problem2 != null) {
                        throw new StatusFailException("修改失败，公共题库中已有该远程题目！");
                    }

                    problem.setProblemId(problem_id);
                }
                break;
            default:
                throw new StatusFailException("请求参数错误：progress请使用1~3");
        }
        boolean isOk = problemEntityService.updateById(problem);
        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }
    }
}
