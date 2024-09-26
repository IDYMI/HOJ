package top.hcode.hoj.crawler.scraper;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import top.hcode.hoj.pojo.vo.ACMContestRankVO;

public abstract class ScraperStrategy {

	public abstract List<ACMContestRankVO> getScraperInfo(String cid, String keyword,
			Map<String, String> usernameToUidMap)
			throws Exception;

	public abstract List<ACMContestRankVO> getScraperInfoByLogin(String cid, String loginUsername,
			String loginPassword, String captcha, String keyword, Map<String, String> usernameToUidMap)
			throws Exception;

	public static String getOrderNumber(List<String> handles) {
		// 对 handles 进行排序并用 '-' 连接
		return handles.stream()
				.sorted()
				.collect(Collectors.joining("-"));
	}

	public static Date getContestDate(long startTime) {
		if (startTime < 10000000000L) {
			// 秒级时间戳，乘以 1000 转换为毫秒级
			startTime *= 1000;
		}
		// 创建 Date 对象
		Date date = new Date(startTime);
		return date;
	}
}
