package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExaminationSeatVO {

    private Long id;

    @ApiModelProperty(value = "学校id")
    private Long schoolId;

    @ApiModelProperty(value = "学校名称")
    private String school;

    @ApiModelProperty(value = "比赛名称")
    private String title;

    @ApiModelProperty(value = "比赛id")
    private Long cid;

    @ApiModelProperty(value = "房间id")
    private Long eid;

    @ApiModelProperty(value = "座位id")
    private Long sid;

    @ApiModelProperty(value = "建筑号")
    private String building;

    @ApiModelProperty(value = "房间号")
    private String room;

    @ApiModelProperty(value = "座位的x轴")
    private Integer grow;

    @ApiModelProperty(value = "座位的y轴")
    private Integer gcol;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "考生姓名")
    private String realname;

    @ApiModelProperty(value = "专业/班级")
    private String course;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "考试科目")
    private String subject;

    @ApiModelProperty(value = "座位状态: 0为可选座位，1为选中座位，2为已选座位，3为维修座位，4为该地方无座位")
    private Integer type;
}