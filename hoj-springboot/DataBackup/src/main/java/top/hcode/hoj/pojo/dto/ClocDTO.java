package top.hcode.hoj.pojo.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClocDTO {

    /**
     * cloc 主机名
     */
    private String clochost;

    /**
     * cloc 端口
     */
    private Integer clocport;

    /**
     * cloc 开始时间
     */
    private String clocstartTime;
}
