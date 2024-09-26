package top.hcode.hoj.pojo.entity.contest;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

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
@ApiModel(value = "StatisticContest对象", description = "")
public class StatisticContest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "系列比赛id")
    private String scid;

    @ApiModelProperty(value = "系列比赛名称")
    private String title;

    @ApiModelProperty(value = "包含比赛的cids")
    private String cids;

    @ApiModelProperty(value = "包含比赛的比例")
    private String percents;

    @ApiModelProperty(value = "是否可见")
    private Boolean visible;

    @ApiModelProperty(value = "作者")
    private String author;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
