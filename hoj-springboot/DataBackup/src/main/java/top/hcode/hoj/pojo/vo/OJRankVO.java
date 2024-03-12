package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/7 14:56
 * @Description:
 */
@ApiModel(value = "O排行榜数据类OJRankVO", description = "")
@Data
public class OJRankVO implements Serializable {
    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "头衔、称号")
    private String titleName;

    @ApiModelProperty(value = "头衔、称号的颜色")
    private String titleColor;

    @ApiModelProperty(value = "codeforces 分数")
    private Integer codeforcesRating;

    @ApiModelProperty(value = "codeforces 最大分数")
    private Integer codeforcesMaxRating;

    @ApiModelProperty(value = "nowcoder 分数")
    private Integer nowcoderRating;

    @ApiModelProperty(value = "codeforces AC")
    private Integer codeforcesAc;

    @ApiModelProperty(value = "nowcoder AC")
    private Integer nowcoderAc;

    @ApiModelProperty(value = "vjudge AC")
    private Integer vjudgeAc;

    @ApiModelProperty(value = "poj AC")
    private Integer pojAc;

    @ApiModelProperty(value = "atcode AC")
    private Integer atcodeAc;

    @ApiModelProperty(value = "leetcode AC")
    private Integer leetcodeAc;

    @ApiModelProperty(value = "全部 AC")
    private Integer sum;
}