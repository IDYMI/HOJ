package top.hcode.hoj.manager.oj;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.school.ContestSeatService;
import top.hcode.hoj.dao.school.ExaminationRoomService;
import top.hcode.hoj.dao.school.ExaminationSeatService;
import top.hcode.hoj.dao.school.SchoolEntityService;
import top.hcode.hoj.mapper.ExaminationRoomMapper;
import top.hcode.hoj.mapper.ExaminationSeatMapper;
import top.hcode.hoj.pojo.dto.ExaminationRoomDTO;
import top.hcode.hoj.pojo.dto.ExaminationSeatDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.school.ContestSeat;
import top.hcode.hoj.pojo.entity.school.ExaminationRoom;
import top.hcode.hoj.pojo.entity.school.ExaminationSeat;
import top.hcode.hoj.pojo.entity.school.School;
import top.hcode.hoj.pojo.vo.ExaminationRoomVO;
import top.hcode.hoj.pojo.vo.ExaminationSeatVO;
import top.hcode.hoj.pojo.vo.ExaminationUserInfoVO;
import top.hcode.hoj.pojo.vo.SchoolVO;
import top.hcode.hoj.shiro.AccountProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 *
 * @Description:
 */
@Component
@Slf4j
public class ExaminationManager {

    @Autowired
    private SchoolEntityService schoolEntityService;

    @Autowired
    private ExaminationRoomService examinationRoomService;

    @Autowired
    private ExaminationSeatService examinationSeatService;

    @Autowired
    private ExaminationSeatMapper examinationSeatMapper;

    @Autowired
    private ExaminationRoomMapper examinationRoomMapper;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestSeatService contestSeatService;

    public List<SchoolVO> getSchoolList() {
        QueryWrapper<School> schoolQueryWrapper = new QueryWrapper<>();
        schoolQueryWrapper.select("id", "province", "city", "name");
        List<School> problemList = schoolEntityService.list(schoolQueryWrapper);

        return problemList.stream()
                .map(item -> {
                    SchoolVO schoolVo = new SchoolVO();
                    schoolVo.setId(item.getId());
                    schoolVo.setProvince(item.getProvince());
                    schoolVo.setCity(item.getCity());
                    schoolVo.setName(item.getName());
                    return schoolVo;
                })
                .collect(Collectors.toList());
    }

    public IPage<ExaminationRoomVO> getExaminationRoomList(Integer limit, Integer currentPage, String keyword, Long eid,
            Long cid)
            throws StatusFailException {

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;

        List<ExaminationRoomVO> allSchoolRoomVOList = examinationRoomMapper.getExaminationRoomList(eid, keyword);

        if (cid != null) {
            for (ExaminationRoomVO ExaminationRoomVo : allSchoolRoomVOList) {
                int used = examinationRoomMapper.getExaminationUsed(cid, ExaminationRoomVo.getEid());
                ExaminationRoomVo.setUsed(used);
            }

            // 对 used 排序
            allSchoolRoomVOList = allSchoolRoomVOList.stream()
                    .sorted(Comparator.comparingInt(ExaminationRoomVO::getUsed).reversed())
                    .collect(Collectors.toList());

        }

        // 初始化 schoolRoomVOList
        List<ExaminationRoomVO> schoolRoomVOList = new ArrayList<>();

        // 根据当前页数和每页数量进行分页
        schoolRoomVOList = allSchoolRoomVOList.stream()
                .skip((currentPage - 1) * limit)
                .limit(limit)
                .collect(Collectors.toList());

        // 创建 Page 对象并设置分页信息和记录列表
        Page<ExaminationRoomVO> page = new Page<>(currentPage, limit);
        page.setTotal(allSchoolRoomVOList.size());
        page.setRecords(schoolRoomVOList);

        return page;
    }

    public ExaminationRoomVO getExaminationRoom(Long eid) throws StatusFailException {
        // 获取房间
        ExaminationRoom examinationRoom = examinationRoomService.getById(eid);

        if (examinationRoom == null) {
            throw new StatusFailException("找不到对应房间，请重试！");
        }

        // 查询已经添加的布局
        QueryWrapper<ExaminationSeat> examinationSeatQueryWrapper = new QueryWrapper<>();
        examinationSeatQueryWrapper.eq("eid", eid);
        List<ExaminationSeat> examinationRoomList = examinationSeatService.list(examinationSeatQueryWrapper);

        List<ExaminationRoomDTO> examinationRoomDTOList = examinationRoomList.stream()
                .map(seat -> {
                    ExaminationRoomDTO examinationRoomDTo = new ExaminationRoomDTO();
                    examinationRoomDTo.setId(seat.getId());
                    examinationRoomDTo.setCol(seat.getGcol());
                    examinationRoomDTo.setRow(seat.getGrow());
                    examinationRoomDTo.setType(seat.getType());
                    return examinationRoomDTo;
                })
                .collect(Collectors.toList());

        List<SchoolVO> schoolList = getSchoolList();
        SchoolVO school = schoolList.stream()
                .filter(s -> s.getId().equals(examinationRoom.getSchoolId()))
                .findFirst()
                .orElse(null);

        ExaminationRoomVO examinationRoomVo = new ExaminationRoomVO();
        examinationRoomVo.setSchool(school.getName())
                .setSchoolId(examinationRoom.getSchoolId())
                .setBuilding(examinationRoom.getBuilding())
                .setMaxRow(examinationRoom.getMaxRow())
                .setMaxCol(examinationRoom.getMaxCol())
                .setRoom(examinationRoom.getRoom())
                .setSeatList(examinationRoomDTOList);

        return examinationRoomVo;
    }

    public void addExaminationRoom(ExaminationRoomVO examinationRoomVo) throws StatusFailException {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        Long schoolId = examinationRoomVo.getSchoolId();
        String building = examinationRoomVo.getBuilding();
        String room = examinationRoomVo.getRoom();
        List<ExaminationRoomDTO> seatList = examinationRoomVo.getSeatList();

        if (schoolId == null) {
            throw new StatusFailException("找不到对应学校，请重试！");
        }

        if (CollectionUtils.isEmpty(seatList)) {
            throw new StatusFailException("提供的布局为空，请重试！");
        }

        QueryWrapper<ExaminationRoom> examinationRoomQueryWrapper = new QueryWrapper<>();
        examinationRoomQueryWrapper.eq("school_id", schoolId).eq("building", building).eq("room", room);
        ExaminationRoom examinationRoom = examinationRoomService.getOne(examinationRoomQueryWrapper, false);

        if (examinationRoom != null) {
            throw new StatusFailException("该考场已布置，请前往编辑！");
        }

        ExaminationRoom newExaminationRoom = new ExaminationRoom();
        newExaminationRoom.setSchoolId(schoolId)
                .setBuilding(building)
                .setRoom(room)
                .setMaxRow(examinationRoomVo.getMaxRow())
                .setMaxCol(examinationRoomVo.getMaxCol())
                .setAuthor(userRolesVo.getUsername());

        boolean isOk1 = examinationRoomService.saveOrUpdate(newExaminationRoom);

        if (isOk1) {
            List<ExaminationSeat> examinationRoomList = seatList.stream()
                    .map(seat -> {
                        ExaminationSeat examinationSeat = new ExaminationSeat();
                        examinationSeat.setEid(newExaminationRoom.getId());
                        examinationSeat.setGcol(seat.getCol());
                        examinationSeat.setGrow(seat.getRow());
                        examinationSeat.setType(seat.getType());
                        return examinationSeat;
                    })
                    .collect(Collectors.toList());

            boolean isOk2 = examinationSeatService.saveBatch(examinationRoomList);
            if (!isOk2) {
                throw new StatusFailException("添加考场失败，请重新尝试！");
            }
        } else {
            throw new StatusFailException("添加考场失败，请重新尝试！");
        }
    }

    public void updateExaminationRoom(ExaminationRoomVO examinationRoomVo) throws StatusFailException {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        if (examinationRoomVo == null || examinationRoomVo.getEid() == null) {
            throw new StatusFailException("找不到对应考场，请重试！");
        }

        ExaminationRoom newExaminationRoom = examinationRoomService.getById(examinationRoomVo.getEid());

        newExaminationRoom.setSchoolId(examinationRoomVo.getSchoolId())
                .setBuilding(examinationRoomVo.getBuilding())
                .setRoom(examinationRoomVo.getRoom())
                .setMaxRow(examinationRoomVo.getMaxRow())
                .setMaxCol(examinationRoomVo.getMaxCol())
                .setModifiedUser(userRolesVo.getUsername());

        boolean isOk1 = examinationRoomService.saveOrUpdate(newExaminationRoom);

        if (isOk1) {
            // 查询已经添加的布局
            QueryWrapper<ExaminationSeat> examinationSeatQueryWrapper = new QueryWrapper<>();
            examinationSeatQueryWrapper.eq("eid", examinationRoomVo.getEid());
            List<ExaminationSeat> examinationRoomList = examinationSeatService.list(examinationSeatQueryWrapper);

            if (!CollectionUtils.isEmpty(examinationRoomList)) {
                // 删除已经添加的布局
                deletExaminationSeatList(examinationRoomList);
            }

            List<ExaminationRoomDTO> seatList = examinationRoomVo.getSeatList();

            if (CollectionUtils.isEmpty(seatList)) {
                throw new StatusFailException("提供的布局为空，请重试！");
            }

            // 重新添加布局
            List<ExaminationSeat> examinationRoomList2 = seatList.stream()
                    .map(seat -> {
                        ExaminationSeat examinationSeat = new ExaminationSeat();
                        examinationSeat.setEid(examinationRoomVo.getEid());
                        examinationSeat.setGcol(seat.getCol());
                        examinationSeat.setGrow(seat.getRow());
                        examinationSeat.setType(seat.getType());
                        return examinationSeat;
                    })
                    .collect(Collectors.toList());

            boolean isOk2 = examinationSeatService.saveBatch(examinationRoomList2);
            if (!isOk2) {
                throw new StatusFailException("修改考场失败，请重新尝试！");
            }
        } else {
            throw new StatusFailException("修改考场失败，请重新尝试！");
        }
    }

    public IPage<ExaminationRoomVO> getExaminationSeatList(Long cid, Integer limit, Integer currentPage, String keyword)
            throws StatusFailException {

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        if (contest == null) {
            throw new StatusFailException("比赛不存在！");
        }

        if (currentPage == null || currentPage < 1)
            currentPage = 1;
        if (limit == null || limit < 1)
            limit = 10;

        // 新建分页
        Page<ExaminationRoomVO> page = new Page<>(currentPage, limit);

        List<ExaminationRoomVO> schoolSeatVoList = examinationRoomMapper.getContestEidList(cid, keyword);

        if (CollectionUtils.isEmpty(schoolSeatVoList)) {
            throw new StatusFailException("比赛未布置考场！");
        }

        page.setRecords(schoolSeatVoList);

        return page;
    }

    public ExaminationRoomVO getExaminationSeat(Long eid, Long cid) throws StatusFailException {

        // 获取房间
        ExaminationRoom examinationRoom = examinationRoomService.getById(eid);

        Contest contest = contestEntityService.getById(cid);

        if (examinationRoom == null && contest == null) {
            throw new StatusFailException("找不到对应房间或者比赛，请重试！");
        }

        // 查询已经分配好的座位
        List<ExaminationSeatVO> examinationSeatVoList = examinationSeatMapper.getContestSeatList(eid, cid);

        StringBuilder title = new StringBuilder(); // 可变的容器

        List<ExaminationRoomDTO> examinationRoomDTOList = examinationSeatVoList.stream()
                .map(seat -> {
                    ExaminationRoomDTO examinationRoomDTo = new ExaminationRoomDTO();
                    examinationRoomDTo.setId(seat.getId());
                    examinationRoomDTo.setCol(seat.getGcol());
                    examinationRoomDTo.setRow(seat.getGrow());
                    examinationRoomDTo.setType(seat.getType());
                    examinationRoomDTo.setRealname(seat.getRealname());
                    examinationRoomDTo.setCourse(seat.getCourse());
                    examinationRoomDTo.setNumber(seat.getNumber());
                    examinationRoomDTo.setType(seat.getType());
                    examinationRoomDTo.setUsername(seat.getUsername());

                    String contestTitle = seat.getTitle();
                    if (StringUtils.isEmpty(title.toString()) && !StringUtils.isEmpty(contestTitle)) {
                        title.append(contestTitle);
                    }

                    return examinationRoomDTo;
                })
                .collect(Collectors.toList());

        ExaminationRoomVO examinationRoomVo = new ExaminationRoomVO();

        examinationRoomVo.setSeatList(examinationRoomDTOList);

        if (examinationRoom != null) {
            List<SchoolVO> schoolList = getSchoolList();
            SchoolVO school = schoolList.stream()
                    .filter(s -> s.getId().equals(examinationRoom.getSchoolId()))
                    .findFirst()
                    .orElse(null);

            examinationRoomVo.setSchool(school.getName())
                    .setTitle(title.toString())
                    .setSchoolId(examinationRoom.getSchoolId())
                    .setBuilding(examinationRoom.getBuilding())
                    .setRoom(examinationRoom.getRoom())
                    .setMaxRow(examinationRoom.getMaxRow())
                    .setMaxCol(examinationRoom.getMaxCol());
        }

        return examinationRoomVo;
    }

    public void adminExaminationSeat(ExaminationSeatDTO examinationSeatDTo) throws StatusFailException {
        Long cid = examinationSeatDTo.getCid();

        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusFailException("比赛不存在！");
        }

        Boolean retroflex = examinationSeatDTo.getRetroflex(); // 是否为回型排列
        Integer spaced = examinationSeatDTo.getSpaced() ? 2 : 1; // 是否间隔
        Boolean sorted = examinationSeatDTo.getSorted(); // 是否按考生人数编排座位
        Boolean random = examinationSeatDTo.getRandom(); // 是否随机排列
        List<Long> eidList = examinationSeatDTo.getEidList();
        Collections.sort(eidList); // 对 eidList 进行从小到大的排序
        List<ExaminationUserInfoVO> studentInfo = examinationSeatDTo.getStudentInfo();

        if (CollectionUtils.isEmpty(eidList)) {
            throw new StatusFailException("选择考场为空！");
        }

        if (CollectionUtils.isEmpty(studentInfo)) {
            throw new StatusFailException("提供用户名单为空！");
        }

        if (spaced <= 0 || spaced == null) {
            throw new StatusFailException("考生间隔应大于0！");
        }

        // 添加的
        List<ExaminationUserInfoVO> insertStudentInfo = studentInfo.stream()
                .filter(s -> s.getId() == null)
                .collect(Collectors.toList());

        // 更新的
        List<ExaminationUserInfoVO> updateStudentInfo = studentInfo.stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toList());

        if (random) {
            // 分成两个子列表：subject 不为 null 和为 null
            List<ExaminationUserInfoVO> notNullSubjects = insertStudentInfo.stream()
                    .filter(s -> s.getSubject() != null)
                    .collect(Collectors.toList());

            List<ExaminationUserInfoVO> nullSubjects = insertStudentInfo.stream()
                    .filter(s -> s.getSubject() == null)
                    .collect(Collectors.toList());

            // 对 notNullSubjects 按照 subject 排序
            Collections.shuffle(notNullSubjects, new Random());
            Collections.sort(notNullSubjects, Comparator.comparing(ExaminationUserInfoVO::getSubject));

            // 对 nullSubjects 随机排序
            Collections.shuffle(nullSubjects, new Random());

            // 合并两个列表
            List<ExaminationUserInfoVO> result = new ArrayList<>();
            result.addAll(notNullSubjects);
            result.addAll(nullSubjects);

            // 将打乱后的结果重新赋值给 insertStudentInfo
            insertStudentInfo = result;
        }

        List<ContestSeat> insertContestSeatList = new ArrayList<>();

        // TODO 获取限定区域内的所有座位
        List<ExaminationSeatVO> examinationSeatVoList = examinationSeatMapper.getSeatList(cid, eidList);

        // TODO 获取限定区域内选择的房间
        List<ExaminationRoom> examinationRoomList = (List<ExaminationRoom>) examinationRoomService.listByIds(eidList);

        if (CollectionUtils.isEmpty(examinationRoomList)) {
            throw new StatusFailException("获取考场为空，请重新尝试！");
        }

        // TODO 按照矩阵处理座位
        Integer index = 0; // 当前轮到的考生
        Integer sortedId = 0; // 当前考场的排位号
        for (ExaminationRoom examinationRoomVo : examinationRoomList) {
            Integer maxRow = examinationRoomVo.getMaxRow();
            Integer maxCol = examinationRoomVo.getMaxCol();
            Long eid = examinationRoomVo.getId();

            List<ExaminationSeatVO> nowRoomExaminationSeatVOList = examinationSeatVoList.stream()
                    .filter(s -> s.getEid().equals(eid))
                    .collect(Collectors.toList());

            for (int i = 0; i < maxRow; i++) {
                Boolean isRowRetroflex = ((i % 2 == 1) && retroflex);

                // 从门口开始排第一个
                int start = isRowRetroflex ? (maxCol % 2 == 0 ? 1 : 0) : maxCol - 1;
                int end = isRowRetroflex ? maxCol : 0;
                int step = isRowRetroflex ? spaced : -spaced;

                for (int j = start; isRowRetroflex ? j < end : j >= end; j += step) {
                    ExaminationSeatVO nowSeat = nowRoomExaminationSeatVOList.get(i * maxCol + j);

                    String nowRealname = nowSeat.getRealname();

                    if (nowRealname == null
                            && nowSeat.getType() == 0 // 当前座位可用
                            && nowSeat.getCid() == null
                            && index < insertStudentInfo.size()) {
                        ContestSeat contestSeat = new ContestSeat()
                                .setCid(cid)
                                .setTitle(contest.getTitle())
                                .setSid(nowSeat.getSid())
                                .setRealname(insertStudentInfo.get(index).getRealname())
                                .setCourse(insertStudentInfo.get(index).getCourse())
                                .setNumber(insertStudentInfo.get(index).getNumber())
                                .setSubject(insertStudentInfo.get(index).getSubject())
                                .setUid(insertStudentInfo.get(index).getUsername())
                                .setType(1);

                        if (sorted) {
                            contestSeat.setSortId(String.valueOf(sortedId));
                        }

                        insertContestSeatList.add(contestSeat);
                        index += 1;
                        sortedId += 1;
                    }
                }
            }
        }

        QueryWrapper<ContestSeat> contestSeatQueryWrapper = new QueryWrapper<>();
        contestSeatQueryWrapper.eq("cid", cid);
        List<ContestSeat> updateContestSeatList = contestSeatService.list(contestSeatQueryWrapper);

        if (!CollectionUtils.isEmpty(updateContestSeatList)) {
            for (ContestSeat contestSeat : updateContestSeatList) {

                ExaminationUserInfoVO examinationUserInfoVO = updateStudentInfo.stream()
                        .filter(info -> (info.getId() != null) && (info.getId().equals(contestSeat.getId())))
                        .findFirst()
                        .orElse(null);

                // 更新位置
                if (examinationUserInfoVO != null) {
                    contestSeat.setRealname(examinationUserInfoVO.getRealname())
                            .setNumber(examinationUserInfoVO.getNumber())
                            .setCourse(examinationUserInfoVO.getCourse())
                            .setSubject(examinationUserInfoVO.getSubject())
                            .setUid(examinationUserInfoVO.getUsername());
                } else { // 删除的位置
                    contestSeatService.removeById(contestSeat.getId());
                }
            }

            boolean isOk = contestSeatService.updateBatchById(updateContestSeatList);
            if (!isOk) {
                throw new StatusFailException("分配考场失败，请重新尝试！");
            }
        }

        if (index != insertStudentInfo.size()) {
            throw new StatusFailException(
                    "当前选择的考场无法为所有学生分配，还差" + String.valueOf(insertStudentInfo.size() - index) + "个座位！");
        }

        if (!CollectionUtils.isEmpty(insertContestSeatList)) {
            boolean isOk = contestSeatService.saveOrUpdateBatch(insertContestSeatList);
            if (!isOk) {
                throw new StatusFailException("分配考场失败，请重新尝试！");
            }
        }
    }

    public void deletExaminationSeatList(List<ExaminationSeat> examinationRoomList) throws StatusFailException {

        List<Long> idList = examinationRoomList.stream().map(ExaminationSeat::getId).collect(Collectors.toList());

        boolean isOk = examinationSeatService.removeByIds(idList);

        if (!isOk) {
            throw new StatusFailException("删除考场失败，请重新尝试！");
        }
    }
}