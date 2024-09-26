package top.hcode.hoj.dao.problem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.dto.ProblemRes;
import top.hcode.hoj.pojo.dto.ProblemResDTO;
import top.hcode.hoj.pojo.vo.ImportProblemVO;
import top.hcode.hoj.pojo.vo.ProblemVO;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemDescription;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */

public interface ProblemEntityService extends IService<Problem> {
    Page<ProblemVO> getProblemList(int limit, int currentPage, Long pid, String title,
            Integer difficulty, Integer type, List<Long> tid, String oj);

    boolean adminUpdateProblem(ProblemDTO problemDto);

    boolean adminAddProblem(ProblemDTO problemDto);

    ImportProblemVO buildExportProblem(Long pid, List<HashMap<String, Object>> problemCaseList,
            HashMap<Long, String> languageMap, HashMap<Long, String> tagMap);

    ProblemResDTO getProblemResDTO(Long pid, Long peid, String problemId, Long gid);

    ProblemRes getProblemRes(Long pid, Long peid, String problemId, Long gid);

    String getDefaultProblemTitle(Problem problem);

    List<ProblemResDTO> getRecentUpdatedProblemList();

    IPage<ProblemResDTO> getAdminProblemList(IPage<ProblemResDTO> iPage, String keyword, Integer auth, String oj,
            Integer difficulty, Integer type, Boolean isRemote);

    IPage<ProblemResDTO> getAdminGroupProblemList(IPage<ProblemResDTO> iPage, String keyword, Long gid);

    IPage<ProblemResDTO> getAdminContestProblemList(IPage<ProblemResDTO> iPage, String keyword, Long cid,
            Integer problemType, String oj, Integer difficulty, Integer type, Long gid, Boolean isRemote,
            Long contestGid, List<Long> pidList);

    IPage<ProblemResDTO> getAdminTrainingProblemList(IPage<ProblemResDTO> iPage, String keyword,
            Boolean queryExisted,
            Long tid, List<Long> pidList);

    List<ProblemDescription> getProblemDescriptionList(Long pid, Long peid, String problemId, Long gid);

    public Boolean updateProblemDescription(Long pid, Long peid, String pdfName);

}
