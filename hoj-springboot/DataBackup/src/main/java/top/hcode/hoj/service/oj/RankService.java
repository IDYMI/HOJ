package top.hcode.hoj.service.oj;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.user.UserCloc;

public interface RankService {

    public CommonResult<IPage> getRankList(Integer limit, Integer currentPage, String searchUser, Integer type);

    public CommonResult<List<UserCloc>> getUserCodeRecord(List<String> uidList, String startTime, String endTime);

}
