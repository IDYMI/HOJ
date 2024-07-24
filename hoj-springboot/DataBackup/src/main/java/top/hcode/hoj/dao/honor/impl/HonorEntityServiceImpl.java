package top.hcode.hoj.dao.honor.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import top.hcode.hoj.dao.honor.HonorEntityService;
import top.hcode.hoj.mapper.HonorMapper;
import top.hcode.hoj.pojo.entity.honor.Honor;

@Service
public class HonorEntityServiceImpl extends ServiceImpl<HonorMapper, Honor> implements HonorEntityService {

}