package top.hcode.hoj.schedule;

public interface ScheduleService {
    void deleteAvatar();

    void deleteTestCase();

    void deleteContestPrintText();

    // void getOjContestsList();

    void getCodeforcesInfo();

    void getNowcoderInfo();

    void getVjudgeInfo();

    void getPojInfo();

    void getAtcodeInfo();

    void getLeetcodeInfo();

    void deleteUserSession();

    void syncNoticeToRecentHalfYearUser();

    void check20MPendingSubmission();

    void checkUnHandleGroupProblemApplyProgress();
}
