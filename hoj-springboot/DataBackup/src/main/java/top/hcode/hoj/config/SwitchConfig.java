package top.hcode.hoj.config;

import cn.hutool.core.text.UnicodeUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/26
 */
@Data
public class SwitchConfig {

    private List<String> hduUsernameList = new ArrayList<>();

    private List<String> hduPasswordList = new ArrayList<>();

    private List<String> cfUsernameList = new ArrayList<>();

    private List<String> cfPasswordList = new ArrayList<>();

    private List<String> pojUsernameList = new ArrayList<>();

    private List<String> pojPasswordList = new ArrayList<>();

    private List<String> atcoderUsernameList = new ArrayList<>();

    private List<String> atcoderPasswordList = new ArrayList<>();

    private List<String> spojUsernameList = new ArrayList<>();

    private List<String> spojPasswordList = new ArrayList<>();

    private List<String> libreojUsernameList = new ArrayList<>();

    private List<String> libreojPasswordList = new ArrayList<>();

    private List<String> scpcUsernameList = new ArrayList<>();

    private List<String> scpcPasswordList = new ArrayList<>();

    private List<String> qojUsernameList = new ArrayList<>();

    private List<String> qojPasswordList = new ArrayList<>();

    private List<String> nswojUsernameList = new ArrayList<>();

    private List<String> nswojPasswordList = new ArrayList<>();

    private List<String> newojUsernameList = new ArrayList<>();

    private List<String> newojPasswordList = new ArrayList<>();

    private List<String> mossUsernameList = new ArrayList<>();

    private List<String> vjUsernameList = new ArrayList<>();

    private List<String> vjPasswordList = new ArrayList<>();

    /**
     * SCPC 超管账号
     */
    private String scpcSuperAdminAccount;

    /**
     * SCPC 超管密码
     */
    private String scpcSuperAdminPassword;

    /**
     * 是否开启公开评论区
     */
    private Boolean openPublicDiscussion = true;

    /**
     * 是否开启团队评论区
     */
    private Boolean openGroupDiscussion = true;

    /**
     * 是否开启比赛讨论区
     */
    private Boolean openContestComment = true;

    /**
     * 是否开启公开评测
     */
    private Boolean openPublicJudge = true;

    /**
     * 是否开启团队评测
     */
    private Boolean openGroupJudge = true;

    /**
     * 是否开启比赛评测
     */
    private Boolean openContestJudge = true;

    /**
     * 是否隐藏非比赛提交详情的代码（超管不受限制）
     */
    private Boolean hideNonContestSubmissionCode = false;

    /**
     * 非比赛的提交间隔秒数
     */
    private Integer defaultSubmitInterval = 8;

    /**
     * 每天可以创建的团队数量
     */
    private Integer defaultCreateGroupDailyLimit = 2;

    /**
     * 总共可以拥有的团队数量
     */
    private Integer defaultCreateGroupLimit = 5;

    /**
     * 创建团队的前提：20道题目通过
     */
    private Integer defaultCreateGroupACInitValue = 20;

    /**
     * 每天可以创建的帖子数量
     */
    private Integer defaultCreateDiscussionDailyLimit = 5;

    /**
     * 创建讨论帖子的前提：10道题目通过
     */
    private Integer defaultCreateDiscussionACInitValue = 10;

    /**
     * 评论和回复的前提：10道题目通过
     */
    private Integer defaultCreateCommentACInitValue = 10;

    public void formatStrRemoteAccount2Unicode() {
        this.setHduUsernameList(format2Unicode(this.hduUsernameList));
        this.setHduPasswordList(format2Unicode(this.hduPasswordList));
        this.setPojUsernameList(format2Unicode(this.pojUsernameList));
        this.setPojPasswordList(format2Unicode(this.pojPasswordList));
        this.setCfUsernameList(format2Unicode(this.cfUsernameList));
        this.setCfPasswordList(format2Unicode(this.cfPasswordList));
        this.setAtcoderUsernameList(format2Unicode(this.atcoderUsernameList));
        this.setAtcoderPasswordList(format2Unicode(this.atcoderPasswordList));
        this.setSpojUsernameList(format2Unicode(this.spojUsernameList));
        this.setSpojPasswordList(format2Unicode(this.spojPasswordList));
        this.setLibreojUsernameList(format2Unicode(this.libreojUsernameList));
        this.setLibreojPasswordList(format2Unicode(this.libreojPasswordList));
        this.setScpcUsernameList(format2Unicode(this.scpcUsernameList));
        this.setScpcPasswordList(format2Unicode(this.scpcPasswordList));
        this.setQojUsernameList(format2Unicode(this.qojUsernameList));
        this.setQojPasswordList(format2Unicode(this.qojPasswordList));
        this.setNswojUsernameList(format2Unicode(this.nswojUsernameList));
        this.setNswojPasswordList(format2Unicode(this.nswojPasswordList));
        this.setNewojUsernameList(format2Unicode(this.newojUsernameList));
        this.setNewojPasswordList(format2Unicode(this.newojPasswordList));
        this.setMossUsernameList(format2Unicode(this.mossUsernameList));
        this.setVjUsernameList(format2Unicode(this.vjUsernameList));
        this.setVjPasswordList(format2Unicode(this.vjPasswordList));
    }

    public void convertUnicodeRemoteAccount2Str() {
        this.setHduUsernameList(convertUnicode2Str(this.hduUsernameList));
        this.setHduPasswordList(convertUnicode2Str(this.hduPasswordList));
        this.setPojUsernameList(convertUnicode2Str(this.pojUsernameList));
        this.setPojPasswordList(convertUnicode2Str(this.pojPasswordList));
        this.setCfUsernameList(convertUnicode2Str(this.cfUsernameList));
        this.setCfPasswordList(convertUnicode2Str(this.cfPasswordList));
        this.setAtcoderUsernameList(convertUnicode2Str(this.atcoderUsernameList));
        this.setAtcoderPasswordList(convertUnicode2Str(this.atcoderPasswordList));
        this.setSpojUsernameList(convertUnicode2Str(this.spojUsernameList));
        this.setSpojPasswordList(convertUnicode2Str(this.spojPasswordList));
        this.setLibreojUsernameList(convertUnicode2Str(this.libreojUsernameList));
        this.setLibreojPasswordList(convertUnicode2Str(this.libreojPasswordList));
        this.setScpcUsernameList(convertUnicode2Str(this.scpcUsernameList));
        this.setScpcPasswordList(convertUnicode2Str(this.scpcPasswordList));
        this.setQojUsernameList(convertUnicode2Str(this.qojUsernameList));
        this.setQojPasswordList(convertUnicode2Str(this.qojPasswordList));
        this.setNswojUsernameList(convertUnicode2Str(this.nswojUsernameList));
        this.setNswojPasswordList(convertUnicode2Str(this.nswojPasswordList));
        this.setNewojUsernameList(convertUnicode2Str(this.newojUsernameList));
        this.setNewojPasswordList(convertUnicode2Str(this.newojPasswordList));
        this.setMossUsernameList(convertUnicode2Str(this.mossUsernameList));
        this.setVjUsernameList(convertUnicode2Str(this.vjUsernameList));
        this.setVjPasswordList(convertUnicode2Str(this.vjPasswordList));
    }

    private List<String> format2Unicode(List<String> strList) {
        if (CollectionUtils.isEmpty(strList)) {
            return Collections.emptyList();
        }
        List<String> unicodeList = new ArrayList<>();
        for (String str : strList) {
            unicodeList.add(UnicodeUtil.toUnicode(str, true));
        }
        return unicodeList;
    }

    private List<String> convertUnicode2Str(List<String> unicodeList) {
        if (CollectionUtils.isEmpty(unicodeList)) {
            return Collections.emptyList();
        }
        List<String> strList = new ArrayList<>();
        for (String str : unicodeList) {
            strList.add(UnicodeUtil.toString(str));
        }
        return strList;
    }
}
