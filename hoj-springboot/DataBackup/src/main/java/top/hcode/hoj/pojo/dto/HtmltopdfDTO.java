package top.hcode.hoj.pojo.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HtmltopdfDTO {

    /**
     * htmltopdf 主机名
     */
    private String htmltopdfHost;

    /**
     * htmltopdf 端口
     */
    private Integer htmltopdfPort;

    /**
     * htmltopdf 默认转化语言，true为英文，false为中文
     */
    private Boolean htmltopdfEc;

}
