package top.hcode.hoj.dao.tools.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import top.hcode.hoj.dao.tools.StatisticContestEntityService;
import top.hcode.hoj.mapper.StatisticContestMapper;
import top.hcode.hoj.pojo.entity.contest.StatisticContest;

@Service
public class StatisticContestEntityServiceImpl extends ServiceImpl<StatisticContestMapper, StatisticContest>
        implements StatisticContestEntityService {
}