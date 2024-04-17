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
@ApiModel(value = "ContestSeat对象", description = "")
public class ContestSeat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String uid;

    private Long cid;

    @ApiModelProperty(value = "座位Id")
    private Long sid;

    @ApiModelProperty(value = "座位排序")
    private String sortId;

    @ApiModelProperty(value = "比赛名称")
    private String title;

    @ApiModelProperty(value = "考生姓名")
    private String realname;

    @ApiModelProperty(value = "专业/班级")
    private String course;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "学科")
    private String subject;

    @ApiModelProperty(value = "座位状态: 0为可选座位，2为已选座位，3为维修座位")
    private Integer type;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
