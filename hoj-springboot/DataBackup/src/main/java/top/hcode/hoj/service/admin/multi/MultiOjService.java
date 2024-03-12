package top.hcode.hoj.service.admin.multi;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.MultiOjDto;

public interface MultiOjService {

    public CommonResult<MultiOjDto> getMultiOjInfo(String username, String globalOj, String multiOjUsername);

}
