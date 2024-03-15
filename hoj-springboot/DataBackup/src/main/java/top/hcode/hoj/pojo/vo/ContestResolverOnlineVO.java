package top.hcode.hoj.pojo.vo;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "比赛ICPC Tool Online滚榜所需提交信息")
@Data
public class ContestResolverOnlineVO {

    @ApiModelProperty(value = "比赛配置")
    private ContestResolverOnlineConfigVO config;

    @ApiModelProperty(value = "提交信息")
    private List<ContestResolverOnlineRunVO> run;

    @ApiModelProperty(value = "队伍信息")
    private Map<String, ContestResolverOnlineTeamVO> team;
}
