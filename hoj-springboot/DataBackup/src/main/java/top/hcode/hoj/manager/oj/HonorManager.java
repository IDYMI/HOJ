package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.stream.Collectors;
import java.util.*;
import javax.annotation.Resource;

import top.hcode.hoj.dao.honor.HonorEntityService;
import top.hcode.hoj.mapper.HonorMapper;
import top.hcode.hoj.pojo.entity.honor.Honor;
import top.hcode.hoj.pojo.vo.HonorVO;

import org.springframework.stereotype.Component;

@Component
public class HonorManager {

    @Resource
    private HonorMapper honorMapper;

    @Resource
    private HonorEntityService honorEntityService;

    // 定义 level 的排序优先级
    private static final Map<String, Integer> LEVEL_ORDER = new HashMap<>();
    // 定义 type 的排序优先级
    private static final Map<String, Integer> TYPE_ORDER = new HashMap<>();

    static {
        TYPE_ORDER.put("Gold", 1);
        TYPE_ORDER.put("Silver", 2);
        TYPE_ORDER.put("Bronze", 3);
        LEVEL_ORDER.put("全球赛", 1);
        LEVEL_ORDER.put("国赛", 2);
        LEVEL_ORDER.put("省赛", 3);
        LEVEL_ORDER.put("校赛", 4);
    }

    /**
     * @param limit
     * @param currentPage
     * @param keyword
     * @MethodName getHonorList
     * @Description 获取荣誉列表，可根据关键词过滤
     * @Return
     */
    public IPage<HonorVO> getHonorList(Integer limit, Integer currentPage, String keyword) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 2;

        QueryWrapper<Honor> honorQueryWrapper = new QueryWrapper<>();
        // 基本条件
        honorQueryWrapper.isNull("gid")
                .eq("is_group", 0)
                .eq("status", true);

        Optional.ofNullable(keyword).filter(k -> !k.isEmpty()).ifPresent(k -> honorQueryWrapper.and(wrapper -> wrapper
                .like("title", k)
                .or()
                .like("team_member", k)));

        List<Honor> honorList = honorEntityService.list(honorQueryWrapper);

        // 使用Map按年份分组
        Map<String, List<Honor>> honorMap = honorList.stream()
                .collect(Collectors.groupingBy(honor -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(honor.getDate());
                    return String.valueOf(calendar.get(Calendar.YEAR));
                }));

        // 创建HonorVO列表
        List<HonorVO> honorVoList = new ArrayList<>();

        for (Map.Entry<String, List<Honor>> entry : honorMap.entrySet()) {
            HonorVO honorVO = new HonorVO();
            honorVO.setYear(entry.getKey());
            honorVO.setHonor(entry.getValue());
            honorVoList.add(honorVO);
        }

        honorVoList = sortHonorVoList(honorVoList);

        // 按照Year从大到小排序
        honorVoList.sort((o1, o2) -> Integer.compare(Integer.parseInt(o2.getYear()), Integer.parseInt(o1.getYear())));

        // 新建分页
        Page<HonorVO> page = new Page<>(currentPage, limit);

        // 分页处理
        int totalSize = honorVoList.size();
        int startIndex = (currentPage - 1) * limit;
        int endIndex = Math.min(startIndex + limit, totalSize);

        List<HonorVO> paginatedList = honorVoList.subList(startIndex, endIndex);

        page.setRecords(paginatedList);
        page.setTotal(totalSize);
        return page;
    }

    /**
     * @MethodName sortHonorVoList
     * @Description 对 HonorVO 列表中的 Honor 进行排序
     * @Return
     */
    public static List<HonorVO> sortHonorVoList(List<HonorVO> honorVoList) {
        // 遍历每个 HonorVO
        for (HonorVO honorVO : honorVoList) {
            // 对 Honor 列表按 type 和 date 排序
            honorVO.getHonor().sort((o1, o2) -> {
                // 按 type 的优先级排序
                int levelCompare = Integer.compare(
                        LEVEL_ORDER.getOrDefault(o1.getLevel(), Integer.MIN_VALUE),
                        LEVEL_ORDER.getOrDefault(o2.getLevel(), Integer.MIN_VALUE));

                // 如果 type 相同，则按 date 从大到小排序
                if (levelCompare == 0) {

                    // 按 type 的优先级排序
                    int typeCompare = Integer.compare(
                            TYPE_ORDER.getOrDefault(o1.getType(), Integer.MIN_VALUE),
                            TYPE_ORDER.getOrDefault(o2.getType(), Integer.MIN_VALUE));

                    if (typeCompare == 0) {
                        return o2.getDate().compareTo(o1.getDate()); // 降序
                    }

                    return typeCompare;
                }

                return levelCompare; // 按 type 的优先级排序
            });
        }
        return honorVoList;
    }
}