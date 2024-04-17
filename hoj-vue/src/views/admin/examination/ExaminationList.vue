<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t("m.Assign_ExaminationRoom_List") }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="goCreateContest"
              icon="el-icon-plus"
            >{{ $t("m.Create") }}</el-button>
          </span>
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              size="medium"
              @search-click="filterByKeyword"
              @keyup.enter.native="filterByKeyword"
            ></vxe-input>
          </span>
          <span>
            <el-select
              v-model="contestType"
              @change="ContestListChangeFilter"
              size="small"
              style="width: 180px"
            >
              <el-option :label="$t('m.All_Contest')" :value="'All'"></el-option>
              <el-option :label="'ACM'" :value="0"></el-option>
              <el-option :label="'OI'" :value="1"></el-option>
            </el-select>
          </span>
          <span>
            <el-select
              v-model="contestAuth"
              @change="ContestListChangeFilter"
              size="small"
              style="width: 180px"
            >
              <el-option :label="$t('m.All_Contest')" :value="'All'"></el-option>
              <el-option :label="$t('m.Public')" :value="0"></el-option>
              <el-option :label="$t('m.Private')" :value="1"></el-option>
              <el-option :label="$t('m.Protected')" :value="2"></el-option>
              <el-option :label="$t('m.Official')" :value="3"></el-option>
              <el-option :label="$t('m.Public_Synchronous')" :value="4"></el-option>
              <el-option :label="$t('m.Private_Synchronous')" :value="5"></el-option>
            </el-select>
          </span>
          <span>
            <el-select
              v-model="contestStatus"
              @change="ContestListChangeFilter"
              size="small"
              style="width: 180px"
            >
              <el-option :label="$t('m.All_Contest')" :value="'All'"></el-option>
              <el-option :label="$t('m.Scheduled')" :value="-1"></el-option>
              <el-option :label="$t('m.Running')" :value="0"></el-option>
              <el-option :label="$t('m.Ended')" :value="1"></el-option>
            </el-select>
          </span>
        </div>
      </div>
      <vxe-table
        :loading="loading"
        ref="xTable"
        :data="contestList"
        auto-resize
        stripe
        align="center"
      >
        <vxe-table-column field="id" width="80" title="ID"></vxe-table-column>
        <vxe-table-column field="title" min-width="150" :title="$t('m.Title')" show-overflow></vxe-table-column>
        <vxe-table-column :title="$t('m.Type')" width="100">
          <template v-slot="{ row }">
            <el-tag type="gray">{{ row.type | parseContestType }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Auth')" width="150">
          <template v-slot="{ row }">
            <el-tooltip
              :content="$t('m.' + CONTEST_TYPE_REVERSE[row.auth].tips)"
              placement="top"
              effect="light"
            >
              <el-tag
                :type="CONTEST_TYPE_REVERSE[row.auth].color"
                effect="plain"
              >{{ CONTEST_TYPE_REVERSE[row.auth].name }}</el-tag>
            </el-tooltip>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Status')" width="100">
          <template v-slot="{ row }">
            <el-tag
              effect="dark"
              :color="CONTEST_STATUS_REVERSE[row.status].color"
              size="medium"
            >{{ CONTEST_STATUS_REVERSE[row.status].name }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="210" :title="$t('m.Info')">
          <template v-slot="{ row }">
            <p>Start Time: {{ row.startTime | localtime }}</p>
            <p>End Time: {{ row.endTime | localtime }}</p>
            <p>Created Time: {{ row.gmtCreate | localtime }}</p>
            <p>Creator: {{ row.author }}</p>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <template v-if="isMainAdminRole || userInfo.uid == row.uid">
              <el-tooltip effect="dark" :content="$t('m.To_Admin_Contest')" placement="top">
                <el-button
                  icon="el-icon-s-unfold"
                  size="mini"
                  @click.native="goAdminContest(row)"
                  type="primary"
                ></el-button>
              </el-tooltip>
              <el-tooltip
                effect="dark"
                :content="$t('m.To_Assign_ExaminationSeat')"
                placement="top"
              >
                <el-button
                  icon="el-icon-tickets"
                  size="mini"
                  @click.native="goContestProblemList(row.id)"
                  type="success"
                ></el-button>
              </el-tooltip>
            </template>
          </template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="currentChange"
          :page-size="pageSize"
          :current-page.sync="currentPage"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import utils from "@/common/utils";
import {
  CONTEST_STATUS_REVERSE,
  CONTEST_TYPE_REVERSE,
} from "@/common/constants";
import { mapGetters } from "vuex";
import myMessage from "@/common/message";
export default {
  name: "ExaminationList",
  data() {
    return {
      title: null,
      pageSize: 10,
      total: 0,
      contestList: [],
      keyword: "",
      contestType: "All", // 比赛类型
      contestAuth: "All", // 赛制
      contestStatus: "All", // 比赛状态
      loading: false,
      excludeAdmin: true,
      splitType: "user",
      currentPage: 1,
      currentId: 1,
      downloadDialogVisible: false,
      CONTEST_TYPE_REVERSE: {},
    };
  },
  mounted() {
    this.keyword = this.$route.query.keyword || "";
    this.contestAuth = this.$route.query.auth || "All";
    this.$router.replace({ query: {} }); // 隐藏 query
    this.CONTEST_TYPE_REVERSE = Object.assign({}, CONTEST_TYPE_REVERSE);
    this.CONTEST_STATUS_REVERSE = Object.assign({}, CONTEST_STATUS_REVERSE);
    this.getContestList(this.currentPage);
  },
  watch: {
    $route() {
      let refresh = this.$route.query.refresh == "true" ? true : false;
      if (refresh) {
        this.getContestList(1);
      }
    },
  },
  computed: {
    ...mapGetters([
      "isMainAdminRole",
      "isSuperAdmin",
      "userInfo",
      "isContestAdmin",
    ]),
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      this.getContestList(page);
    },
    getContestList(page) {
      this.loading = true;
      api
        .admin_getContestList(
          page,
          this.pageSize,
          this.contestType,
          this.contestAuth,
          this.contestStatus,
          this.keyword
        )
        .then(
          (res) => {
            this.loading = false;
            this.total = res.data.data.total;
            this.contestList = res.data.data.records;
          },
          (res) => {
            this.loading = false;
          }
        );
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    goCreateContest() {
      this.$router.push({ name: "admin-create-contest" });
    },
    ContestListChangeFilter() {
      this.currentPage = 1;
      this.getContestList();
    },
    goAdminContest(contest) {
      this.$router.push({
        name: "admin-contest-list",
        query: { keyword: contest.title, auth: contest.auth },
      });
    },
    goContestProblemList(contestId) {
      this.$router.push({
        name: "admin-edit-examinationRoom",
        params: { contestId },
      });
    },
  },
};
</script>
<style scoped>
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
  .filter-row span div {
    width: 80% !important;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
.el-tag--dark {
  border-color: #fff;
}
</style>
