package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 13:08
 * @Description:
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "用户的代码", description = "")
public class CodeVO {

    @ApiModelProperty(value = "提交id")
    private Long submitId;

    @ApiModelProperty(value = "比赛display_id")
    private String displayId;

    @ApiModelProperty(value = "代码语言")
    private String language;

    @ApiModelProperty(value = "代码")
    private String code;

}