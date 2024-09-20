package top.hcode.hoj.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.apache.commons.lang.time.DateFormatUtils;
import org.json.JSONObject;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import top.hcode.hoj.config.NacosSwitchConfig;
import top.hcode.hoj.config.WebConfig;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.user.UserClocEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.manager.file.ContestFileManager;
import top.hcode.hoj.pojo.dto.ClocResultDTO;
import top.hcode.hoj.pojo.dto.ClocResultJsonDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.user.UserCloc;
import top.hcode.hoj.pojo.vo.ClocVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.*;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@RefreshScope
@Slf4j(topic = "hoj")
public class ClocUtils {

    /**
     * cloc github地址：
     * https://github.com/AlDanial/cloc
     *
     */

    // TODO 查询时间级别限制为小时

    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private UserClocEntityService userClocEntityService;

    @Resource
    private ContestFileManager contestFileManager;

    @Autowired
    private UserRoleEntityService userRoleEntityService;

    /**
     * 统计用户在时间段内的code记录
     *
     * @param uidList   选择查询的uid列表
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param isSave    是否保存
     * @return 该用户统计代码后的各代码代码行数
     */
    public List<UserCloc> getUserCodeLines(List<String> uidList, Date startTime, Date endTime, Boolean isSave)
            throws StatusFailException, StatusNotFoundException, IOException {

        // 起止日期为空，则设置为活动开始时间
        if (startTime == null) {
            WebConfig webConfig = nacosSwitchConfig.getWebConfig();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                startTime = sdf.parse(webConfig.getClocstartTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // 截止日期为空，则设置为当前时间
        if (endTime == null) {
            // 获取当天零点的 LocalDate
            LocalDate localDate = LocalDate.now();

            // 将 LocalDate 转换为当天零点的 Date
            endTime = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        Map<String, Set<String>> codePathList = getUserCodeList(uidList, startTime, endTime);

        List<UserCloc> insertUserClocList = new ArrayList<>();

        // 发送指令
        if (!CollectionUtils.isEmpty(codePathList)) {
            List<ClocVO> clocResult = sendClocCmd(codePathList, startTime, endTime);

            // 今日时间，每个小时最多生成一次
            String day = DateFormatUtils.format(new Date(), "yyyy-MM-dd-HH");

            // 保持到数据库
            for (ClocVO clocVO : clocResult) {
                String uid = clocVO.getUid();

                QueryWrapper<UserCloc> userClocQueryWrapper = new QueryWrapper<>();
                userClocQueryWrapper.eq("uid", uid).orderByDesc("time").last("limit 1");
                UserCloc userCloc = userClocEntityService.getOne(userClocQueryWrapper);

                ClocResultDTO clocResultDTO = getClocInfo(clocVO.getJson());

                if (clocResultDTO == null) {
                    throw new StatusFailException("Cloc生成结果失败！");
                }

                cn.hutool.json.JSONObject codeConfigJson = new cn.hutool.json.JSONObject();
                codeConfigJson.set("config", clocResultDTO.getClocResultJsonDTo());

                String codeConfigJsonStr = codeConfigJson.toString();

                if (isSave) {
                    Long sum = clocResultDTO.getSum();

                    // 第一次生成或者一小时内发生变化，生成一次
                    if (userCloc == null
                            || (!userCloc.getTime().equals(day) && sum != userCloc.getSum())) {
                        addUserClocList(uid, day, codeConfigJsonStr, clocResultDTO, insertUserClocList);
                    }
                } else {
                    addUserClocList(uid, day, codeConfigJsonStr, clocResultDTO, insertUserClocList);
                }
            }

            if (isSave && !CollectionUtils.isEmpty(insertUserClocList)) {
                Boolean isOk = userClocEntityService.saveBatch(insertUserClocList);
                if (!isOk) {
                    throw new StatusFailException("保存失败！");
                }
            }
        }

        return insertUserClocList;
    }

    /**
     *
     * @param uidList   选择查询的uid列表
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 返回查询的uid 对应的文件列表字典
     */
    public Map<String, Set<String>> getUserCodeList(List<String> uidList, Date startTime, Date endTime)
            throws StatusFailException {
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();

        judgeQueryWrapper.select("username", "uid", "language", "code", "submit_time", "status", "submit_id")
                .in(uidList.size() > 0, "uid", uidList)
                .eq("status", 0) // 正确代码
                .ge("submit_time", startTime) // 在开始时间之后
                .le(endTime != null, "submit_time", endTime) // 在结束时间之前
                .orderByDesc("uid") // 将用户排序
                .orderByDesc("submit_time"); // 将提交时间降序

        List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);

        if (judgeList == null) {
            throw new StatusFailException("获取评测记录为空！");
        }

        // 保存到本地文件
        String workDir = Constants.File.CODE_FOLDER.getPath();

        // 使用线程池
        ExecutorService threadPool = new ThreadPoolExecutor(
                2, // 核心线程数
                4, // 最大线程数。最多几个线程并发。
                3, // 当非核心线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS, // 结束线程时间单位
                new LinkedBlockingDeque<>(200), // 阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());// 队列满了，尝试去和最早的竞争，也不会抛出异常！

        List<FutureTask<Void>> futureTasks = new ArrayList<>();

        // 创建字典
        Map<String, Set<String>> dictionary = new HashMap<>();

        for (Judge judge : judgeList) {
            futureTasks.add(new FutureTask<>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    String uid = judge.getUid();

                    if (StringUtils.isEmpty(uid)) {
                        return null;
                    }

                    // 使用 DateFormatUtils 格式化时间戳为字符串
                    String time = DateFormatUtils.format(judge.getSubmitTime(), "yyyy-MM-dd-HH");
                    // 文件后缀
                    String suffix = contestFileManager.languageToFileSuffix(judge.getLanguage().toLowerCase());
                    // 创建用户对应的文件夹
                    String usercodeWorkDir = workDir + File.separator + time + File.separator + uid;

                    addElement(dictionary, uid, time);

                    FileUtil.mkdir(new File(usercodeWorkDir));

                    String outputName = usercodeWorkDir + File.separator + judge.getSubmitId() + "." + suffix;

                    File file = new File(outputName);

                    if (!FileUtil.exist(file)) { // 存在则说明，该提交记录已经保存
                        FileWriter outfileWriter = new FileWriter(file);
                        outfileWriter.write(judge.getCode());

                        FileUtil.copy(new File(usercodeWorkDir), new File(workDir), true);
                    }

                    return null;
                }
            }));
        }

        // 提交到线程池进行执行
        for (FutureTask<Void> futureTask : futureTasks) {
            threadPool.submit(futureTask);
        }
        // 所有任务执行完成且等待队列中也无任务关闭线程池
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        // 阻塞主线程, 直至线程池关闭
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("线程池异常--------------->", e);
        }

        return dictionary;
    }

    public List<ClocVO> sendClocCmd(Map<String, Set<String>> dictionary, Date startTime, Date endTime)
            throws IOException {

        // 本地文件code保存路径对应的docker路径
        String codeDir = Constants.File.DOCKER_CODE_FOLDE.getPath();

        WebConfig webConfig = nacosSwitchConfig.getWebConfig();

        // 使用线程池
        ExecutorService threadPool = new ThreadPoolExecutor(
                2, // 核心线程数
                4, // 最大线程数。最多几个线程并发。
                3, // 当非核心线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS, // 结束线程时间单位
                new LinkedBlockingDeque<>(200), // 阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());// 队列满了，尝试去和最早的竞争，也不会抛出异常！

        List<FutureTask<Void>> futureTasks = new ArrayList<>();

        List<ClocVO> resultList = new ArrayList<>();

        // 遍历字典
        for (Map.Entry<String, Set<String>> entry : dictionary.entrySet()) {
            String uid = entry.getKey();
            Set<String> times = entry.getValue();

            futureTasks.add(new FutureTask<>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    StringBuilder pathsBuilder = new StringBuilder();

                    for (String time : times) {
                        // 用户对应的文件夹
                        String usercodeWorkDir = codeDir + "/" + time + "/" + uid;
                        pathsBuilder.append(usercodeWorkDir).append(" ");
                    }

                    // 转换为字符串
                    String paths = pathsBuilder.toString().trim();

                    // 用cloc统计代码量返回json结果
                    try {

                        if (StringUtils.isEmpty(webConfig.getClochost())) {
                            throw new StatusNotFoundException("cloc 服务未配置！");
                        }

                        // 构建命令并执行
                        String cmd = getCmdCommand(paths, null);
                        log.info("Cloc cmd: {}", cmd);

                        HttpResponse response = postRequest(webConfig.getClochost(), webConfig.getClocport(), cmd);
                        if (!response.isOk()) {
                            log.error("Create Cloc Error: {}", response.body());
                        }

                        resultList.add(new ClocVO().setUid(uid).setJson(response.body()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("Cloc Error: {}", e.getMessage());
                    }
                    return null;
                }
            }));
        }

        // 提交到线程池进行执行
        for (FutureTask<Void> futureTask : futureTasks) {
            threadPool.submit(futureTask);
        }
        // 所有任务执行完成且等待队列中也无任务关闭线程池
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        // 阻塞主线程, 直至线程池关闭
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("线程池异常--------------->", e);
        }

        return resultList;
    }

    private void addUserClocList(String uid, String day, String codeConfigJsonStr, ClocResultDTO clocResultDTO,
            List<UserCloc> insertUserClocList) {
        UserRolesVO userRoles = userRoleEntityService.getUserRoles(uid, null);
        insertUserClocList.add(new UserCloc()
                .setUid(uid)
                .setUsername(userRoles.getUsername())
                .setRealname(userRoles.getRealname())
                .setTime(day)
                .setJson(codeConfigJsonStr)
                .setSum(clocResultDTO.getSum()));
    }

    public static String getCmdCommand(String codePath, String destPath) {
        StringBuilder cmd = new StringBuilder();

        cmd.append("cloc");
        cmd.append(" ");
        cmd.append(codePath);

        if (destPath != null) {
            cmd.append(" --out=" + destPath);
            cmd.append(" --csv");
        } else {
            cmd.append(" --json");
        }

        return cmd.toString();
    }

    public ClocResultDTO getClocInfo(String cloc_json) {
        // 将 JSON 字符串解析为 JSONObject
        JSONObject jsonObject = new JSONObject(cloc_json);

        // 统计代码量
        Long sum = 0L;

        // 提取除了headers以外的数据
        List<ClocResultJsonDTO> json_results = new ArrayList<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject languageData = jsonObject.getJSONObject(key);
            if (key.equals("SUM")) {
                sum = languageData.getLong("code");
            } else if (!key.equals("header")) {
                int nFiles = languageData.getInt("nFiles");
                int blank = languageData.getInt("blank");
                int comment = languageData.getInt("comment");
                int code = languageData.getInt("code");
                ClocResultJsonDTO result = new ClocResultJsonDTO()
                        .setLanguage(key)
                        .setCommit(nFiles)
                        .setBlank(blank)
                        .setComment(comment)
                        .setCode(code);
                json_results.add(result);
            }
        }

        ClocResultDTO clocResultDTo = new ClocResultDTO().setSum(sum).setClocResultJsonDTo(json_results);

        return clocResultDTo;
    }

    public static HttpResponse postRequest(String url, Integer port, String cmd) throws IOException {
        // 默认远程服务器为https协议
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        if (port != null) {
            url += ":" + port;
        }

        log.info("Create Cloc url: {}", url);
        HttpRequest httpRequest = HttpRequest.post(url)
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .form("command", cmd);

        HttpResponse response = httpRequest.execute();
        return response;
    }

    /**
     * 向字典中的指定键的集合中添加元素。如果集合不存在，则创建它。
     *
     * @param dictionary 字典
     * @param key        键
     * @param value      要添加的元素
     */
    public static void addElement(Map<String, Set<String>> dictionary, String key, String value) {
        // 获取键对应的集合
        Set<String> set = dictionary.get(key);

        // 如果集合不存在，创建一个新的集合
        if (set == null) {
            set = new HashSet<>();
            dictionary.put(key, set);
        }

        // 向集合中添加元素
        set.add(value);
    }

}