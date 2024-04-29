package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.utils.CodeForcesUtils;
import top.hcode.hoj.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class NSWOJProblemStrategy extends ProblemStrategy {

	public static final String JUDGE_NAME = "NSWOJ";
	public static final String HOST = "https://acm.nyist.edu.cn";
	public static final String PROBLEM_URL = "/p/%s";

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

		String url = String.format(HOST + PROBLEM_URL, problemId);

		HttpResponse response = HttpRequest.get(url)
				.execute();

		String html = response.body();

		int statusCode = response.getStatus();
		if (statusCode == 404) {
			throw new IllegalArgumentException("[NSWOJ]: Don't have problem id!");
		}

		JSONObject jsonObject = new JSONObject(html);
		List<String> allTags = new ArrayList<>();

		if (jsonObject.has("pdoc")) {
			JSONObject pdoc = jsonObject.getJSONObject("pdoc");

			info.setTitle(pdoc.getString("title"));

			String content = pdoc.getString("content");
			if (content.startsWith("{") && content.endsWith("}")) {
				// 字符串是一个字典，取第一个键对应的值
				JSONObject contentJson = new JSONObject(content);
				Iterator<String> keys = contentJson.keys();
				if (keys.hasNext()) {
					String firstKey = keys.next();
					String description = contentJson.getString(firstKey);

					if (description.contains("@[pdf]")) {
						info.setDescription(getPdfUrl(description, problemId));
					} else {
						info.setDescription(
								"<pp>" + HtmlUtil.unescape(description.replaceAll("src=\"[../]*", "src=\"" + HOST + "/")
										.replaceAll("(?<=\\>)\\s+(?=\\<)", "")));
					}
				} else {
					// 字典为空，不处理
					info.setDescription("");
				}
			} else {
				String description = content;
				if (description.contains("@[pdf]")) {
					info.setDescription(getPdfUrl(description, problemId));
				} else {
					// 字符串不是一个字典，直接处理
					info.setDescription(
							"<pp>" + HtmlUtil.unescape(description.replaceAll("src=\"[../]*", "src=\"" + HOST + "/")
									.replaceAll("(?<=\\>)\\s+(?=\\<)", "")));
				}
			}

			if (pdoc.has("config")) {
				JSONObject config = pdoc.getJSONObject("config");

				info.setTimeLimit(config.getInt("timeMax"));
				info.setMemoryLimit(config.getInt("memoryMax"));
			}

			// 提取标签列表
			if (pdoc.has("tag")) {
				JSONArray tagsArray = pdoc.getJSONArray("tag");
				for (int i = 0; i < tagsArray.length(); i++) {
					allTags.add(tagsArray.getString(i));
				}
			}

		} else {
			throw new IllegalArgumentException("[NSWOJ] Failed to Reload Html!");
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
				.setDifficulty(0); // 默认为简单

		List<Tag> tagList = new LinkedList<>();
		for (String tmp : allTags) {
			tagList.add(new Tag().setName(tmp.trim()));
		}

		return new RemoteProblemInfo()
				.setProblem(info)
				.setTagList(tagList)
				.setRemoteOJ(Constants.RemoteOJ.NSWOJ);

	}

	public String getPdfUrl(String pdf_str, String problemId) {
		StringBuilder pdf_description = new StringBuilder();

		Pattern pattern = Pattern.compile("\\((.*?)\\)");
		Matcher matcher = pattern.matcher(pdf_str);

		if (matcher.find()) {
			String uri = matcher.group(1).replace("file://", HOST + "/p/" + problemId + "/file/");

			String fileName = IdUtil.fastSimpleUUID() + ".pdf";
			String filePath = Constants.File.PROBLEM_FILE_FOLDER.getPath() + File.separator + fileName;

			CodeForcesUtils.downloadPDF(uri, filePath, false);
			String pdfURI = Constants.File.FILE_API.getPath() + fileName;

			String[] split = uri.split("/");
			String fileRealName = split[split.length - 1];
			pdf_description.append("<p><a href=\"")
					.append(pdfURI).append("\">")
					.append(fileRealName)
					.append("</a></p>");
		}

		return pdf_description.toString();
	}

	@Override
	public RemoteProblemInfo getProblemInfoByLogin(String problemId, String author, String username, String password)
			throws Exception {
		return null;
	}

}
