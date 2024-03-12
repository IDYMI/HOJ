package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MultiOjDto {

    private String uid;

    private String username;

    /**
     * 查询 Oj
     */
    private String multiOj;

    /**
     * 查询 Oj 对应的用户名
     */
    private String multiOjUsername;

    /**
     * 当前 ranking
     */
    private Integer ranking;

    /**
     * 最高 ranking
     */
    private Integer maxRanking;

    /**
     * Ac 数
     */
    private Integer resolved;

    /**
     * 实际报错信息
     */
    private String msg;
}