package top.hcode.hoj.dao.multiOj.impl;

import top.hcode.hoj.pojo.entity.user.UserMultiOj;
import top.hcode.hoj.dao.multiOj.UserMultiOjEntityService;
import top.hcode.hoj.mapper.UserMultiOjMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class UserMultiOjEntityServiceImpl extends ServiceImpl<UserMultiOjMapper, UserMultiOj>
        implements UserMultiOjEntityService {

}
