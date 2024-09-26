package top.hcode.hoj.manager.admin.contest;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestProblemEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.manager.admin.problem.RemoteProblemManager;
import top.hcode.hoj.manager.group.GroupManager;
import top.hcode.hoj.pojo.dto.ContestProblemDTO;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.dto.ProblemResDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemDescription;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.HtmlToPdfUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 11:20
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class AdminContestProblemManager {

    @Autowired
    private ContestProblemEntityService contestProblemEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private RemoteProblemManager remoteProblemManager;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private GroupManager groupManager;

    @Autowired
    private HtmlToPdfUtils htmlToPdfUtils;

    public HashMap<String, Object> getProblemList(Integer limit, Integer currentPage, String keyword,
            Long cid, Integer problemType, String oj, Integer difficulty, Integer type, Long gid)
            throws StatusForbiddenException {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = groupManager.getGroupAuthAdmin(gid);

        // 获取本场比赛的信息
        Contest contest = contestEntityService.getById(cid);
        // 只有超级管理员和题目管理员、题目创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(contest.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限查看题目！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;
        IPage<ProblemResDTO> iPage = new Page<>(currentPage, limit);
        // 根据cid在ContestProblem表中查询到对应pid集合
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid);
        List<Long> pidList = new LinkedList<>();

        List<ContestProblem> contestProblemList = contestProblemEntityService.list(contestProblemQueryWrapper);
        HashMap<Long, ContestProblem> contestProblemMap = new HashMap<>();
        contestProblemList.forEach(contestProblem -> {
            contestProblemMap.put(contestProblem.getPid(), contestProblem);
            pidList.add(contestProblem.getPid());
        });

        HashMap<String, Object> contestProblemHashMap = new HashMap<>();

        Boolean isRemote = Constants.RemoteOJ.isRemoteOJ(oj);

        Long contestGid = contest.getGid();
        IPage<ProblemResDTO> problemListPage = problemEntityService.getAdminContestProblemList(iPage, keyword, cid,
                problemType, oj, difficulty, type, gid, isRemote, contestGid, pidList);

        if (pidList.size() > 0 && problemType == null) {
            List<ProblemResDTO> problemList = problemListPage.getRecords();

            problemList.forEach(problemResDto -> {
                ContestProblem contestProblem = contestProblemMap.get(problemResDto.getId());
                List<ProblemDescription> problemDescriptionList = problemResDto.getProblemDescriptionList();

                // 获取对应的 peid 或第一个 problemDescription 的 title 和 id
                problemDescriptionList.stream()
                        .filter(pd -> contestProblem.getPeid() == null || pd.getId().equals(contestProblem.getPeid()))
                        .findFirst()
                        .ifPresent(problemDescription -> {
                            problemResDto.setTitle(problemDescription.getTitle())
                                    .setPeid(problemDescription.getId());
                        });
            });

            List<ProblemResDTO> sortedProblemList = problemList.stream()
                    .sorted(Comparator.comparing(ProblemResDTO::getId, (a, b) -> {
                        ContestProblem x = contestProblemMap.get(a);
                        ContestProblem y = contestProblemMap.get(b);
                        if (x == null && y != null) {
                            return 1;
                        } else if (x != null && y == null) {
                            return -1;
                        } else if (x == null) {
                            return -1;
                        } else {
                            return x.getDisplayId().compareTo(y.getDisplayId());
                        }
                    })).collect(Collectors.toList());
            problemListPage.setRecords(sortedProblemList);
        }

        contestProblemHashMap.put("problemList", problemListPage);
        contestProblemHashMap.put("contestProblemMap", contestProblemMap);

        return contestProblemHashMap;
    }

    public ProblemResDTO getProblem(Long pid, Long peid) throws StatusFailException, StatusForbiddenException {

        ProblemResDTO problem = problemEntityService.getProblemResDTO(pid, peid, null, null);

        if (problem != null) { // 查询成功
            // 获取当前登录的用户
            AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

            boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                    || SecurityUtils.getSubject().hasRole("admin");
            // 只有超级管理员和题目管理员、题目创建者才能操作
            if (!isRoot && !userRolesVo.getUsername().equals(problem.getAuthor())) {
                throw new StatusForbiddenException("对不起，你无权限查看题目！");
            }

            return problem;
        } else {
            throw new StatusFailException("查询失败！");
        }
    }

    public void deleteProblem(Long pid, Long cid) throws StatusForbiddenException {
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        // 获取本场比赛的信息
        Contest contest = contestEntityService.getById(cid);
        // 只有超级管理员和题目管理员、题目创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(contest.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限删除题目！");
        }

        // 比赛id不为null，表示就是从比赛列表移除而已
        if (cid != null) {
            QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
            contestProblemQueryWrapper.eq("cid", cid).eq("pid", pid);
            contestProblemEntityService.remove(contestProblemQueryWrapper);
            // 把该题目在比赛的提交全部删掉
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.eq("cid", cid).eq("pid", pid);
            judgeEntityService.remove(judgeUpdateWrapper);

            log.info("[{}],[{}],cid:[{}],pid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                    "Admin_Contest", "Remove_Problem", cid, pid, userRolesVo.getUid(), userRolesVo.getUsername());
        } else {
            /*
             * problem的id为其他表的外键的表中的对应数据都会被一起删除！
             */
            problemEntityService.removeById(pid);
            FileUtil.del(new File(Constants.File.TESTCASE_BASE_FOLDER.getPath() + File.separator + "problem_" + pid));

            log.info("[{}],[{}],cid:[{}],pid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                    "Admin_Contest", "Delete_Problem", cid, pid, userRolesVo.getUid(), userRolesVo.getUsername());
        }
    }

    public Map<Object, Object> addProblem(ProblemDTO problemDto) throws StatusFailException {

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());

        Long gid = problemDto.getProblem().getGid();
        if (gid == null) {
            queryWrapper.isNull("gid");
        } else {
            queryWrapper.eq("gid", gid);
        }
        Problem problem = problemEntityService.getOne(queryWrapper);
        if (problem != null) {
            throw new StatusFailException("该题目的Problem ID已存在，请更换！");
        }
        // 设置为比赛题目
        problemDto.getProblem().setAuth(3);
        boolean isOk = problemEntityService.adminAddProblem(problemDto);
        if (isOk) { // 添加成功
            // 顺便返回新的题目id，好下一步添加外键操作
            return MapUtil.builder().put("pid", problemDto.getProblem().getId()).map();
        } else {
            throw new StatusFailException("添加失败");
        }
    }

    public void updateProblem(ProblemDTO problemDto) throws StatusForbiddenException, StatusFailException {
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");
        // 只有超级管理员和题目管理员、题目创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(problemDto.getProblem().getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限修改题目！");
        }

        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemDto.getProblem().getProblemId().toUpperCase());
        Long gid = problemDto.getProblem().getGid();
        if (gid == null) {
            queryWrapper.isNull("gid");
        } else {
            queryWrapper.eq("gid", gid);
        }
        Problem problem = problemEntityService.getOne(queryWrapper);

        // 如果problem_id不是原来的且已存在该problem_id，则修改失败！
        if (problem != null && problem.getId().longValue() != problemDto.getProblem().getId()) {
            throw new StatusFailException("当前的Problem ID 已被使用，请重新更换新的！");
        }

        // 记录修改题目的用户
        problemDto.getProblem().setModifiedUser(userRolesVo.getUsername());

        boolean isOk = problemEntityService.adminUpdateProblem(problemDto);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public ContestProblem getContestProblem(Long cid, Long pid) throws StatusFailException {
        QueryWrapper<ContestProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", pid);
        ContestProblem contestProblem = contestProblemEntityService.getOne(queryWrapper);
        if (contestProblem == null) {
            throw new StatusFailException("查询失败");
        }
        return contestProblem;
    }

    public ContestProblem setContestProblem(ContestProblem contestProblem) throws StatusFailException {
        boolean isOk = contestProblemEntityService.saveOrUpdate(contestProblem);
        if (isOk) {
            contestProblemEntityService.syncContestRecord(contestProblem.getPid(), contestProblem.getCid(),
                    contestProblem.getDisplayId());
            // 获取当前登录的用户
            AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
            log.info("[{}],[{}],cid:[{}],ContestProblem:[{}],operatorUid:[{}],operatorUsername:[{}]",
                    "Admin_Contest", "Update_Problem", contestProblem.getCid(), contestProblem, userRolesVo.getUid(),
                    userRolesVo.getUsername());
            return contestProblem;
        } else {
            throw new StatusFailException("更新失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addProblemFromPublic(ContestProblemDTO contestProblemDto) throws StatusFailException {

        Long pid = contestProblemDto.getPid();
        Long peid = contestProblemDto.getPeid();
        Long cid = contestProblemDto.getCid();
        String displayId = contestProblemDto.getDisplayId();

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", pid)
                        .or()
                        .eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }

        // 比赛中题目显示默认为原标题
        ProblemResDTO problem = problemEntityService.getProblemResDTO(pid, peid, null, null);

        String displayName = problem.getProblemDescriptionList().get(0).getTitle();

        Problem problem_ = new Problem();
        BeanUtil.copyProperties(problem, problem_);

        problem_.setAuth(3); // 设置为比赛题目

        // 修改成比赛题目
        boolean updateProblem = problemEntityService.saveOrUpdate(problem_);

        boolean isOk = contestProblemEntityService.saveOrUpdate(new ContestProblem()
                .setCid(cid).setPid(pid).setPeid(peid).setDisplayTitle(displayName).setDisplayId(displayId));
        if (!isOk || !updateProblem) {
            throw new StatusFailException("添加失败");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],cid:[{}],pid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Contest", "Add_Public_Problem", cid, pid, userRolesVo.getUid(), userRolesVo.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeProblemDescription(ContestProblemDTO contestProblemDto) throws StatusFailException {
        Long pid = contestProblemDto.getPid();
        Long peid = contestProblemDto.getPeid();
        Long cid = contestProblemDto.getCid();

        // 比赛中题目显示默认为原标题
        ProblemResDTO problem = problemEntityService.getProblemResDTO(pid, null, null, null);
        List<ProblemDescription> problemDescriptionList = problem.getProblemDescriptionList();

        boolean isOk = problemDescriptionList.stream()
                .anyMatch(desc -> {
                    if (peid.equals(desc.getId())) {
                        UpdateWrapper<ContestProblem> contestProblemUpdateWrapper = new UpdateWrapper<>();
                        contestProblemUpdateWrapper.eq("cid", cid).eq("pid", pid).set("peid", peid);
                        return contestProblemEntityService.update(contestProblemUpdateWrapper);
                    }
                    return false;
                });

        if (!isOk) {
            throw new StatusFailException("更新失败");
        }
    }

    public void importContestRemoteOJProblem(String name, String problemId, Long cid, String displayId, Long gid)
            throws StatusFailException {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("problem_id", name.toUpperCase() + "-" + problemId);
        if (gid == null) {
            queryWrapper.isNull("gid");
        } else {
            queryWrapper.eq("gid", gid);
        }
        Problem problem = problemEntityService.getOne(queryWrapper, false);

        // 如果该题目不存在，需要先导入
        if (problem == null) {
            AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
            try {
                ProblemStrategy.RemoteProblemInfo otherOJProblemInfo = remoteProblemManager
                        .getOtherOJProblemInfo(name.toUpperCase(), problemId, userRolesVo.getUsername());
                if (otherOJProblemInfo != null) {
                    problem = remoteProblemManager.adminAddOtherOJProblem(otherOJProblemInfo, name, gid);
                    if (problem == null) {
                        throw new StatusFailException("导入新题目失败！请重新尝试！");
                    }
                } else {
                    throw new StatusFailException("导入新题目失败！原因：可能是与该OJ链接超时或题号格式错误！");
                }
            } catch (Exception e) {
                throw new StatusFailException(e.getMessage());
            }
        }

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        Problem finalProblem = problem;
        contestProblemQueryWrapper.eq("cid", cid)
                .and(wrapper -> wrapper.eq("pid", finalProblem.getId())
                        .or()
                        .eq("display_id", displayId));
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        if (contestProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的比赛展示ID已存在！");
        }

        // 比赛中题目显示默认为原标题
        String displayName = problemEntityService.getProblemResDTO(finalProblem.getId(), null, null, null)
                .getProblemDescriptionList().get(0).getTitle();

        // 修改成比赛题目
        boolean updateProblem = problemEntityService.saveOrUpdate(problem.setAuth(3));

        boolean isOk = contestProblemEntityService.saveOrUpdate(new ContestProblem()
                .setCid(cid).setPid(problem.getId()).setDisplayTitle(displayName).setDisplayId(displayId));

        if (!isOk || !updateProblem) {
            throw new StatusFailException("添加失败");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        log.info("[{}],[{}],cid:[{}],pid:[{}],problemId:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Contest", "Add_Remote_Problem", cid, problem.getId(), problem.getProblemId(),
                userRolesVo.getUid(), userRolesVo.getUsername());
    }

}