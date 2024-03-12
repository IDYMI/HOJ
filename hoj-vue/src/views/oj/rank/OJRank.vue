<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card :padding="10">
        <div slot="header">
          <ul class="nav-list">
            <li>
              <span class="panel-title-oj">{{ $t("m.OJ_Ranklist") }}</span>
            </li>
          </ul>
        </div>
        <div style="text-align: center;">
          <el-input
            :placeholder="$t('m.Rank_Search_Placeholder')"
            v-model="searchUser"
            @keyup.enter.native="getRankData(1)"
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              class="search-btn"
              @click="getRankData(1)"
            ></el-button>
          </el-input>
          <div class="filter-right">
            <el-switch
              v-model="isNew"
              @change="handleOnlyNew"
              :active-text="$t('m.NewAcmer')"
              :inactive-text="$t('m.All')"
            ></el-switch>
          </div>
        </div>
      </el-card>

      <vxe-table
        :data="dataRank"
        :loading="loadingTable"
        align="center"
        highlight-hover-row
        auto-resize
        :seq-config="{ seqMethod }"
        style="font-weight: 500;"
      >
        <vxe-table-column type="seq" min-width="50"></vxe-table-column>
        <vxe-table-column
          field="username"
          :title="$t('m.User')"
          min-width="200"
          show-overflow
          align="left"
        >
          <template v-slot="{ row }">
            <avatar
              :username="row.username"
              :inline="true"
              :size="25"
              color="#FFF"
              :src="row.avatar"
              class="user-avatar"
            ></avatar>
            <a
              @click="getInfoByUsername(row.uid, row.username)"
              style="color:#2d8cf0;"
            >{{ row.username }}</a>
            <span style="margin-left:2px" v-if="row.titleName">
              <el-tag effect="dark" size="small" :color="row.titleColor">{{ row.titleName }}</el-tag>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column field="nickname" :title="$t('m.Nickname')" width="160">
          <template v-slot="{ row }">
            <el-tag
              effect="plain"
              size="small"
              v-if="row.nickname"
              :type="nicknameColor(row.nickname)"
            >{{ row.nickname }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column
          v-for="column in columns"
          :key="column.title"
          :title="$t(column.title)"
          :min-width="90"
        >
          <template v-slot="{ row }">
            <span>{{ row[column.field] }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.codeforcesRating')" min-width="120">
          <template v-slot="{ row }">
            <span>{{ row.codeforcesRating }} / {{ row.codeforcesMaxRating }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.nowcoderRating')" min-width="90">
          <template v-slot="{ row }">
            <span>{{ row.nowcoderRating }}</span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          :title="$t('m.Signature')"
          min-width="300"
          show-overflow="ellipsis"
          align="left"
        >
          <template v-slot="{ row }">
            <span v-katex class="rank-signature-body" v-if="row.signature">
              {{
              row.signature
              }}
            </span>
          </template>
        </vxe-table-column>
      </vxe-table>
      <Pagination
        :page-size.sync="limit"
        :current.sync="page"
        @on-change="getRankData"
        show-sizer
        @on-page-size-change="getRankData(1)"
        :layout="'jumper, prev, pager, next, sizes'"
      ></Pagination>
    </el-col>
  </el-row>
</template>

<script>
import api from "@/common/api";
import utils from "@/common/utils";
import { RULE_TYPE } from "@/common/constants";
import { mapGetters } from "vuex";
import Avatar from "vue-avatar";
const Pagination = () => import("@/components/oj/common/Pagination");
export default {
  name: "acm-rank",
  components: {
    Pagination,
    Avatar,
  },
  data() {
    return {
      page: 1,
      limit: 30,
      searchUser: null,
      isNew: false,
      dataRank: [],
      loadingTable: false,
      screenWidth: 768,
      columns: [
        { title: "m.codeforcesAc", field: "codeforcesAc" },
        { title: "m.nowcoderAc", field: "nowcoderAc" },
        { title: "m.vjudgeAc", field: "vjudgeAc" },
        { title: "m.pojAc", field: "pojAc" },
        { title: "m.atcodeAc", field: "atcodeAc" },
        { title: "m.leetcodeAc", field: "leetcodeAc" },
        { title: "m.sum", field: "sum" },
      ],
    };
  },
  created() {
    this.screenWidth = window.screen.width;
    const that = this;
    window.onresize = () => {
      return (() => {
        that.screenWidth = document.documentElement.clientWidth;
      })();
    };
  },
  mounted() {
    this.page = parseInt(this.$route.query.page) || 1;
    this.isNew = this.$route.query.isNew === "true" || false;
    this.searchUser = this.$route.query.searchUser || null;
    this.getRankData(1);
  },
  methods: {
    getRankData(page) {
      this.loadingTable = true;
      const type = this.isNew ? RULE_TYPE.NewOJ : RULE_TYPE.OJ;
      api.getUserRank(page, this.limit, type, this.searchUser).then(
        (res) => {
          this.dataRank = res.data.data.records;
          this.loadingTable = false;
        },
        (err) => {
          this.loadingTable = false;
        }
      );
    },
    seqMethod({ rowIndex }) {
      return this.limit * (this.page - 1) + rowIndex + 1;
    },
    getInfoByUsername(uid, username) {
      this.$router.push({
        path: "/user-home",
        query: { uid, username },
      });
    },
    getACRate(ac, total) {
      return utils.getACRate(ac, total);
    },
    nicknameColor(nickname) {
      let typeArr = ["", "success", "info", "danger", "warning"];
      let index = nickname.length % 5;
      return typeArr[index];
    },
    handleOnlyNew() {
      this.$router.push({
        name: "OJ Rank",
        query: { isNew: this.isNew },
      });
      this.getRankData(1);
    },
  },
  computed: {
    ...mapGetters(["isAuthenticated", "userInfo"]),
  },
};
</script>

<style scoped>
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
.user-avatar {
  margin-right: 5px !important;
  vertical-align: middle;
}
@media screen and (min-width: 768px) {
  .el-input-group {
    width: 50%;
  }
}
@media screen and (min-width: 1050px) {
  .el-input-group {
    width: 30%;
  }
}
.nav-list {
  display: flex;
  list-style: none;
}

.nav-list li {
  display: inline-block;
  margin-right: 100px;
}
.selected {
  color: #409eff;
}
.panel-title-oj {
  font-size: 2em;
  font-weight: 500;
  line-height: 30px;
}
.filter-right {
  float: right;
  margin-top: 15px;
}
</style>
