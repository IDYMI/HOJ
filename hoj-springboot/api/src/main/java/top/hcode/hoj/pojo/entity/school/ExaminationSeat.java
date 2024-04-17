package top.hcode.hoj.pojo.entity.school;

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
@ApiModel(value = "ExaminationSeat对象", description = "")
public class ExaminationSeat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "考场编号")
    private Long eid;

    @ApiModelProperty(value = "座位的x轴")
    private Integer grow;

    @ApiModelProperty(value = "座位的y轴")
    private Integer gcol;

    @ApiModelProperty(value = "座位状态: 0为可选座位，1为选中座位，2为已选座位，3为维修座位，4为该地方无座位")
    private Integer type;

    @ApiModelProperty(value = "默认0可用，1不可用")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
