package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import top.hcode.hoj.pojo.dto.ExaminationRoomDTO;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

@Data
@Accessors(chain = true)
public class ExaminationRoomVO {

    @ApiModelProperty(value = "考场Id")
    private Long eid;

    @ApiModelProperty(value = "学校Id")
    private Long schoolId;

    @ApiModelProperty(value = "学校名称")
    private String school;

    @ApiModelProperty(value = "比赛标题")
    private String title;

    @ApiModelProperty(value = "教学楼")
    private String building;

    @ApiModelProperty(value = "教室")
    private String room;

    @ApiModelProperty(value = "发表者用户名")
    private String author;

    @ApiModelProperty(value = "最大行数，默认从0到n-1'")
    private Integer maxCol;

    @ApiModelProperty(value = "最大列数，默认从0到n-1")
    private Integer maxRow;

    @ApiModelProperty(value = "可用座位数量")
    private Integer count;

    @ApiModelProperty(value = "已用座位数量")
    private Integer used;

    @ApiModelProperty(value = "座位表")
    private List<ExaminationRoomDTO> seatList;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}