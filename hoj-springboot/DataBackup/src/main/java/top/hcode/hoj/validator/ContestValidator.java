package top.hcode.hoj.validator;

import cn.hutool.core.util.ReUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestRegisterEntityService;
import top.hcode.hoj.dao.tools.StatisticContestEntityService;
import top.hcode.hoj.manager.group.GroupManager;
import top.hcode.hoj.pojo.bo.Pair_;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestRegister;
import top.hcode.hoj.pojo.entity.contest.StatisticContest;
import top.hcode.hoj.pojo.vo.AdminContestVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 20:06
 * @Description:
 */
@Component
public class ContestValidator {

    @Resource
    private ContestRegisterEntityService contestRegisterEntityService;

    @Autowired
    private GroupValidator groupValidator;

    @Resource
    private CommonValidator commonValidator;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private GroupManager groupManager;

    @Resource
    private StatisticContestEntityService statisticContestEntityService;

    public void validateContest(AdminContestVO adminContestVO) throws StatusFailException {
        commonValidator.validateContent(adminContestVO.getTitle(), "比赛标题", 500);
        commonValidator.validateContentLength(adminContestVO.getDescription(), "比赛描述", 65535);

        if (!Objects.equals(Constants.Contest.TYPE_OI.getCode(), adminContestVO.getType())
                && !Objects.equals(Constants.Contest.TYPE_ACM.getCode(), adminContestVO.getType())) {
            throw new StatusFailException("比赛的赛制必须为ACM(0)、OI(1)！");
        }

        if (Objects.equals(Constants.Contest.TYPE_OI.getCode(), adminContestVO.getType())) {
            if (!Objects.equals(Constants.Contest.OI_RANK_RECENT_SCORE.getName(), adminContestVO.getOiRankScoreType())
                    && !Objects.equals(Constants.Contest.OI_RANK_HIGHEST_SCORE.getName(),
                            adminContestVO.getOiRankScoreType())) {
                throw new StatusFailException("OI比赛排行榜得分类型必须为最近得分(Recent)、最高得分(Highest)！");
            }
        }

        if (!Objects.equals(Constants.Contest.AUTH_PUBLIC.getCode(), adminContestVO.getAuth())
                && !Objects.equals(Constants.Contest.AUTH_PRIVATE.getCode(), adminContestVO.getAuth())
                && !Objects.equals(Constants.Contest.AUTH_PROTECT.getCode(), adminContestVO.getAuth())
                && !Objects.equals(Constants.Contest.AUTH_OFFICIAL.getCode(), adminContestVO.getAuth())
                && !Objects.equals(Constants.Contest.AUTH_PUBLIC_SYNCHRONOUS.getCode(), adminContestVO.getAuth())
                && !Objects.equals(Constants.Contest.AUTH_PRIVATE_SYNCHRONOUS.getCode(), adminContestVO.getAuth())) {
            throw new StatusFailException("比赛的权限必须为公开赛(0)、私有赛(1)、保护赛(2)、正式赛(3)、同步公开赛(4)、同步私有赛(5)！");
        }
    }

    public boolean isSealRank(String uid, Contest contest, Boolean forceRefresh, Boolean isRoot) {
        if (!contest.getSealRank()) {
            return false;
        }
        // 如果是管理员同时选择强制刷新榜单，则封榜无效
        Long gid = contest.getGid();
        boolean isContestAdmin = isRoot || contest.getUid().equals(uid);
        if (forceRefresh && (isContestAdmin || (contest.getIsGroup() && groupValidator.isGroupRoot(uid, gid)))) {
            return false;
        } else if (contest.getSealRank() && contest.getSealRankTime() != null) { // 该比赛开启封榜模式
            Date now = new Date();
            // 如果现在时间处于封榜开始到比赛结束之间
            if (now.after(contest.getSealRankTime()) && now.before(contest.getEndTime())) {
                return true;
            }
            // 或者没有开启赛后自动解除封榜，不可刷新榜单
            return !contest.getAutoRealRank() && now.after(contest.getEndTime());
        }
        return false;
    }

    /**
     * @param contest
     * @param userRolesVo
     * @param isRoot
     * @MethodName validateContestAuth
     * @Description 需要对该比赛做判断，是否处于开始或结束状态才可以获取，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
     * @Since 2021/1/17
     */
    public void validateContestAuth(Contest contest, AccountProfile userRolesVo, Boolean isRoot)
            throws StatusFailException, StatusForbiddenException {

        if (contest == null || !contest.getVisible()) {
            throw new StatusFailException("对不起，该比赛不存在！");
        }

        boolean isContestAdmin = isRoot || contest.getUid().equals(userRolesVo.getUid());
        Long gid = contest.getGid();
        // 若是比赛管理者
        if (isContestAdmin || (contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), gid))) {
            return;
        }

        // 判断一下比赛的状态，还未开始不能查看题目。
        if (contest.getStatus().intValue() != Constants.Contest.STATUS_RUNNING.getCode() &&
                contest.getStatus().intValue() != Constants.Contest.STATUS_ENDED.getCode()) {
            throw new StatusForbiddenException("比赛还未开始，您无权访问该比赛！");
        } else {

            if (contest.getIsGroup() && !groupValidator.isGroupMember(userRolesVo.getUid(), gid)) {
                throw new StatusForbiddenException("对不起，您并非团队内的成员无法参加该团队内的比赛！");
            }

            // 如果是处于比赛正在进行阶段，需要判断该场比赛是否为私有赛或者正式赛，需要判断该用户是否已注册
            if (contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE.getCode()
                    || contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE_SYNCHRONOUS.getCode()
                    || contest.getAuth().intValue() == Constants.Contest.AUTH_OFFICIAL.getCode()) {
                QueryWrapper<ContestRegister> registerQueryWrapper = new QueryWrapper<>();
                registerQueryWrapper.eq("cid", contest.getId()).eq("uid", userRolesVo.getUid());
                ContestRegister register = contestRegisterEntityService.getOne(registerQueryWrapper);
                if (register == null) { // 如果数据为空，表示未注册私有赛，不可访问
                    throw new StatusForbiddenException(
                            contest.getAuth().intValue() == Constants.Contest.AUTH_OFFICIAL.getCode()
                                    || contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE_SYNCHRONOUS
                                            .getCode()
                                                    ? "对不起，请先到比赛首页报名进行注册！"
                                                    : "对不起，请先到比赛首页输入比赛密码进行注册！");
                }

                if (contest.getOpenAccountLimit()
                        && !validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                    throw new StatusForbiddenException("对不起！本次比赛只允许特定账号规则的用户参赛！");
                }
            }
        }

    }

    public void validateJudgeAuth(Contest contest, String uid) throws StatusForbiddenException {

        if (contest.getAuth().intValue() == Constants.Contest.AUTH_PRIVATE.getCode() ||
                contest.getAuth().intValue() == Constants.Contest.AUTH_PROTECT.getCode() ||
                contest.getAuth().intValue() == Constants.Contest.AUTH_OFFICIAL.getCode()) {
            QueryWrapper<ContestRegister> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("cid", contest.getId()).eq("uid", uid);
            ContestRegister register = contestRegisterEntityService.getOne(queryWrapper, false);
            // 如果还没注册
            if (register == null) {
                throw new StatusForbiddenException("对不起，请你先注册该比赛，提交代码失败！");
            }
        }
    }

    public boolean validateAccountRule(String accountRule, String username) {

        String prefix = ReUtil.get("<prefix>([\\s\\S]*?)</prefix>",
                accountRule, 1);
        String suffix = ReUtil.get("<suffix>([\\s\\S]*?)</suffix>",
                accountRule, 1);
        String start = ReUtil.get("<start>([\\s\\S]*?)</start>",
                accountRule, 1);
        String end = ReUtil.get("<end>([\\s\\S]*?)</end>",
                accountRule, 1);
        String extra = ReUtil.get("<extra>([\\s\\S]*?)</extra>",
                accountRule, 1);

        int startNum = Integer.parseInt(start);
        int endNum = Integer.parseInt(end);

        for (int i = startNum; i <= endNum; i++) {
            if (username.equals(prefix + i + suffix)) {
                return true;
            }
        }
        // 额外账号列表
        if (!StringUtils.isEmpty(extra)) {
            String[] accountList = extra.trim().split(" ");
            for (String account : accountList) {
                if (username.equals(account)) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<Contest> validateContestList(String cids)
            throws StatusFailException, StatusForbiddenException {

        // 将对应的 cids 转化为 cid 用 + 分割的状态
        String statistic_cids = getStatisticRankCids(cids);

        // 如果对应的cids有缓存的uid，则不检查登录状态
        Boolean isCheck = !statistic_cids.equals(cids);

        List<Contest> contestList = new ArrayList<>();

        List<Pair_<String, String>> contest_cids = getSplitedOjCid(statistic_cids);

        for (int i = 0; i < contest_cids.size(); i++) {
            String input_cid = statistic_cids.split("\\+")[i];

            String oj = contest_cids.get(i).getKey();
            String cid = contest_cids.get(i).getValue();

            if (oj == null) {
                throw new StatusFailException("错误，请输入正确的 cid, 对应错误 cid: " + input_cid + "无效");
            }

            Contest contest = new Contest();

            if (oj.equals("default")) {
                contest = contestEntityService.getById(Long.valueOf(cid));

                if (contest == null) { // 查询不存在
                    throw new StatusFailException("错误：cid对应比赛不存在, 对应错误 cid: " + input_cid + "无效");
                }

                contest.setOj("default");

                Boolean isACM = (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode());

                if (!isACM) {
                    throw new StatusFailException("错误：cid对应比赛不为ACM类型, 对应错误 cid: " + input_cid + "无效");
                }

                if (contest.getStatus().intValue() != Constants.Contest.STATUS_ENDED.getCode()) {
                    throw new StatusFailException("错误：cid对应比赛还未结束, 对应错误 cid: " + input_cid + "无效");
                }

                if (isCheck) {
                    boolean isRoot = groupManager.getGroupAuthAdmin(contest.getGid());

                    // 获取当前登录的用户
                    AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

                    if (userRolesVo == null) {
                        throw new StatusForbiddenException("请先登录！");
                    }

                    // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
                    validateContestAuth(contest, userRolesVo, isRoot);
                }

            } else {
                contest.setTitle(cid);
                contest.setOj(oj);
            }

            contestList.add(contest);
        }

        return contestList;
    }

    public List<Integer> validatePercentList(String percents) {
        List<Integer> contestPercents = Arrays.stream(percents.split("\\-"))
                .map(String::trim) // 去除每个段的前后空白字符
                .map(percent -> Integer.parseInt(percent.replace("%", ""))) // 去掉 % 并转换为 Integer
                .collect(Collectors.toList());
        return contestPercents;
    }

    public List<Pair_<String, String>> getSplitedOjCid(String cids) throws StatusFailException {
        List<Pair_<String, String>> cidList = new ArrayList<>();

        // 使用 '+' 分割字符串，然后将每个段作为字符串类型收集到列表中
        List<String> contestCids = Arrays.stream(cids.split("\\+"))
                .map(String::trim) // 去除每个段的前后空白字符
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(contestCids)) {
            throw new StatusFailException("错误，请传入 cids !");
        }

        for (String contestCid : contestCids) {
            cidList.add(identifyString(contestCid));
        }
        return cidList;
    }

    private static final String[] PREFIXES = { "cf", "gym", "vj", "hdu", "nowcoder", "pta", "xcpc" };
    private static final String PREFIX_REGEX = "^(cf|gym|vj|hdu|nowcoder|pta)(\\d+)$|^(xcpc)(.+)$";

    public Pair_<String, String> identifyString(String input) {
        // 检查是否是纯数字
        if (input.matches("\\d+")) {
            return new Pair_<>("default", input);
        }

        // 检查是否以特定前缀开头的纯数字
        if (input.matches(PREFIX_REGEX)) {
            for (String prefix : PREFIXES) {
                if (input.startsWith(prefix)) {
                    return new Pair_<>(prefix, input.substring(prefix.length()));
                }
            }
        }

        // 不符合条件的情况
        return new Pair_<>(null, null);
    }

    public List<String> getSplitedCid(String cids) throws StatusFailException {
        List<String> cidList = new ArrayList<>();

        // 使用 '+' 分割字符串，然后将每个段作为字符串类型收集到列表中
        List<String> contestCids = Arrays.stream(cids.split("\\+"))
                .map(String::trim) // 去除每个段的前后空白字符
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(contestCids)) {
            throw new StatusFailException("错误，请传入 cids !");
        }

        for (String contestCid : contestCids) {
            cidList.add(contestCid);
        }
        return cidList;
    }

    public String getStatisticRankCids(String keywords) {
        StatisticContest statisticContest = statisticContestEntityService.getOne(
                new QueryWrapper<StatisticContest>().eq("scid", keywords));

        Boolean is_ = statisticContest != null;

        return is_ ? (statisticContest.getCids()) + "+" : (keywords.endsWith("+") ? keywords : keywords + "+");
    }

}