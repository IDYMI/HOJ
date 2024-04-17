package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExaminationUserInfoVO {

    private Long id;

    @ApiModelProperty(value = "考生姓名")
    private String realname;

    @ApiModelProperty(value = "专业/班级")
    private String course;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "考试科目")
    private String subject;

    @ApiModelProperty(value = "绑定账号")
    private String username;
}