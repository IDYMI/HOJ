package top.hcode.hoj.dao.school.impl;

import top.hcode.hoj.pojo.entity.school.ExaminationSeat;
import top.hcode.hoj.dao.school.ExaminationSeatService;
import top.hcode.hoj.mapper.ExaminationSeatMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ExaminationSeatServiceImpl extends ServiceImpl<ExaminationSeatMapper, ExaminationSeat>
                implements ExaminationSeatService {

}
