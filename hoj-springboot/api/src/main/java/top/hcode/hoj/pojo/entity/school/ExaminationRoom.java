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
@ApiModel(value = "ExaminationRoom对象", description = "")
public class ExaminationRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "学校id")
    private Long schoolId;

    @ApiModelProperty(value = "建筑号")
    private String building;

    @ApiModelProperty(value = "房间号")
    private String room;

    @ApiModelProperty(value = "最大行数，默认从0到n-1'")
    private Integer maxCol;

    @ApiModelProperty(value = "最大列数，默认从0到n-1")
    private Integer maxRow;

    @ApiModelProperty(value = "发表者用户名")
    private String author;

    @ApiModelProperty(value = "修改者用户名")
    private String modifiedUser;

    @ApiModelProperty(value = "默认0可用，1不可用")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
