package top.hcode.hoj.pojo.vo;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClocVO {

    /**
     * uid
     */
    private String uid;

    /**
     * cloc的结果
     */
    private String json;

}
