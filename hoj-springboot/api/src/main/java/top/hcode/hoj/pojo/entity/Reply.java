package top.hcode.hoj.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/5 19:03
 * @Description:
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Reply对象", description="")
public class Reply {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "评论id")
    private Integer commentId;

    @ApiModelProperty(value = "回复评论者id")
    private String fromUid;

    @ApiModelProperty(value = "回复评论者用户名")
    private String fromName;

    @ApiModelProperty(value = "回复评论组头像地址")
    private String fromAvatar;

    @ApiModelProperty(value = "回复评论者角色")
    private String fromRole;

    @ApiModelProperty(value = "被回复的用户id")
    private String toUid;

    @ApiModelProperty(value = "被回复的用户名")
    private String toName;

    @ApiModelProperty(value = "被回复的用户头像地址")
    private String toAvatar;

    @ApiModelProperty(value = "回复的内容")
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}