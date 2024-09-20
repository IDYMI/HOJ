package top.hcode.hoj.config;

import lombok.Data;
import com.alibaba.excel.util.CollectionUtils;
import top.hcode.hoj.pojo.dto.RelatedLinkDTO;
import top.hcode.hoj.utils.IpUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Himit_ZH
 * @Date 2022/10/26
 */
@Data
public class WebConfig {

    // 邮箱配置
    private String emailUsername;

    private String emailPassword;

    private String emailHost;

    private Integer emailPort;

    // htmltopdf配置
    private String htmltopdfHost;

    private Integer htmltopdfPort;

    private Boolean htmltopdfEc = true;

    // cloc配置
    private String clochost;

    private Integer clocport;

    private String clocstartTime;

    private Boolean emailSsl = true;

    private String emailBGImg = "https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/HCODE.png";

    // 网站前端显示配置
    private String baseUrl = "http://" + IpUtils.getServiceIp();

    private String name = "Hcode Online Judge";

    private String shortName = "HOJ";

    private String description;

    private Boolean register = true;

    private String recordName;

    private String recordUrl;

    private String projectName = "HOJ";

    private String projectUrl = "https://gitee.com/himitzh0730/hoj";

    private List<Map<String, String>> related;

    public void setRelatedByList(List<RelatedLinkDTO> relatedList) {
        if (CollectionUtils.isEmpty(relatedList)) {
            return;
        }

        related = relatedList.stream()
                .map(link -> {
                    Map<String, String> linkMap = new HashMap<>();
                    linkMap.put("link", link.getLink());
                    linkMap.put("title", link.getTitle());
                    linkMap.put("iconClass", link.getIconClass());
                    return linkMap;
                })
                .collect(Collectors.toList());
    }

}
