package top.hcode.hoj.pojo.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "比赛ICPC Tool Online滚榜所需判题信息")
@Data
public class ContestResolverOnlineTeamVO {

    @ApiModelProperty(value = "队伍编号")
    private String team_id;

    @ApiModelProperty(value = "队伍名称")
    private String name;

    @ApiModelProperty(value = "组织（国内默认为 School)")
    private String organization;

    @ApiModelProperty(value = "队伍内人员")
    private List<String> members;

    @ApiModelProperty(value = "正式队伍")
    private Integer official;

    @ApiModelProperty(value = "打星队伍")
    private Boolean unofficial;

    @ApiModelProperty(value = "女队")
    private Boolean girl;
}
