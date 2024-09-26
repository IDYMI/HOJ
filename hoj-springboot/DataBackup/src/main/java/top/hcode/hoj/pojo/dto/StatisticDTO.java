package top.hcode.hoj.pojo.dto;

import java.util.Map;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatisticDTO {

    private Integer limit;

    private Integer currentPage;

    @ApiModelProperty(value = "查询的关键词学校")
    private String keyword;

    @NotBlank(message = "cids不能为空")
    private String cids;

    @ApiModelProperty(value = "包含比赛的比例")
    private String percents;

    @ApiModelProperty(value = "系列比赛的标题")
    private String title;

    @ApiModelProperty(value = "oj对应的账号列表")
    private StatisticAccountDTO statisticAccountDto;

    @ApiModelProperty(value = "用户字典")
    private Map<String, String> data;

    @ApiModelProperty(value = "验证码")
    private String captcha;

    private Boolean isSave;
}
