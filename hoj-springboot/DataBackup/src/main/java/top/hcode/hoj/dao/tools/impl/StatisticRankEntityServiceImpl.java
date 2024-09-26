package top.hcode.hoj.dao.tools.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import top.hcode.hoj.dao.tools.StatisticRankEntityService;
import top.hcode.hoj.mapper.StatisticRankMapper;
import top.hcode.hoj.pojo.entity.contest.StatisticRank;

@Service
public class StatisticRankEntityServiceImpl extends ServiceImpl<StatisticRankMapper, StatisticRank>
        implements StatisticRankEntityService {
}