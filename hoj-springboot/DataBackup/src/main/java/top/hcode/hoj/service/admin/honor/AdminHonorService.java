package top.hcode.hoj.service.admin.honor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.honor.Honor;

public interface AdminHonorService {

    public CommonResult<IPage<Honor>> getHonorList(Integer limit, Integer currentPage, String keyword, String type,
            String year);

    public CommonResult<Honor> getHonor(Long hid);

    public CommonResult<Void> deleteHonor(Long hid);

    public CommonResult<Void> addHonor(Honor honor);

    public CommonResult<Void> updateHonor(Honor honor);

    public CommonResult<Void> changeHonorStatus(Long hid, String author, Boolean status);
}
