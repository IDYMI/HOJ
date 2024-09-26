package top.hcode.hoj.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.druid.util.StringUtils;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.config.NacosSwitchConfig;
import top.hcode.hoj.config.WebConfig;
import top.hcode.hoj.pojo.dto.ProblemRes;

@Component
@RefreshScope
@Slf4j(topic = "hoj")
public class HtmlToPdfUtils {

    /**
     *
     * Pandoc 官方地址:
     * https://pandoc.org/
     *
     * 1. 查看系统中的字体：
     * fc-list :lang=zh
     *
     * 2. 检查系统中是否包含 Courier New 字体：
     * fc-list | grep "Courier New"
     *
     * 问题：原本 Pandoc 转换生成的表格不显示表格中的横线。
     * 解决方案：将测试样例中的内容先转换为 TeX 格式，再合成 template.tex 模板进行 PDF 转换。
     *
     */

    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

    private String RootUrl;
    private String Host;
    private Boolean EC;

    /**
     * html字符串转pdf
     *
     * @param problem 题目信息
     * @return 转换成功返回保存的文件名称
     */
    public String convertByHtml(ProblemRes problem) throws StatusNotFoundException, IOException {
        WebConfig webConfig = nacosSwitchConfig.getWebConfig();

        String host = webConfig.getHtmltopdfHost();
        Integer port = webConfig.getHtmltopdfPort();
        this.RootUrl = webConfig.getBaseUrl().replaceAll("/$", ""); // 去除末尾的 "/"
        this.Host = (host.startsWith("http") ? host : "https://" + host) + (port != null ? ":" + port : "");
        this.EC = webConfig.getHtmltopdfEc();

        String fileName = getProblemDescriptionName(problem.getPdfDescription());
        String html = problem.getHtml();

        if (StringUtils.isEmpty(webConfig.getHtmltopdfHost())) {
            throw new StatusNotFoundException("htmltopdf 服务未配置！");
        }

        if (fileName == null) {
            fileName = IdUtil.fastSimpleUUID();
        }

        problem.setPdfDescription(fileName);

        if (html == null) {
            // 生成保存 HTML 题面
            saveHtmlDetails(problem);
        }

        // 构建命令并执行
        HttpResponse response = savePdfDetails(problem);

        String workspace = Constants.File.PROBLEM_FILE_FOLDER.getPath() + File.separator + fileName + ".pdf";
        // 创建一个 File 对象
        File file = new File(workspace);

        if (!response.isOk() || !file.exists()) {
            log.error("Problem: {" + problem.getTitle() + "}, Create HTML Error: {" + response.body() + "}");
            throw new IOException("PDF题面保存失败！");
        }

        return fileName;
    }

    /**
     * 保存 HTML 题面
     *
     * @param problem 题目
     */
    public void saveHtmlDetails(ProblemRes problem) throws IOException {
        String fileName = getProblemDescriptionName(problem.getPdfDescription());

        // docker 对应的wkhtmltopdf默认目录
        String workspace = Constants.File.DOCKER_PROBLEM_FILE_FOLDER.getPath() + "/";

        HttpRequest httpRequest = HttpRequest.post(Host + "/html");

        // 将标题转化为比赛标题
        String title = problem.getTitle();

        httpRequest.header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .form("input_path", workspace + fileName + ".html")
                .form("timeLimit", problem.getTimeLimit())
                .form("memoryLimit", problem.getMemoryLimit())
                .form("title", title)
                .form("description", convertToMarkdown(problem.getDescription()))
                .form("input", convertToMarkdown(problem.getInput()))
                .form("output", convertToMarkdown(problem.getOutput()))
                .form("hint", convertToMarkdown(problem.getHint()))
                .form("EC", EC);

        List<Map<String, String>> examplesList = stringToExamples(problem.getExamples());

        // 根据类型添加表格样例和选项
        Integer type = problem.getType();
        if ((type == 0 || type == 1) && !CollectionUtils.isEmpty(examplesList)) {
            httpRequest.form("examples", JSONUtil.toJsonStr(examplesList));
        }
        if (type == 2 && !CollectionUtils.isEmpty(examplesList)) {
            httpRequest.form("selections", JSONUtil.toJsonStr(examplesList));
        }

        HttpResponse response = httpRequest.execute();

        if (!response.isOk()) {
            String error = "Problem: {" + problem.getTitle() + "}, Create HTML Error: {" + response.body() + "}";
            log.error(error);
            throw new IOException(error);
        }
    }

    /**
     * 保存 PDF 题面
     *
     * @param problem 题目
     */
    public HttpResponse savePdfDetails(ProblemRes problem) throws IOException {
        String fileName = getProblemDescriptionName(problem.getPdfDescription());

        // docker 对应的wkhtmltopdf默认目录
        String workspace = Constants.File.DOCKER_PROBLEM_FILE_FOLDER.getPath() + "/";

        HttpRequest httpRequest = HttpRequest.post(Host + "/pdf")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .form("input_path", workspace + fileName + ".html")
                .form("output_path", (workspace + fileName + ".pdf"))
                .form("EC", EC);

        HttpResponse response = httpRequest.execute();

        return response;
    }

    /**
     * 将该网站中的 Markdown 的特殊字符转化。
     *
     * @param reloadHtml Markdown 字符串
     * @return 转化后的字符串
     */
    private String convertToMarkdown(String reloadHtml) {

        if (StringUtils.isEmpty(reloadHtml)) {
            return null;
        }

        // 提取所有 $...$ 包围的部分，替换为占位符
        Map<String, String> placeholders = new HashMap<>();
        Matcher matcher = Pattern.compile("\\$(.*?)\\$").matcher(reloadHtml);
        int i = 0;
        while (matcher.find()) {
            String key = "__PLACEHOLDER_" + i++ + "__";
            placeholders.put(key, matcher.group(0)); // 保存 $...$ 部分
            reloadHtml = reloadHtml.replace(matcher.group(0), key); // 替换为占位符
        }

        // 处理加粗、斜体、标题、下划线、删除线、标记、上角标、下角标
        reloadHtml = reloadHtml
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>") // 加粗
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>") // 斜体
                .replaceAll("\\+\\+(.*?)\\+\\+", "<ins>$1</ins>") // 下划线
                .replaceAll("~\\~(.*?)~\\~", "<s>$1</s>") // 删除线
                .replaceAll("==([^=]*)==", "<mark>$1</mark>") // 标记
                .replaceAll("\\^(.*?)\\^", "<sup>$1</sup>") // 上角标
                .replaceAll("~(.*?)~", "<sub>$1</sub>"); // 下角标

        // 处理居左、居中、居右的容器
        reloadHtml = reloadHtml.replaceAll(
                ":::\\s*hljs-(left|center|right)[\\s\\S]*?(?:<br>\\s*){0,2}([\\s\\S]*?)(?:<br>\\s*){0,2}:::",
                "<div class=\"hljs-$1\"><p>$2</p></div>");

        // 处理引用、有序列表、无序列表
        reloadHtml = reloadHtml
                .replaceAll("^> (.*)$", "<blockquote><p>$1</p></blockquote>") // 引用
                .replaceAll("^(\\d+\\. .*)$", "<ol><li>$1</li></ol>") // 有序列表
                .replaceAll("^(- .*)$", "<ul><li>$1</li></ul>"); // 无序列表

        // 将本域图片加上域名
        reloadHtml = reloadHtml
                .replaceAll("/api/public/img", RootUrl + "/api/public/img") // 本域图片
                .replaceAll("\"//images\\.weserv\\.nl/\\?url=", "\""); // 跨域图片

        // 处理标题
        reloadHtml = convertHeading(reloadHtml);

        // 还原 $...$ 部分
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            reloadHtml = reloadHtml.replace(entry.getKey(), entry.getValue());
        }

        // 处理代码块，去除 ``` 后面的语言声明
        reloadHtml = reloadHtml.replaceAll("(?m)^```\\s*\\w*\\s*[\r\n]+", "```\n");

        // 将 HTML 实体转换为普通字符
        reloadHtml = HtmlUtil.unescape(reloadHtml);

        // 去除两端空白
        return reloadHtml.trim();
    }

    /**
     * 将字符串类型的题面样例转化
     *
     * @param examples 测试用例
     * @return 对应的题目样例列表
     */
    private List<Map<String, String>> stringToExamples(String examples) {
        if (StringUtils.isEmpty(examples)) {
            return null;
        }
        List<Map<String, String>> example_list = new ArrayList<>();

        // 正则表达式，用于匹配 <input> 和 <output> 之间的内容
        String reg = "<input>([\\s\\S]*?)</input><output>([\\s\\S]*?)</output>";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(examples);

        // 查找匹配的部分
        while (matcher.find()) {
            Map<String, String> map = new HashMap<>();
            String input = matcher.group(1);
            if (!input.equals("undefined")) {
                map.put("input", input.replaceAll("\\r?\\n", "<br>") + "<br>"); // 获取 <input> 标签之间的内容
            }
            map.put("output", matcher.group(2).replaceAll("\\r?\\n", "<br>") + "<br>"); // 获取 <output> 标签之间的内容
            example_list.add(map);
        }

        return example_list;
    }

    private String convertHeading(String text) {
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile("^(#{1,5}) (.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // 在当前匹配之前附加文本
            result.append(text, lastEnd, matcher.start());
            int level = matcher.group(1).length();
            result.append(String.format("<h%d>%s</h%d>", level, matcher.group(2), level));
            lastEnd = matcher.end();
        }
        result.append(text.substring(lastEnd));
        return result.toString();
    }

    public String getProblemDescriptionName(String pdfDescription) {
        if (StringUtils.isEmpty(pdfDescription)) {
            return null;
        }

        int lastSlash = pdfDescription.lastIndexOf('/');
        int lastDot = pdfDescription.lastIndexOf('.');

        return (lastDot > lastSlash && lastSlash != -1)
                ? pdfDescription.substring(lastSlash + 1, lastDot)
                : pdfDescription;
    }
}