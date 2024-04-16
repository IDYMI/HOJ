<template>
  <div id="main">
    <el-card ref="card">
      <vxe-table
        round
        border
        auto-resize
        size="medium"
        align="center"
        ref="ACContestRank"
        :data="acContestSubmissionList"
        :key="contestProblems"
        :row-style="{height: height }"
        @cell-click="getUserProblemSubmission"
      >
        <vxe-table-column
          fixed="left"
          field="username"
          min-width="96"
          :title="$t('m.Username')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          fixed="left"
          field="realname"
          min-width="96"
          :title="$t('m.RealName')"
          show-overflow
        ></vxe-table-column>
        <vxe-table-column
          min-width="400"
          v-for="problem in contestProblems"
          :key="problem.displayId"
          :field="problem.displayId"
        >
          <template v-slot:header>
            <span class="contest-rank-balloon" v-if="problem.color">
              <svg
                t="1633685184463"
                class="icon"
                viewBox="0 0 1088 1024"
                version="1.1"
                xmlns="http://www.w3.org/2000/svg"
                p-id="5840"
                width="25"
                height="25"
              >
                <path
                  d="M575.872 849.408c-104.576 0-117.632-26.56-119.232-31.808-6.528-22.528 32.896-70.592 63.744-96.768l-1.728-2.624c137.6-42.688 243.648-290.112 243.648-433.472A284.544 284.544 0 0 0 478.016 0a284.544 284.544 0 0 0-284.288 284.736c0 150.4 116.352 415.104 263.744 438.336-25.152 29.568-50.368 70.784-39.104 108.928 12.608 43.136 62.72 63.232 157.632 63.232 7.872 0 11.52 9.408 4.352 19.52-21.248 29.248-77.888 63.424-167.68 63.424V1024c138.944 0 215.936-74.816 215.936-126.528a46.72 46.72 0 0 0-16.32-36.608 56.32 56.32 0 0 0-36.416-11.456zM297.152 297.472c0 44.032-38.144 25.344-38.144-38.656 0-108.032 85.248-195.712 190.592-195.712 62.592 0 81.216 39.232 38.08 39.232-105.152 0.064-190.528 87.04-190.528 195.136z"
                  :fill="problem.color"
                  p-id="5841"
                />
              </svg>
            </span>
            <span>
              <a
                @click="getContestProblemById(problem.displayId)"
                class="emphasis"
                style="color:#495060;"
              >{{ problem.displayId }}</a>
            </span>
            <br />
            <span>
              <el-tooltip effect="dark" placement="top">
                <div slot="content">
                  {{ problem.displayId + '. ' + problem.displayTitle }}
                  <br />
                  {{ 'Accepted: ' + problem.ac }}
                  <br />
                  {{ 'Rejected: ' + (problem.total - problem.ac) }}
                </div>
                <span>({{ problem.ac }}/{{ problem.total }})</span>
              </el-tooltip>
            </span>
          </template>

          <template v-slot="{ row }">
            <el-col :span="24" style="overflow-y: auto;" v-if="row.codeList[problem.displayId]">
              <div
                style="margin-top: 13px; text-align: left;display: flex; flex-direction: column; max-height:450px;"
              >
                <template v-for="(codeInfo, index) in row.codeList[problem.displayId]">
                  <Highlight
                    @height-change="handleCodeHeightChange(index, $event)"
                    :key="codeInfo.submitId"
                    :code="codeInfo.code"
                    :canFold="true"
                    :collapsed="false"
                    :lineHeight="20"
                  ></Highlight>
                </template>
              </div>
            </el-col>
          </template>
        </vxe-table-column>
      </vxe-table>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import mMessage from "@/common/message";
const Highlight = () => import("@/components/oj/common/Highlight");
import { mapState } from "vuex";

export default {
  name: "SearchExamination",
  components: {
    Highlight,
  },
  data() {
    return {
      pageSize: 10,
      total: 0,
      acContestSubmissionList: [],
      dataRank: [],
      keyword: "",
      excludeAdmin: true,
      currentPage: 1,
      currentId: 1,
      downloadDialogVisible: false,
      height: "500px",
    };
  },
  beforeCreate() {
    if (this.$store.state.contest.contestProblems.length === 0) {
      this.$store.dispatch("getContestProblems");
    }
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getAcContestSubmissionList();
  },
  methods: {
    getAcContestSubmissionList(page) {
      let params = {
        username: null,
        problemID: null,
        contestID: this.$route.params.contestID,
      };
      api.getAcContestSubmissionList(params).then(
        (res) => {
          this.total = res.data.data;
          this.acContestSubmissionList = res.data.data;
          this.applyToTable(this.acContestSubmissionList);
        },
        (res) => {}
      );
    },
    getContestProblemById(pid) {
      this.$router.push({
        name: "ContestProblemDetails",
        params: {
          contestID: this.contestID,
          problemID: pid,
        },
      });
    },

    applyToTable(acContestSubmissionList) {
      acContestSubmissionList.forEach((submission, i) => {
        let codeList = submission.codeList;
        let cellClass = {};

        codeList.forEach((codeInfo) => {
          if (!cellClass[codeInfo.displayId]) {
            cellClass[codeInfo.displayId] = [];
          }
          cellClass[codeInfo.displayId].push({
            submitId: codeInfo.submitId,
            code: codeInfo.code,
          });
        });
        submission.codeList = cellClass;
      });
    },
    getUserProblemSubmission({ row, column }) {
      // let submitId = row.submitId;
      // if (
      //   submitId != null &&
      //   submitId != "" &&
      //   submitId != undefined &&
      //   column.property != "username" &&
      //   column.property != "realname"
      // ) {
      //   this.$router.push({
      //     name: "ContestSubmissionDetails",
      //     params: {
      //       contestID: this.contestID,
      //       problemID: row.displayId,
      //       submitID: submitId,
      //     },
      //   });
      // }
    },
    handleCodeHeightChange(index, height) {
      // 根据高度变化调整行高
      // this.height = height + 100 + "px";
    },
  },
  computed: {
    ...mapState({
      contest: (state) => state.contest.contest,
      contestProblems: (state) => state.contest.contestProblems,
    }),
  },
};
</script>

<style scoped>
.hljs code {
  line-height: inherit !important;
}

/* 设置 vxe-table 的单元格行高 */
.el-table__body-wrapper tbody tr td {
  line-height: inherit !important;
}
</style>