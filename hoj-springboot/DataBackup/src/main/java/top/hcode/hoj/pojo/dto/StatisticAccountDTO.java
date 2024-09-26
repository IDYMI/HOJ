package top.hcode.hoj.pojo.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticAccountDTO {

    /**
     * hdu 用户
     */
    private String hdu;

    /**
     * cf 用户
     */
    private String cf;

    /**
     * vj 用户
     */
    private String vj;

}
