package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProblemLastIdVO {

    @ApiModelProperty(value = "题目id")
    private String problemLastId;
}