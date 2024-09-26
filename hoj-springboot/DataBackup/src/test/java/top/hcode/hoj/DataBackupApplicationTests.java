package top.hcode.hoj;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.mapper.*;
import top.hcode.hoj.pojo.bo.File_;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.dao.common.impl.AnnouncementEntityServiceImpl;
import top.hcode.hoj.dao.discussion.impl.DiscussionEntityServiceImpl;
import top.hcode.hoj.dao.problem.impl.LanguageEntityServiceImpl;
import top.hcode.hoj.dao.user.impl.UserInfoEntityServiceImpl;
import top.hcode.hoj.dao.user.impl.UserRoleEntityServiceImpl;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.JsoupUtils;
import top.hcode.hoj.utils.RedisUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/24 17:24
 * @Description:
 */
@SpringBootTest
public class DataBackupApplicationTests {

}