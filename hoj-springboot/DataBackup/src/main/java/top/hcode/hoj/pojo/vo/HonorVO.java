package top.hcode.hoj.pojo.vo;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import top.hcode.hoj.pojo.entity.honor.Honor;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HonorVO {

    @ApiModelProperty(value = "年份")
    private String year;

    @ApiModelProperty(value = "信息")
    private List<Honor> honor;

}
