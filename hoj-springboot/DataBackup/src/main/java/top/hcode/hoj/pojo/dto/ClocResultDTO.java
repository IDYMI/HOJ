package top.hcode.hoj.pojo.dto;

import java.util.List;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClocResultDTO {

    /**
     * 总代码量
     */
    private Long sum;

    /**
     * 代码信息
     */
    private List<ClocResultJsonDTO> clocResultJsonDTo;

}
