package top.hcode.hoj.dao.school.impl;

import top.hcode.hoj.pojo.entity.school.ContestSeat;
import top.hcode.hoj.dao.school.ContestSeatService;
import top.hcode.hoj.mapper.ContestSeatMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ContestSeatServiceImpl extends ServiceImpl<ContestSeatMapper, ContestSeat>
        implements ContestSeatService {

}
