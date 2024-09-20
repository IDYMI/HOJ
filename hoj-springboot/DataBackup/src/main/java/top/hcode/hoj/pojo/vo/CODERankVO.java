package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.hcode.hoj.pojo.dto.ClocResultJsonDTO;

import java.io.Serializable;
import java.util.List;

@ApiModel(value = "万码行动数据类CODERankVO", description = "")
@Data
public class CODERankVO implements Serializable {
    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "头衔、称号")
    private String titleName;

    @ApiModelProperty(value = "头衔、称号的颜色")
    private String titleColor;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "专业/班级")
    private String course;

    @ApiModelProperty(value = "做题日期")
    private String time;

    @ApiModelProperty(value = "做题数据")
    private String json;

    @ApiModelProperty(value = "转化后的List数据")
    private List<ClocResultJsonDTO> listJson;

    @ApiModelProperty(value = "代码量")
    private Integer sum;
}