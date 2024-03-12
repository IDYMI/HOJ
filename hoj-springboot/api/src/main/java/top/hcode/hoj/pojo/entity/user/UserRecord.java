package top.hcode.hoj.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserRecord对象", description = "")
public class UserRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "codeforces分数")
    private Integer codeforcesRating;

    @ApiModelProperty(value = "codeforces最大分数")
    private Integer codeforcesMaxRating;

    @ApiModelProperty(value = "nowcoder分数")
    private Integer nowcoderRating;

    @ApiModelProperty(value = "codeforces AC")
    private Integer codeforcesAc;

    @ApiModelProperty(value = "luogu AC")
    private Integer luoguAc;

    @ApiModelProperty(value = "nowcoder AC")
    private Integer nowcoderAc;

    @ApiModelProperty(value = "vjudge AC")
    private Integer vjudgeAc;

    @ApiModelProperty(value = "zzuiloj AC")
    private Integer zzuilojAc;

    @ApiModelProperty(value = "poj AC")
    private Integer pojAc;

    @ApiModelProperty(value = "atcode AC")
    private Integer atcodeAc;

    @ApiModelProperty(value = "leetcode AC")
    private Integer leetcodeAc;

    @ApiModelProperty(value = "是否显示")
    private Boolean see;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
