package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 13:08
 * @Description:
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "用户的代码", description = "")
public class SubmissionVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "具体的提交列表")
    private List<CodeVO> codeList;

}