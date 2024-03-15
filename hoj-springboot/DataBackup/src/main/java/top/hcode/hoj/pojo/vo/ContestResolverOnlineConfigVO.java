package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel(value = "比赛ICPC Tool Online滚榜所需比赛信息")
@Data
@Accessors(chain = true)
public class ContestResolverOnlineConfigVO {

    @ApiModelProperty(value = "比赛名称")
    private String contest_name;

    @ApiModelProperty(value = "开始时间")
    private Long start_time;

    @ApiModelProperty(value = "结束时间")
    private Long end_time;

    @ApiModelProperty(value = "封榜时间")
    private Long frozen_time;

    @ApiModelProperty(value = "罚时")
    private Long penalty;

    @ApiModelProperty(value = "组织（国内默认为 School)")
    private String organization;

    @ApiModelProperty(value = "队伍种类")
    private Map<String, String> group;

    @ApiModelProperty(value = "状态展示")
    private Map<String, Integer> status_time_display;

    @ApiModelProperty(value = "题目数量")
    private Integer problem_quantity;

    @ApiModelProperty(value = "题目展示ID")
    private List<String> problem_id;

    @ApiModelProperty(value = "题目对应显示颜色")
    private List<Map<String, String>> balloon_color;

    @ApiModelProperty(value = "模型")
    private Map<String, String> medal;

    public ContestResolverOnlineConfigVO() {
        // 设置默认值
        this.group = new HashMap<>();
        this.group.put("official", "正式队伍");
        this.group.put("unofficial", "打星队伍");
        this.group.put("girl", "女队");

        this.status_time_display = new HashMap<>();
        this.status_time_display.put("correct", 1);
        this.status_time_display.put("incorrect", 1);
        this.status_time_display.put("pending", 1);

        this.medal = new HashMap<>();
    }
}
