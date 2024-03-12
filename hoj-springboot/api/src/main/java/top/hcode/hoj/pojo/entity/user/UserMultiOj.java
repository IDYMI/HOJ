package top.hcode.hoj.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UserRemoteOj对象", description = "")
public class UserMultiOj implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableId(value = "uid", type = IdType.UUID)
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

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
