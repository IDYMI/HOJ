package top.hcode.hoj.pojo.entity.contest;

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
@ApiModel(value = "StatisticRank对象", description = "")
public class StatisticRank implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "系列比赛id")
    private String scid;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户真实姓名")
    private String realname;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "ac题目数")
    private Double ac;

    @ApiModelProperty(value = "总提交数")
    private Integer total;

    @ApiModelProperty(value = "提交总罚时")
    private Double totalTime;

    @ApiModelProperty(value = "排名,排名为-1则为打星队伍")
    @TableField("`rank`")
    private Integer rank;

    @ApiModelProperty(value = "比赛对应的提交信息")
    private String json;

    @ApiModelProperty(value = "是否为外网站数据")
    private Boolean synchronous;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
