package top.hcode.hoj.dao.school.impl;

import top.hcode.hoj.pojo.entity.school.ExaminationRoom;
import top.hcode.hoj.dao.school.ExaminationRoomService;
import top.hcode.hoj.mapper.ExaminationRoomMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ExaminationRoomServiceImpl extends ServiceImpl<ExaminationRoomMapper, ExaminationRoom>
                implements ExaminationRoomService {

}
