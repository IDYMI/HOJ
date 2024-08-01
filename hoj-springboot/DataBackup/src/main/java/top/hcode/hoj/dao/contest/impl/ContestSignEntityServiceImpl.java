package top.hcode.hoj.dao.contest.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import top.hcode.hoj.mapper.ContestSignMapper;
import top.hcode.hoj.pojo.entity.contest.ContestSign;
import top.hcode.hoj.dao.contest.ContestSignEntityService;

@Service
public class ContestSignEntityServiceImpl extends ServiceImpl<ContestSignMapper, ContestSign>
                implements ContestSignEntityService {
}