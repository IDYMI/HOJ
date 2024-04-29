package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.utils.Constants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.StringUtils;

public class NEWOJProblemStrategy extends ProblemStrategy {

	public static final String JUDGE_NAME = "NEWOJ";
	public static final String HOST = "http://oj.ecustacm.cn";
	public static final String PROBLEM_URL = "/problem.php?id=%s";

	/**
	 * @param problemId String的原因是因为某些题库题号不是纯数字
	 * @param author    导入该题目的管理员用户名
	 * @return 返回Problem对象
	 * @throws Exception
	 */
	public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {

		Problem info = new Problem();

		// 题号仅为正整数
		if (!problemId.matches("[1-9]\\d*")) {
			throw new IllegalArgumentException("[NEWOJ]: Incorrect problem id format!");
		}

		info.setProblemId(JUDGE_NAME + "-" + problemId);

		String url = String.format(HOST + PROBLEM_URL, problemId);

		HttpResponse response = HttpRequest.get(url)
				.execute();

		String html = response.body();

		int statusCode = response.getStatus();
		if (statusCode == 404 || html.contains("题目当前不可用!")) {
			throw new IllegalArgumentException("[NEWOJ]: Don't have problem id!");
		}

		Pattern pattern = Pattern.compile("<h1 class=\"ui header\">\\s*(.*?)\\s*</h1>");
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			String headerContent = matcher.group(1);
			info.setTitle(getLastLine(headerContent));
		}

		Pattern memoryPattern = Pattern.compile("内存限制：(\\d+)\\s*MB");
		Matcher memoryMatcher = memoryPattern.matcher(html);
		if (memoryMatcher.find()) {
			String memoryLimit = memoryMatcher.group(1);
			if (!StringUtils.isEmpty(memoryLimit))
				info.setMemoryLimit((int) Math.round(Double.parseDouble(memoryLimit)));
		}

		Pattern timePattern = Pattern.compile("时间限制：(\\d+)\\s*S");
		Matcher timeMatcher = timePattern.matcher(html);
		if (timeMatcher.find()) {
			String timeLimit = timeMatcher.group(1);
			if (!StringUtils.isEmpty(timeLimit))
				info.setTimeLimit((int) Math.round(Double.parseDouble(timeLimit)) * 1000);
		}

		List<String> headers = ReUtil.findAll("\\<h4 class=\"ui top attached block header\"\\>(.*?)\\</h4\\>", html, 1);
		List<String> contents = ReUtil
				.findAll("\\<div class=\"ui bottom attached segment font-content\"\\>(.*?)\\</div\\>", html, 1);

		List<String> sampleInput = new ArrayList<>();
		List<String> sampleOutput = new ArrayList<>();
		for (int i = 0; i < headers.size() && i < contents.size(); i++) {
			String header = headers.get(i).trim();
			String content = contents.get(i).trim().replaceAll("src=\"/",
					"src=\"" + "//images.weserv.nl/?url=" + HOST + "/");

			if (header.equals("题目描述")) {
				info.setDescription(getNoSplitString(content));
			} else if (header.equals("输入格式")) {
				info.setInput(getNoSplitString(content));
			} else if (header.equals("输出格式")) {
				info.setOutput(getNoSplitString(content));
			} else if (header.startsWith("输入样例") || header.startsWith("输出样例")) {
				String[] examples = ReUtil.findAll("\\<pre.*?\\>(.*?)\\<\\/pre\\>", contents.get(i).trim(), 1).get(0)
						.split("样例\\d+");
				for (String example : examples) {
					String trimmedExample = example.trim().replaceAll("<code class=\"lang-plain\">|</code>|[：:]", "");
					if (!trimmedExample.isEmpty()) {
						if (header.startsWith("输入样例")) {
							sampleInput.add(trimmedExample);
						} else {
							sampleOutput.add(trimmedExample);
						}
					}
				}
			} else if (header.equals("数据范围与提示")) {
				info.setHint(getNoSplitString(content));
			}

		}

		StringBuilder examples = new StringBuilder();

		for (int i = 0; i < sampleInput.size() && i < sampleOutput.size(); i++) {
			examples.append("<input>");
			String exampleInput = sampleInput.get(i).trim();
			examples.append(exampleInput).append("</input>");
			examples.append("<output>");
			String exampleOutput = sampleOutput.get(i).trim();
			examples.append(exampleOutput).append("</output>");
		}

		info.setExamples(examples.toString());

		List<Tag> tagList = new LinkedList<>();
		Pattern pattern2 = Pattern.compile("href=\"problemset\\.php\\?search=([^\"]+)\"");
		Matcher matcher2 = pattern2.matcher(html);
		while (matcher2.find()) {
			String hrefContent = matcher2.group(1);
			tagList.add(new Tag().setName(hrefContent.trim()));
		}
		info.setIsRemote(true)
				.setSource(String.format(
						"<a style='color:#1A5CC8' href='%s'>%s</a>",
						url, JUDGE_NAME + "-" + problemId))
				.setType(0)
				.setAuth(1)
				.setAuthor(author)
				.setOpenCaseResult(false)
				.setIsRemoveEndBlank(false)
				.setIsGroup(false)
				.setDifficulty(1); // 默认为简单

		return new RemoteProblemInfo()
				.setProblem(info)
				.setTagList(tagList)
				.setRemoteOJ(Constants.RemoteOJ.NEWOJ);

	}

	public static String getLastLine(String input) {
		Pattern pattern = Pattern.compile("\\s*\\:\\s*(.*?)$");
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(1) : "";
	}

	private static String getNoSplitString(String content) {
		Document doc = Jsoup.parse(content);
		Element body = doc.body();
		cleanWhitespace(body);

		return HtmlUtil.unescape(body.html().toString().trim().replaceAll("(?<=\\>)\\s+(?=\\<)", ""));
	}

	private static void cleanWhitespace(Element element) {
		for (Element child : element.children()) {
			cleanWhitespace(child);
		}
	}

	@Override
	public RemoteProblemInfo getProblemInfoByLogin(String problemId, String author, String username, String password)
			throws Exception {
		return null;
	}

}
