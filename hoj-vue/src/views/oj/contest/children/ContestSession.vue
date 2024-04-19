<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">{{ $t('m.Admin_Session') }}</span>
      <div class="filter-row">
        <span>
          <vxe-input
            v-model="keyword"
            :placeholder="$t('m.Enter_Session_Keyword')"
            type="search"
            size="small"
            @search-click="filterByKeyword"
            @keyup.enter.native="filterByKeyword"
            style="width: 250px;"
          ></vxe-input>
        </span>
        <span>
          <vxe-input
            v-model="unkeyword"
            :placeholder="$t('m.Enter_Session_Unkeyword')"
            type="search"
            size="small"
            @search-click="filterByUnkeyword"
            @keyup.enter.native="filterByUnkeyword"
            style="width: 250px;"
          ></vxe-input>
        </span>
        <span>
          {{ $t('m.Auto_Refresh') }}(10s)
          <el-switch @change="handleAutoRefresh" v-model="autoRefresh"></el-switch>
        </span>
        <span>
          <el-button
            type="primary"
            @click="getSessionList(1)"
            size="small"
            icon="el-icon-refresh"
            :loading="btnLoading"
          >{{ $t('m.Refresh') }}</el-button>
        </span>
      </div>
    </div>
    <vxe-table border="inner" stripe auto-resize align="center" :data="sessionList">
      <vxe-table-column field="gmtCreate" min-width="150" :title="$t('m.Submit_Time')">
        <template v-slot="{ row }">
          <span>{{ row.gmtCreate | localtime }}</span>
        </template>
      </vxe-table-column>
      <vxe-table-column field="username" :title="$t('m.Username')" min-width="150">
        <template v-slot="{ row }">
          <span>
            <a
              @click="getUserTotalSubmit(row.username)"
              style="color:rgb(87, 163, 243);"
            >{{ row.username }}</a>
          </span>
        </template>
      </vxe-table-column>
      <vxe-table-column field="realname" :title="$t('m.RealName')" min-width="150"></vxe-table-column>
      <vxe-table-column field="ip" title="IP" min-width="150"></vxe-table-column>
      <vxe-table-column field="routeName" :title="$t('m.routeName')" min-width="150"></vxe-table-column>
    </vxe-table>
    <Pagination
      :total="total"
      :page-size.sync="limit"
      :current.sync="page"
      @on-change="getSessionList"
    ></Pagination>
  </el-card>
</template>
<script>
import api from "@/common/api";
const Pagination = () => import("@/components/oj/common/Pagination");

export default {
  name: "ACM-Info-Admin",
  components: {
    Pagination,
  },
  data() {
    return {
      page: 1,
      limit: 20,
      total: 0,
      btnLoading: false,
      autoRefresh: false,
      sessionList: [],
      keyword: null,
      unkeyword: null,
    };
  },
  mounted() {
    this.getSessionList(1);
  },
  methods: {
    getUserTotalSubmit(username) {
      this.$router.push({
        name: "ContestSubmissionList",
        query: { username: username },
      });
    },
    getSessionList(page = 1) {
      this.btnLoading = true;

      let params = {
        cid: this.$route.params.contestID,
        limit: this.limit,
        currentPage: page,
      };

      if (this.keyword != null && this.keyword != "") {
        params.keyword = this.keyword;
      }

      if (this.unkeyword != null && this.unkeyword != "") {
        params.unkeyword = this.unkeyword;
      }

      api
        .getContestSession(params)
        .then((res) => {
          this.btnLoading = false;
          this.sessionList = res.data.data.records;
          this.total = res.data.data.total;
        })
        .catch(() => {
          this.btnLoading = false;
        });
    },
    handleAutoRefresh() {
      if (this.autoRefresh) {
        this.refreshFunc = setInterval(() => {
          this.page = 1;
          this.getSessionList();
        }, 10000);
      } else {
        clearInterval(this.refreshFunc);
      }
    },
    filterByKeyword() {
      this.getSessionList(1);
    },
    filterByUnkeyword() {
      this.getSessionList(1);
    },
  },
  beforeDestroy() {
    clearInterval(this.refreshFunc);
  },
};
</script>
<style scoped>
.filter-row {
  float: right;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 2px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
/deep/ .el-tag--dark {
  border-color: #fff;
}
</style>
