package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserMultiOjVO {

    private Long id;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "codeforces 用户名")
    private String codeforces;

    @ApiModelProperty(value = "nowcoder 用户名")
    private String nowcoder;

    @ApiModelProperty(value = "vjudge 用户名")
    private String vjudge;

    @ApiModelProperty(value = "poj 用户名")
    private String poj;

    @ApiModelProperty(value = "atcode 用户名")
    private String atcode;

    @ApiModelProperty(value = "leetcode 用户名")
    private String leetcode;

    @ApiModelProperty(value = "是否展示")
    private Boolean see;
}
