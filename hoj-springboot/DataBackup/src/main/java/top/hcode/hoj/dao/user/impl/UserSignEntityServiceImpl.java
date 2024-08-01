package top.hcode.hoj.dao.user.impl;

import top.hcode.hoj.pojo.entity.user.UserSign;
import top.hcode.hoj.mapper.UserSignMapper;
import top.hcode.hoj.dao.user.UserSignEntityService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserSignEntityServiceImpl extends ServiceImpl<UserSignMapper, UserSign>
        implements UserSignEntityService {

}
