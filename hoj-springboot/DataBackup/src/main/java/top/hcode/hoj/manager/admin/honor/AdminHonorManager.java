package top.hcode.hoj.manager.admin.honor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.common.AnnouncementEntityService;
import top.hcode.hoj.dao.honor.HonorEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.mapper.HonorMapper;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.entity.honor.Honor;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.validator.HonorValidator;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j(topic = "hoj")
public class AdminHonorManager {

    @Resource
    private HonorEntityService honorEntityService;

    @Resource
    private HonorValidator honorValidator;

    @Resource
    private HonorMapper honorMapper;

    @Resource
    private UserRoleEntityService userRoleEntityService;

    @Autowired
    private AnnouncementEntityService announcementEntityService;

    public IPage<Honor> getHonorList(Integer limit, Integer currentPage, String keyword, String type, String year) {

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;

        // 新建分页
        Page<Honor> page = new Page<>(currentPage, limit);

        List<Honor> honorList = honorMapper.getAdminHonorList(page, keyword, type, year);

        page.setRecords(honorList);

        return page;

    }

    public Honor getHonor(Long hid) throws StatusFailException, StatusForbiddenException {
        // 获取荣誉的信息
        Honor honor = honorEntityService.getById(hid);
        if (honor == null) { // 查询不存在
            throw new StatusFailException("查询失败：该荣誉不存在,请检查参数hid是否准确！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        // 只有超级管理员和荣誉创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(honor.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        String link = honor.getLink();
        if (link != null && link.startsWith("/announcement")) {
            Announcement announcement = getAnnouncement(link);

            if (announcement != null) {
                honor.setDescription(announcement.getContent());
            }
        }

        return honor;
    }

    public void deleteHonor(Long hid) throws StatusFailException, StatusForbiddenException {
        // 获取荣誉的信息
        Honor honor = honorEntityService.getById(hid);
        if (honor == null) { // 查询不存在
            throw new StatusFailException("查询失败：该荣誉不存在,请检查参数hid是否准确！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");

        // 只有超级管理员和题目管理和荣誉创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(honor.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        String link = honor.getLink();
        if (link != null && link.startsWith("/announcement")) {
            Announcement announcement = getAnnouncement(link);

            if (announcement != null) {
                boolean isOk = announcementEntityService.removeById(announcement.getId());

                if (!isOk) {
                    throw new StatusFailException("删除失败！");
                }
            }
        }

        boolean isOk = honorEntityService.removeById(hid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
        log.info("[{}],[{}],hid:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Honor", "Delete", hid, userRolesVo.getUid(), userRolesVo.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addHonor(Honor honor) throws StatusFailException, StatusForbiddenException {
        honor.setIsGroup(false);
        honorValidator.validateHonor(honor);

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");
        // 只有超级管理员和荣誉创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(honor.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        String author = honor.getAuthor();
        UserRolesVO userRolesVO = userRoleEntityService.getUserRoles(null, author);

        if (honor.getDescription() != null) {
            Announcement announcement = new Announcement()
                    .setTitle(honor.getTitle())
                    .setContent(honor.getDescription())
                    .setUid(userRolesVO.getUid())
                    .setGid(null)
                    .setStatus(0);

            boolean isOk = announcementEntityService.save(announcement);
            if (!isOk) {
                throw new StatusFailException("添加失败");
            }

            honor.setLink("/announcement/" + announcement.getId());
            honor.setDescription(null);
        }

        boolean isOk = honorEntityService.save(honor);

        if (!isOk) {
            throw new StatusFailException("添加失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateHonor(Honor honor) throws StatusForbiddenException, StatusFailException {
        honorValidator.validateHonor(honor);

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");
        // 只有超级管理员和荣誉创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(honor.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        String link = honor.getLink();
        if (link != null && link.startsWith("/announcement")) {
            Announcement announcement = getAnnouncement(link);

            if (announcement != null) {
                boolean isOk = announcementEntityService
                        .saveOrUpdate(announcement.setTitle(honor.getTitle()).setContent(honor.getDescription()));

                if (!isOk) {
                    throw new StatusFailException("修改失败");
                }
            }
            honor.setDescription(null);
        }

        Boolean isOk = honorEntityService.updateById(honor);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public void changeHonorStatus(Long hid, String author, Boolean status)
            throws StatusForbiddenException, StatusFailException {
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        boolean isRoot = SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin");
        // 只有超级管理员和荣誉创建者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(author)) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        boolean isOk = honorEntityService.saveOrUpdate(new Honor().setId(hid).setStatus(status));
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public Announcement getAnnouncement(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String[] parts = str.split("/");
        String index = parts[parts.length - 1];

        Long id = Long.parseLong(index);
        Announcement announcement = announcementEntityService.getById(id);

        return announcement;
    }
}