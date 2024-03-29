package top.hcode.hoj.crawler.problem;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.utils.CodeForcesUtils;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.JsoupUtils;

import java.io.File;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import java.util.stream.Collectors;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class QOJProblemStrategy extends ProblemStrategy {

	public static final String JUDGE_NAME = "QOJ";
	public static final String HOST = "https://qoj.ac";
	public static final String LOGIN_URL = "/login";
	public static final String PROBLEM_URL = "/problem/%s";
	public static final String CONTEST_PROBLEM_URL = "/contest/%s/problem/%s";

	public static String csrfToken = "";
	public static List<HttpCookie> cookies = new ArrayList<>();
	public static Map<String, String> headers = MapUtil
			.builder(new HashMap<String, String>())
			.put("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
			.map();

	/**
	 * @param problemId String的原因是因为某些题库题号不是纯数字
	 * @param author    导入该题目的管理员用户名
	 * @return 返回Problem对象
	 * @throws Exception
	 */
	@Override
	public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {

		Problem info = new Problem();

		info.setProblemId(JUDGE_NAME + "-" + problemId);

		String contestId = "0";
		String[] arr = problemId.split("_");

		if (arr.length == 2) {
			contestId = arr[0];
			problemId = arr[1];
		}

		// 验证题号是否符合规范
		if (!problemId.matches("[0-9]\\d*") || !contestId.matches("[0-9]\\d*")) {
			throw new IllegalArgumentException("[QOJ]: Incorrect problem id format!");
		}

		String url = null;
		if (!contestId.equals("0")) {
			url = String.format(HOST + CONTEST_PROBLEM_URL, contestId, problemId);
		} else {
			url = String.format(HOST + PROBLEM_URL, problemId);
		}

		String cookie_ = cookies.stream()
				.map(cookie -> cookie.getName() + "=" + cookie.getValue())
				.collect(Collectors.joining("; ", "", ";"));

		headers.put("_token", csrfToken);
		headers.put("Cookie", cookie_);

		Connection connection = JsoupUtils.getConnectionFromUrl(url, null, headers, false);
		Document document = JsoupUtils.getDocument(connection, null);
		String html = document.html();

		int statusCode = connection.response().statusCode();
		if (statusCode == 404) {
			throw new IllegalArgumentException("[QOJ]: Don't have problem id!");
		}

		info.setTitle(getLastLine(ReUtil.get("text-center\">([\\s\\S]*?)</h1>", html, 1).trim()));
		info.setTimeLimit(
				(int) Math.round(Double.parseDouble(
						ReUtil.get("<span class=.*?>Time Limit:\\s*(\\d*\\.?\\d*)\\s*s\\s*</span>", html, 1))) * 1000);
		info.setMemoryLimit(
				(int) Math.round(Double.parseDouble(
						ReUtil.get("<span class=.*?>Memory Limit:\\s*(\\d*)\\s*MB\\s*</span>",
								html, 1))));

		String score = ReUtil.get("<span class=.*?>Total points:\\s*(\\d*)\\s*</span>",
				html, 1);

		info.setIoScore(score != null ? Integer.parseInt(score) : null);

		// 判断是否为pdf文档
		String uri = HtmlUtil.unescape(ReUtil.get("<iframe src=\"([\\s\\S]*?)\"", html, 1));

		if (uri != null) {
			StringBuilder pdf_description = new StringBuilder();
			String fileName = IdUtil.fastSimpleUUID() + ".pdf";
			String filePath = Constants.File.PROBLEM_FILE_FOLDER.getPath() + File.separator + fileName;

			CodeForcesUtils.downloadPDF(HOST + uri, filePath);
			String pdfURI = Constants.File.FILE_API.getPath() + fileName;

			String[] split = uri.split("/");
			String fileRealName = split[split.length - 1];
			pdf_description.append("<p><a href=\"")
					.append(pdfURI).append("\">")
					.append(fileRealName)
					.append("</a></p>");

			info.setDescription(pdf_description.toString().trim());
		} else {
			String description = ReUtil
					.get("<article class=\"uoj-article top-buffer-md\">([\\s\\S]*?)</article>", html,
							1);
			info.setDescription(
					"<pp>" + HtmlUtil.unescape(description.trim()).replaceAll("src=\"../../", "src=\"" + HOST + "/")
							.replaceAll("(?<=\\>)\\s+(?=\\<)", ""));
		}

		info.setIsRemote(true)
				.setSource(String.format(
						"<a style='color:#1A5CC8' href='%s'>%s</a>",
						url, problemId, JUDGE_NAME + "-" + problemId))
				.setType(info.getIsFileIO() != null ? 1 : 0)
				.setAuth(1)
				.setAuthor(author)
				.setOpenCaseResult(false)
				.setIsRemoveEndBlank(false)
				.setIsGroup(false)
				.setDifficulty(1); // 默认为简单

		return new RemoteProblemInfo()
				.setProblem(info)
				.setTagList(null)
				.setRemoteOJ(Constants.RemoteOJ.QOJ);

	}

	public void login(String username, String password) {
		// 清除当前线程的cookies缓存
		HttpRequest.getCookieManager().getCookieStore().removeAll();

		String _token = null;

		HttpResponse response = HttpRequest.get(HOST + LOGIN_URL)
				.header("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
				.timeout(5000)
				.execute();

		String html = response.body();
		Pattern pattern = Pattern.compile("\\b_token\\s*:\\s*\"([^\"]*)\"");
		Matcher matcher = pattern.matcher(html);

		if (matcher.find()) {
			_token = matcher.group(1);
			csrfToken = _token;
		}

		String token = String.format(
				"_token=%s&login=&username=%s&password=%s", _token, username, SecureUtil.md5(password));

		String cookie_ = response.getCookies().stream()
				.map(cookie -> cookie.getName() + "=" + cookie.getValue())
				.collect(Collectors.joining("; ", "", ";"));

		// 发送POST请求
		HttpResponse response2 = HttpRequest.post(HOST + LOGIN_URL)
				.header("authority", "qoj.ac")
				.header("Cookie", cookie_)
				.header("x-requested-with", "XMLHttpRequest")
				.body(token)
				.execute();

		// 解析响应内容
		String status = response2.body();

		cookies = response2.getCookies();

		if (!status.equals("ok") || response2.getStatus() != 200) {
			throw new RuntimeException(
					"[QOJ] Failed to login! The possible cause is connection failure, and the returned status code is "
							+ response2.getStatus() + ", and status is " + status);
		}
	}

	@Override
	public RemoteProblemInfo getProblemInfoByLogin(String problemId, String author, String username, String password)
			throws Exception {
		login(username, password);
		if (!CollectionUtils.isEmpty(cookies) && !StringUtils.isEmpty(csrfToken)) {
			return this.getProblemInfo(problemId, author);
		} else {
			return null;
		}
	}

	public static String getLastLine(String input) {
		Pattern pattern = Pattern.compile("\\s*\\.\\s*(.*?)$");
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(1) : "";
	}
}
