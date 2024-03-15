package top.hcode.hoj.manager.oj;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestProblemEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.ContestResolverOnlineConfigVO;
import top.hcode.hoj.pojo.vo.ContestResolverOnlineRunVO;
import top.hcode.hoj.pojo.vo.ContestResolverOnlineTeamVO;
import top.hcode.hoj.pojo.vo.ContestResolverOnlineVO;
import top.hcode.hoj.pojo.vo.ContestScrollBoardInfoVO;
import top.hcode.hoj.pojo.vo.ContestScrollBoardSubmissionVO;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @Author Himit_ZH
 * @Date 2022/10/3
 */
@Component
public class ContestScrollBoardManager {

    @Resource
    private ContestEntityService contestEntityService;

    @Resource
    private ContestProblemEntityService contestProblemEntityService;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private ContestCalculateRankManager contestCalculateRankManager;

    public ContestScrollBoardInfoVO getContestScrollBoardInfo(Long cid) throws StatusFailException {
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("比赛不存在 (The contest does not exist)");
        }

        if (!Objects.equals(contest.getType(), Constants.Contest.TYPE_ACM.getCode())) {
            throw new StatusFailException("非ACM赛制的比赛无法进行滚榜  (Non - ACM contest board cannot be rolled)");
        }

        if (!contest.getSealRank()) {
            throw new StatusFailException("比赛未开启封榜，无法进行滚榜 (The contest has not been closed, and cannot roll)");
        }

        if (!Objects.equals(contest.getStatus(), Constants.Contest.STATUS_ENDED.getCode())) {
            throw new StatusFailException("比赛未结束，禁止进行滚榜 (Roll off is prohibited before the contest is over)");
        }

        QueryWrapper<ContestProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid);
        List<ContestProblem> contestProblemList = contestProblemEntityService.list(queryWrapper);

        List<Long> pidList = contestProblemList.stream().map(ContestProblem::getPid).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(pidList)) {
            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.select("id", "auth")
                    .ne("auth", 2)
                    .in("id", pidList);
            List<Problem> problemList = problemEntityService.list(problemQueryWrapper);
            List<Long> idList = problemList.stream().map(Problem::getId).collect(Collectors.toList());
            contestProblemList = contestProblemList.stream()
                    .filter(p -> idList.contains(p.getPid()))
                    .collect(Collectors.toList());
        }

        HashMap<String, String> balloonColor = new HashMap<>();
        for (ContestProblem contestProblem : contestProblemList) {
            balloonColor.put(contestProblem.getDisplayId(), contestProblem.getColor());
        }

        ContestScrollBoardInfoVO info = new ContestScrollBoardInfoVO();
        info.setId(cid);
        info.setProblemCount(contestProblemList.size());
        info.setBalloonColor(balloonColor);
        info.setRankShowName(contest.getRankShowName());
        info.setStarUserList(starAccountToList(contest.getStarAccount()));
        info.setStartTime(contest.getStartTime());
        info.setSealRankTime(contest.getSealRankTime());

        return info;
    }

    private List<String> starAccountToList(String starAccountStr) {
        if (StringUtils.isEmpty(starAccountStr)) {
            return new ArrayList<>();
        }
        JSONObject jsonObject = JSONUtil.parseObj(starAccountStr);
        List<String> list = jsonObject.get("star_account", List.class);
        return list;
    }

    public List<ContestScrollBoardSubmissionVO> getContestScrollBoardSubmission(Long cid, Boolean removeStar)
            throws StatusFailException {
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("比赛不存在 (The contest does not exist)");
        }

        if (!Objects.equals(contest.getType(), Constants.Contest.TYPE_ACM.getCode())) {
            throw new StatusFailException("非ACM赛制的比赛无法进行滚榜  (Non - ACM contest board cannot be rolled)");
        }

        if (!contest.getSealRank()) {
            throw new StatusFailException("比赛未开启封榜，无法进行滚榜 (The contest has not been closed, and cannot roll)");
        }

        if (!Objects.equals(contest.getStatus(), Constants.Contest.STATUS_ENDED.getCode())) {
            throw new StatusFailException("比赛未结束，禁止进行滚榜 (Roll off is prohibited before the contest is over)");
        }

        List<String> removeUidList = userInfoEntityService.getNowContestAdmin(cid);

        if (!removeUidList.contains(contest.getUid())) {
            removeUidList.add(contest.getUid());
        }
        List<ContestScrollBoardSubmissionVO> submissions = judgeEntityService.getContestScrollBoardSubmission(cid,
                removeUidList);
        if (removeStar && StrUtil.isNotBlank(contest.getStarAccount())) {
            JSONObject jsonObject = JSONUtil.parseObj(contest.getStarAccount());
            List<String> usernameList = jsonObject.get("star_account", List.class);
            if (!CollectionUtils.isEmpty(usernameList)) {
                submissions = submissions.stream()
                        .filter(submission -> !usernameList.contains(submission.getUsername()))
                        .collect(Collectors.toList());
            }
        }
        submissions = submissions.stream()
                .filter(submission -> submission.getSubmitTime().getTime() < contest.getEndTime().getTime())
                .collect(Collectors.toList());
        return submissions;
    }

    private static AtomicLong currentId = new AtomicLong(1000000000000000L);

    public ContestResolverOnlineVO getContestResolverOnlineInfo(Long cid, Boolean removeStar)
            throws StatusFailException {

        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("比赛不存在 (The contest does not exist)");
        }

        if (!Objects.equals(contest.getType(), Constants.Contest.TYPE_ACM.getCode())) {
            throw new StatusFailException("非ACM赛制的比赛无法进行滚榜  (Non - ACM contest board cannot be rolled)");
        }

        if (!contest.getSealRank()) {
            throw new StatusFailException("比赛未开启封榜，无法进行滚榜 (The contest has not been closed, and cannot roll)");
        }

        if (!Objects.equals(contest.getStatus(), Constants.Contest.STATUS_ENDED.getCode())) {
            throw new StatusFailException("比赛未结束，禁止进行滚榜 (Roll off is prohibited before the contest is over)");
        }

        if (contest.getAuth() == Constants.Contest.AUTH_OFFICIAL.getCode()) {
            throw new StatusFailException("比赛为公开赛，暂不支持此类滚榜");
        }

        List<String> removeUidList = userInfoEntityService.getNowContestAdmin(cid);

        if (!removeUidList.contains(contest.getUid())) {
            removeUidList.add(contest.getUid());
        }
        List<ContestScrollBoardSubmissionVO> submissions = judgeEntityService.getContestScrollBoardSubmission(cid,
                removeUidList);

        if (removeStar && StrUtil.isNotBlank(contest.getStarAccount())) {
            JSONObject jsonObject = JSONUtil.parseObj(contest.getStarAccount());
            List<String> usernameList = jsonObject.get("star_account", List.class);
            if (!CollectionUtils.isEmpty(usernameList)) {
                submissions = submissions.stream()
                        .filter(submission -> !usernameList.contains(submission.getUsername()))
                        .collect(Collectors.toList());
            }
        }

        // 排除赛后提交
        submissions = submissions.stream()
                .filter(submission -> submission.getSubmitTime().getTime() < contest.getEndTime().getTime())
                .collect(Collectors.toList());

        // 保留队伍中对应题目的 AC 为 1 个
        Set<String> uniqueKeys = new HashSet<>();
        submissions.removeIf(submission -> !uniqueKeys.add(submission.getDisplayId() + "$" + submission.getUid())
                && submission.getStatus() == Constants.Judge.STATUS_ACCEPTED.getStatus());

        // 为每个队伍分配一个 16位的Long 编号
        Map<String, Long> usernameToIdMap = submissions.stream()
                .map(ContestScrollBoardSubmissionVO::getUsername)
                .distinct()
                .collect(Collectors.toMap(
                        username -> username,
                        username -> generateId(),
                        (existing, replacement) -> existing));

        // 题目对应的准备信息
        QueryWrapper<ContestProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("display_id", "color")
                .eq("cid", cid)
                .orderByAsc("CAST(display_id AS SIGNED)", "display_id");
        List<ContestProblem> contestProblemList = contestProblemEntityService.list(queryWrapper);

        List<String> problem_id = contestProblemList.stream()
                .map(contestProblem -> contestProblem.getDisplayId())
                .collect(Collectors.toList());

        List<Map<String, String>> balloonColorList = contestProblemList.stream()
                .map(contestProblem -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("background_color", contestProblem.getColor());
                    map.put("color", "#fff");
                    return map;
                })
                .collect(Collectors.toList());

        // TODO config
        ContestResolverOnlineConfigVO configVO = new ContestResolverOnlineConfigVO()
                .setContest_name(contest.getTitle())
                .setStart_time(contest.getStartTime().getTime() / 1000)
                .setEnd_time(contest.getEndTime().getTime() / 1000)
                .setFrozen_time(DateUtil.between(contest.getSealRankTime(), contest.getEndTime(), DateUnit.SECOND))
                .setPenalty(1200L)
                .setOrganization("School")
                .setProblem_quantity(problem_id.size())
                .setProblem_id(problem_id)
                .setBalloon_color(balloonColorList);

        // TODO run
        List<ContestResolverOnlineRunVO> contestResolverOnlineRunVoList = submissions.stream()
                .map(submission -> {
                    ContestResolverOnlineRunVO runVO = new ContestResolverOnlineRunVO();
                    runVO.setTeam_id(usernameToIdMap.get(submission.getUsername()));
                    runVO.setProblem_id(problem_id.indexOf(submission.getDisplayId()));
                    runVO.setTimestamp(
                            DateUtil.between(submission.getStartTime(), submission.getSubmitTime(), DateUnit.SECOND));
                    Integer status = submission.getStatus();
                    String status_ = status == Constants.Judge.STATUS_ACCEPTED.getStatus() ? "correct"
                            : (status == Constants.Judge.STATUS_PENDING.getStatus() ||
                                    status == Constants.Judge.STATUS_COMPILING.getStatus() ||
                                    status == Constants.Judge.STATUS_JUDGING.getStatus() ||
                                    status == Constants.Judge.STATUS_SUBMITTING.getStatus() ? "pending" : "incorrect");
                    runVO.setStatus(status_);
                    return runVO;
                })
                .collect(Collectors.toList());

        // TODO team
        List<ContestScrollBoardSubmissionVO> uniqueSubmissions = submissions.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(ContestScrollBoardSubmissionVO::getUsername))),
                        ArrayList::new));

        Map<String, ContestResolverOnlineTeamVO> contestResolverOnlineTeamMap = uniqueSubmissions.stream()
                .map(submission -> {
                    ContestResolverOnlineTeamVO contestResolverOnlineTeamVo = new ContestResolverOnlineTeamVO();
                    contestResolverOnlineTeamVo
                            .setTeam_id(String.valueOf(usernameToIdMap.get(submission.getUsername())));
                    contestResolverOnlineTeamVo.setName(submission.getUsername());
                    contestResolverOnlineTeamVo.setOrganization(submission.getSchool());
                    contestResolverOnlineTeamVo.setMembers(new ArrayList<>(Arrays.asList(submission.getUsername())));

                    List<String> usernameList = removeStar && StrUtil.isNotBlank(contest.getStarAccount())
                            ? Optional.ofNullable(JSONUtil.parseObj(contest.getStarAccount()))
                                    .map(json -> json.getJSONArray("star_account"))
                                    .map(jsonArray -> jsonArray.toList(String.class))
                                    .orElseGet(ArrayList::new)
                            : new ArrayList<>();

                    if (!CollectionUtils.isEmpty(usernameList) && usernameList.contains(submission.getUsername())) {
                        contestResolverOnlineTeamVo.setUnofficial(true);
                        contestResolverOnlineTeamVo.setOfficial(0);
                    } else {
                        contestResolverOnlineTeamVo.setUnofficial(false);
                        contestResolverOnlineTeamVo.setOfficial(1);
                    }

                    contestResolverOnlineTeamVo.setGirl(submission.getGender().equals("female"));
                    return contestResolverOnlineTeamVo;
                })
                .collect(Collectors.toMap(ContestResolverOnlineTeamVO::getTeam_id, team -> team));

        ContestResolverOnlineVO contestResolverOnlineVo = new ContestResolverOnlineVO();
        contestResolverOnlineVo.setConfig(configVO);
        contestResolverOnlineVo.setRun(contestResolverOnlineRunVoList);
        contestResolverOnlineVo.setTeam(contestResolverOnlineTeamMap);

        return contestResolverOnlineVo;
    }

    private static synchronized long generateId() {
        return currentId.getAndIncrement();
    }
}
