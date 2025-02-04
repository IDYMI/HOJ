package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @Date: 2021/1/7 22:27
 * @Description:用户主页的数据格式
 */
@ApiModel(value = "用户主页的数据格式类UserHomeVO", description = "")
@Data
public class UserHomeVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "gender")
    private String gender;

    @ApiModelProperty(value = "github地址")
    private String github;

    @ApiModelProperty(value = "博客地址")
    private String blog;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "头衔、称号")
    private String titleName;

    @ApiModelProperty(value = "头衔、称号的颜色")
    private String titleColor;

    @ApiModelProperty(value = "总提交数")
    private Integer total;

    @ApiModelProperty(value = "正在攻克列表")
    private List<String> overcomingList;

    @ApiModelProperty(value = "已解决题目列表")
    private List<String> solvedList;

    @ApiModelProperty(value = "已参加比赛id")
    private List<Long> contestPidList;

    @ApiModelProperty(value = "日期对应的比赛名次数据列表")
    private List<HashMap<String, Object>> dataList;

    @ApiModelProperty(value = "难度=>[P1000,P1001]")
    private Map<Integer, List<UserHomeProblemVO>> solvedGroupByDifficulty;

    @ApiModelProperty(value = "真实姓名")
    private String realName;

    @ApiModelProperty(value = "专业/班级")
    private String course;

    @ApiModelProperty(value = "联系方式")
    private String phoneNumber;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "角色列表")
    private List<String> roles;

    @ApiModelProperty(value = "最近上线时间")
    private Date recentLoginTime;

}