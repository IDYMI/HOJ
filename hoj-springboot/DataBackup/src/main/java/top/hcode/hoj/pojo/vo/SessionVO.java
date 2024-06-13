package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class SessionVO {

    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "ip信息")
    private String ip;

    @ApiModelProperty(value = "网页")
    private String routeName;

    @ApiModelProperty(value = "IP列表")
    private String ipList;

    @ApiModelProperty(value = "生成时间")
    private Date gmtCreate;
}