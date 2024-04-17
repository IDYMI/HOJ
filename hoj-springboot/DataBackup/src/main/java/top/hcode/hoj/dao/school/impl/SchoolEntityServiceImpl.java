package top.hcode.hoj.dao.school.impl;

import top.hcode.hoj.pojo.entity.school.School;
import top.hcode.hoj.dao.school.SchoolEntityService;
import top.hcode.hoj.mapper.SchoolMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SchoolEntityServiceImpl extends ServiceImpl<SchoolMapper, School>
                implements SchoolEntityService {

}
