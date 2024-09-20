package top.hcode.hoj.pojo.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClocResultJsonDTO {

    /**
     * 语言
     */
    private String language;

    /**
     * 提交数
     */
    private Integer commit;

    /**
     * 空白行
     */
    private Integer blank;

    /**
     * 注释行
     */
    private Integer comment;

    /**
     * 代码行
     */
    private Integer code;

}
