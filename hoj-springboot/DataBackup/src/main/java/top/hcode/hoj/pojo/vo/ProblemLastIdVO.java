package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ProblemLastIdVO {

    @ApiModelProperty(value = "题目id")
    private String problemLastId;
}