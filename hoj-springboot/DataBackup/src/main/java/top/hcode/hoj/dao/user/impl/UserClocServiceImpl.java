package top.hcode.hoj.dao.user.impl;

import top.hcode.hoj.pojo.entity.user.UserCloc;
import top.hcode.hoj.mapper.UserClocMapper;
import top.hcode.hoj.dao.user.UserClocEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserClocServiceImpl extends ServiceImpl<UserClocMapper, UserCloc>
                implements UserClocEntityService {

}
