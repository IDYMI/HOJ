<template>
  <div style="text-align:center">
    <div style="margin-bottom:10px" v-if="contest.type != undefined">
      <span class="tips">
        {{
        contest.type == 0
        ? $t('m.ACM_Contest_Add_From_Public_Problem_Tips')
        : $t('m.OI_Contest_Add_From_Public_Problem_Tips')
        }}
      </span>
    </div>
    <vxe-input
      v-model="keyword"
      :placeholder="$t('m.Enter_keyword')"
      type="search"
      size="medium"
      @search-click="filterByKeyword"
      @keyup.enter.native="filterByKeyword"
      style="margin-bottom:10px"
    ></vxe-input>
    <vxe-table :data="problemList" :loading="loading" auto-resize stripe align="center">
      <vxe-table-column title="ID" min-width="100" field="problemId"></vxe-table-column>
      <vxe-table-column min-width="150" :title="$t('m.Title')" field="title"></vxe-table-column>
      <vxe-table-column :title="$t('m.Option')" align="center" min-width="100">
        <template v-slot="{ row }">
          <el-tooltip effect="dark" :content="$t('m.Add')" placement="top">
            <el-button
              icon="el-icon-plus"
              size="mini"
              @click.native="handleAddProblem(row.id, row.problemId, row.problemDescriptionList)"
              type="primary"
            ></el-button>
          </el-tooltip>
        </template>
      </vxe-table-column>
    </vxe-table>
    <el-pagination
      class="page"
      layout="prev, pager, next, sizes"
      @current-change="currentChange"
      :page-size="limit"
      :current-page.sync="currentPage"
      :total="total"
      @size-change="onPageSizeChange"
      :page-sizes="[10, 30, 50, 100]"
    ></el-pagination>

    <el-dialog append-to-body :visible.sync="handleVisible" z-index="9000">
      <el-form>
        <el-form-item
          v-if="contestId"
          :label="$t('m.Enter_The_Problem_Display_ID_in_the_Contest')"
          required
        >
          <el-input v-model="displayId" size="small"></el-input>
        </el-form-item>

        <el-form-item :label="$t('m.Enter_The_Problem_Description_ID')" required>
          <el-tag
            v-for="problemDescription in problemDescriptionList"
            size="medium"
            class="filter-item"
            :effect="peid == problemDescription.id ? 'dark' : 'plain'"
            :key="problemDescription.id"
            @click="filterByPeid(problemDescription.id)"
          >{{ problemDescription.id + ": " + problemDescription.title }}</el-tag>
        </el-form-item>

        <el-form-item style="text-align: center">
          <el-button type="primary" @click="addProblem" :loading="handleLoading">{{ $t("m.OK") }}</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script>
import api from "@/common/api";
import mMessage from "@/common/message";
import Pagination from "@/components/oj/common/Pagination";
import utils from "@/common/utils";

export default {
  name: "AddProblemFromPublic",
  components: {
    Pagination,
  },
  props: {
    apiMethod: {
      type: String,
      default: "getGroupTrainingProblemList",
    },
    trainingId: {
      type: Number,
      default: null,
    },
    contestId: {
      type: Number,
      default: null,
    },
  },
  data() {
    return {
      currentPage: 1,
      limit: 10,
      total: 0,
      loading: false,
      problemList: [],
      contest: {},
      keyword: "",
      handleVisible: false,
      handleLoading: false,
      problemDescriptionList: [],
      pid: null,
      peid: null,
      displayId: null,
      problemId: null,
    };
  },
  mounted() {
    if (this.contestId) {
      api
        .getGroupContest(this.contestId)
        .then((res) => {
          this.contest = res.data.data;
          this.init();
        })
        .catch(() => {});
    } else if (this.trainingId) {
      this.init();
    }
  },
  methods: {
    init() {
      this.getPublicProblem();
    },
    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.init();
    },
    currentChange(page) {
      this.currentPage = page;
      this.init();
    },
    getPublicProblem() {
      this.loading = true;
      let params = {
        keyword: this.keyword,
        problemType: this.contest.type,
        cid: this.contest.id,
        tid: this.trainingId,
        queryExisted: false,
      };
      api[this.apiMethod](this.currentPage, this.limit, params)
        .then((res) => {
          this.loading = false;
          this.total = res.data.data.problemList.total;
          this.problemList = res.data.data.problemList.records;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handleAddProblem(id, problemId, problemDescriptionList) {
      this.pid = id;
      this.problemId = problemId;
      this.problemDescriptionList = problemDescriptionList;
      this.handleVisible = true;
      this.peid = null;
      this.displayId = null;
    },
    addProblem() {
      let data = {
        pid: this.pid,
        peid: this.peid,
        displayId: this.displayId || this.problemId,
        cid: this.contestId,
        tid: this.trainingId,
      };

      const func = this.contestId
        ? "addGroupContestProblemFromPublic"
        : "addGroupTrainingProblemFromPublic";

      api[func](data).then(
        (res) => {
          mMessage.success(this.$i18n.t("m.Add_Successfully"));
          this.getPublicProblem(this.page);
          this.handleVisible = false;
          this.handleLoading = false;
          this.$emit("currentChangeProblem");
          this.currentChange(1);
        },
        () => {
          this.handleVisible = false;
          this.handleLoading = false;
        }
      );
    },
    filterByPeid(peid) {
      this.peid = peid;
    },
    filterByKeyword() {
      this.page = 1;
      this.getPublicProblem(this.page);
    },
  },
};
</script>
<style scoped>
.page {
  margin-top: 20px;
  text-align: right;
}
.tips {
  color: red;
  font-weight: bolder;
  font-size: 1rem;
}
.filter-item {
  margin-right: 1em;
  margin-top: 0.5em;
  font-size: 13px;
}
.filter-item:hover {
  cursor: pointer;
}
</style>
