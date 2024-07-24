package top.hcode.hoj.validator;

import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.training.TrainingRegisterEntityService;
import top.hcode.hoj.pojo.entity.honor.Honor;

import javax.annotation.Resource;
import java.util.Objects;

@Component
public class HonorValidator {

    @Resource
    private TrainingRegisterEntityService trainingRegisterEntityService;

    @Resource
    private CommonValidator commonValidator;

    public void validateHonor(Honor honor) throws StatusFailException {
        commonValidator.validateContent(honor.getTitle(), "荣誉标题", 500);
        commonValidator.validateContentLength(honor.getDescription(), "荣誉描述", 65535);
        if (!Objects.equals(honor.getType(), "Gold")
                && !Objects.equals(honor.getType(), "Silver")
                && !Objects.equals(honor.getType(), "Bronze")) {
            throw new StatusFailException("荣誉的类型必须为金牌(Gold)、银牌(Silver)、铜牌(Bronze)！");
        }
    }

}