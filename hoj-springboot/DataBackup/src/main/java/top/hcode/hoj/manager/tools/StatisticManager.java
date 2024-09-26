package top.hcode.hoj.manager.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.config.NacosSwitchConfig;
import top.hcode.hoj.config.SwitchConfig;
import top.hcode.hoj.dao.tools.StatisticContestEntityService;
import top.hcode.hoj.dao.tools.StatisticRankEntityService;
import top.hcode.hoj.manager.oj.ContestRankManager;
import top.hcode.hoj.mapper.StatisticContestMapper;
import top.hcode.hoj.pojo.bo.Pair_;
import top.hcode.hoj.pojo.dto.StatisticDTO;
import top.hcode.hoj.pojo.dto.StatisticAccountDTO;
import top.hcode.hoj.pojo.entity.contest.*;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.ContestValidator;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Component
public class StatisticManager {

    @Autowired
    private ContestValidator contestValidator;

    @Autowired
    private ContestRankManager contestRankManager;

    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

    @Autowired
    private StatisticContestMapper statisticContestMapper;

    @Resource
    private StatisticRankEntityService statisticRankEntityService;

    @Resource
    private StatisticContestEntityService statisticContestEntityService;

    public IPage<StatisticContest> getStatisticList(Integer currentPage, Integer limit,
            String keyword) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        IPage<StatisticContest> iPage = new Page<>(currentPage, limit);

        return statisticContestMapper.getStatisticContestList(iPage, keyword);
    }

    public IPage<ACMContestRankVO> getStatisticRank(Integer currentPage, Integer limit, String cids, String keyword)
            throws StatusFailException, StatusForbiddenException, Exception {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        List<ACMContestRankVO> acmContestRankVoList = new ArrayList<>();

        // 如果传入的是scid
        StatisticContest statisticContest = statisticContestEntityService.getOne(
                new QueryWrapper<StatisticContest>().eq("scid", cids), false);

        if (statisticContest != null) {
            List<StatisticRank> statisticRankList = statisticRankEntityService.list(
                    new QueryWrapper<StatisticRank>().in("scid", statisticContest.getScid()));

            // 转换 StatisticRank 为 ACMContestRankVO
            acmContestRankVoList = (List<ACMContestRankVO>) statisticRankList.stream()
                    .map(statisticRank -> {
                        ACMContestRankVO acmContestRankVo = new ACMContestRankVO();
                        BeanUtil.copyProperties(statisticContest, acmContestRankVo);
                        BeanUtil.copyProperties(statisticRank, acmContestRankVo);
                        acmContestRankVo.setPercents(statisticContest.getPercents());
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            acmContestRankVo
                                    .setSubmissionInfo(objectMapper.readValue(statisticRank.getJson(), HashMap.class));
                        } catch (JsonProcessingException e) {
                        }
                        return acmContestRankVo;
                    })
                    .collect(Collectors.toList());
        } else {
            List<Contest> contestList = contestValidator.validateContestList(cids);

            StatisticDTO statisticDto = new StatisticDTO()
                    .setKeyword(keyword)
                    .setCids(cids)
                    .setLimit(limit)
                    .setCurrentPage(currentPage);

            StatisticVO statisticVo = new StatisticVO()
                    .setStatisticDTO(statisticDto)
                    .setContestList(contestList);

            acmContestRankVoList = contestRankManager.getStatisticRankList(statisticVo);
        }

        cids = contestValidator.getStatisticRankCids(cids);

        // 筛选关键词 + 比例调整
        acmContestRankVoList = contestRankManager.getStatisticRankListByKeywordAndPercents(acmContestRankVoList, cids,
                null, keyword);

        return contestRankManager.getPagingRankList(acmContestRankVoList, currentPage, limit);
    }

    public String getStatisticRankCids(String scid) {
        return contestValidator.getStatisticRankCids(scid);
    }

    public IPage<StatisticContest> getAdminStatisticList(Integer currentPage, Integer limit,
            String keyword) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        IPage<StatisticContest> iPage = new Page<>(currentPage, limit);

        return statisticContestMapper.getAdminStatisticContestList(iPage, keyword);
    }

    public IPage<ACMContestRankVO> addStatisticRank(StatisticDTO statisticDto)
            throws StatusFailException, StatusForbiddenException, Exception {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        // 只有超级管理员和普通管理员
        if (!isRoot) {
            throw new StatusForbiddenException("对不起，你无权创建！");
        }

        Integer currentPage = statisticDto.getCurrentPage();
        Integer limit = statisticDto.getLimit();

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 30;

        String cids = statisticDto.getCids();

        List<Contest> contestList = contestValidator.validateContestList(cids);

        List<Pair_<String, String>> accountList = new ArrayList<>();

        StatisticAccountDTO statisticAccountDto = statisticDto.getStatisticAccountDto();
        SwitchConfig switchConfig = nacosSwitchConfig.getSwitchConfig();
        if (statisticAccountDto != null) {
            for (Contest contest : contestList) {
                String oj = contest.getOj();
                String loginUsername = "";
                String loginPassword = "";

                switch (oj) {
                    case "cf":
                    case "gym":
                        loginUsername = statisticAccountDto.getCf();
                        loginPassword = getLoginPassword(loginUsername, switchConfig.getCfUsernameList(),
                                switchConfig.getCfPasswordList());
                        break;
                    case "hdu":
                        loginUsername = statisticAccountDto.getHdu();
                        loginPassword = getLoginPassword(loginUsername, switchConfig.getHduUsernameList(),
                                switchConfig.getHduPasswordList());
                        break;
                    case "vj":
                        loginUsername = statisticAccountDto.getVj();
                        loginPassword = getLoginPassword(loginUsername, switchConfig.getVjUsernameList(),
                                switchConfig.getVjPasswordList());
                        break;
                }
                accountList.add(new Pair_<>(loginUsername, loginPassword));
            }
        }

        // 处理传入信息
        StatisticVO statisticVo = new StatisticVO()
                .setStatisticDTO(statisticDto)
                .setContestList(contestList)
                .setAccountList(accountList);

        List<ACMContestRankVO> acmContestRankVoList = contestRankManager.getStatisticRankList(statisticVo);

        String keyword = statisticDto.getKeyword();
        String percents = statisticDto.getPercents();

        // 筛选关键词 + 比例调整
        acmContestRankVoList = contestRankManager.getStatisticRankListByKeywordAndPercents(acmContestRankVoList, cids,
                percents, keyword);

        // 重新排序
        acmContestRankVoList = contestRankManager.getSortedACMContestRankVoList(acmContestRankVoList);

        if (statisticDto.getIsSave()) {
            String scid = IdUtil.fastSimpleUUID();

            List<String> titles = contestList.stream()
                    .map(contest -> contest.getOj() + contest.getTitle())
                    .collect(Collectors.toList());

            // 创建 StatisticContest 并保存
            StatisticContest statisticContest = new StatisticContest()
                    .setScid(scid)
                    .setTitle(statisticDto.getTitle())
                    .setCids(String.join("+", titles))
                    .setPercents(percents)
                    .setAuthor(userRolesVo.getUsername());
            statisticContestEntityService.save(statisticContest);

            // 遍历并保存 StatisticRank
            acmContestRankVoList.forEach(acmContestRankVO -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String submissionInfoStr = objectMapper.writeValueAsString(acmContestRankVO.getSubmissionInfo());

                    StatisticRank statisticRank = new StatisticRank();
                    BeanUtil.copyProperties(acmContestRankVO, statisticRank);

                    statisticRankEntityService.save(statisticRank.setJson(submissionInfoStr).setScid(scid));
                } catch (JsonProcessingException e) {
                }
            });
        }

        return contestRankManager.getPagingRankList(acmContestRankVoList, currentPage, limit);
    }

    public void deleteStatisticRank(String scid) {
        // 删除对应的 StatisticContest
        statisticContestEntityService.remove(new UpdateWrapper<StatisticContest>().eq("scid", scid));
    }

    public void changeStatisticVisible(String scid, Boolean show) {
        UpdateWrapper<StatisticContest> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("scid", scid).set("visible", show);
        statisticContestEntityService.update(queryWrapper);
    }

    private String getLoginPassword(String loginUsername, List<String> usernameList, List<String> passwordList) {
        for (int i = 0; i < usernameList.size(); i++) {
            if (usernameList.get(i).equals(loginUsername)) {
                return passwordList.get(i);
            }
        }
        return "";
    }

}
