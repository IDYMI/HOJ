package top.hcode.hoj.pojo.dto;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import top.hcode.hoj.pojo.vo.ExaminationUserInfoVO;

/**
 *
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ExaminationSeatDTO {

    /**
     * 房间Id（必填）
     */
    List<Long> eidList;

    /**
     * 比赛Id
     */
    Long cid;

    /**
     * 是否为回型排列
     */
    Boolean retroflex;

    /**
     * 是否间隔
     */
    Boolean spaced;

    /**
     * 是否按照考生人数编排座位号，默认为考场能使用的位置递增
     */
    Boolean sorted;

    /**
     * 是否打乱学生座位
     */
    Boolean random;

    /**
     * 考生信息
     */
    List<ExaminationUserInfoVO> studentInfo;

}