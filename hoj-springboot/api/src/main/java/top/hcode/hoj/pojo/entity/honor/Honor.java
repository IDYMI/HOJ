package top.hcode.hoj.pojo.entity.honor;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
@ApiModel(value = "Honor对象", description = "荣誉实体")
public class Honor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "荣誉标题")
    private String title;

    @ApiModelProperty(value = "荣誉创建者用户名")
    private String author;

    @ApiModelProperty(value = "荣誉类型：Gold、Silver、Bronze")
    private String type;

    @ApiModelProperty(value = "荣誉的等级（全球赛，国赛，省赛，校赛）")
    private String level;

    @ApiModelProperty(value = "荣誉的时间")
    private Date date;

    @ApiModelProperty(value = "荣誉的队员")
    private String teamMember;

    @ApiModelProperty(value = "是否可用")
    private Boolean status;

    @ApiModelProperty(value = "是否为团队内的荣誉")
    private Boolean isGroup;

    @ApiModelProperty(value = "团队ID")
    private Long gid;

    @ApiModelProperty(value = "跳转链接")
    private String link;

    @ApiModelProperty(value = "荣誉简介")
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}