package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "比赛ICPC Tool Online滚榜所需判题信息")
@Data
public class ContestResolverOnlineRunVO {

    @ApiModelProperty(value = "队伍编号")
    private Long team_id;

    @ApiModelProperty(value = "题目编号")
    private Integer problem_id;

    @ApiModelProperty(value = "提交时间，为提交时间减去比赛时间")
    private Long timestamp;

    @ApiModelProperty(value = "状态")
    private String status;

}
