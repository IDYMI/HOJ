package top.hcode.hoj.pojo.entity.problem;

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
@ApiModel(value = "ProblemDescription对象", description = "")
public class ProblemDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long pid;

    @ApiModelProperty(value = "题目")
    private String title;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "输入描述")
    private String input;

    @ApiModelProperty(value = "输出描述")
    private String output;

    @ApiModelProperty(value = "题面样例")
    private String examples;

    @ApiModelProperty(value = "题目来源（vj判题时例如HDU-1000的链接）")
    private String source;

    @ApiModelProperty(value = "备注,提醒")
    private String hint;

    @ApiModelProperty(value = "编号，升序")
    @TableField("`rank`")
    private Integer rank;

    @ApiModelProperty(value = "创建者用户名")
    private String author;

    @ApiModelProperty(value = "PDF链接")
    private String pdfDescription;

    @ApiModelProperty(value = "题面")
    private String html;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
