package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ExaminationRoomDTO {

    private Long id;

    private String title;

    private Integer row;

    private Integer col;

    private Integer type;

    private String realname;

    private String course;

    private String number;

    private String username;

}