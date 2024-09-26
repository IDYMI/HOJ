package top.hcode.hoj.dao.problem.impl;

import top.hcode.hoj.pojo.entity.problem.ProblemDescription;
import top.hcode.hoj.mapper.ProblemDescriptionMapper;
import top.hcode.hoj.dao.problem.ProblemDescriptionEntityService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class ProblemDescriptionServiceImpl extends ServiceImpl<ProblemDescriptionMapper, ProblemDescription>
        implements ProblemDescriptionEntityService {

}
