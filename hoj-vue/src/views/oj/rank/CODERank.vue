<template>
  <el-row type="flex" justify="space-around">
    <el-col :span="24">
      <el-card :padding="10">
        <div slot="header">
          <ul class="nav-list">
            <li>
              <span class="panel-title-oj">{{ $t("m.NavBar_Code_Rank") }}</span>
            </li>
          </ul>
          <div class="filter-left">{{ $t("m.Limited_Tips") }}</div>
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
        <template v-if="isMainAdminRole">
          <vxe-table-column :title="$t('m.RealName')" min-width="80">
            <template v-slot="{ row }">
              <span>{{ row.realname }}</span>
            </template>
          </vxe-table-column>
          <vxe-table-column :title="$t('m.Course')" min-width="80">
            <template v-slot="{ row }">
              <span>{{ row.course }}</span>
            </template>
          </vxe-table-column>
        </template>
        <vxe-table-column
          v-for="column in columns"
          :key="column.title"
          :title="$t(column.title)"
          :min-width="90"
        >
          <template v-slot="{ row }">
            <span>
              <!-- 直接使用预处理后的数据 -->
              <span>{{ codeMap[row.uid][column.title] || "-" }}</span>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column
          :title="$t('m.Code_Lines')"
          min-width="300"
          show-overflow="ellipsis"
          align="left"
        >
          <template v-slot="{ row }">
            <span v-katex class="rank-signature-body" v-if="row.sum">{{ row.sum }}</span>
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
  name: "code-rank",
  components: {
    Pagination,
    Avatar,
  },
  data() {
    return {
      page: 1,
      limit: 30,
      searchUser: null,
      dataRank: [],
      loadingTable: false,
      screenWidth: 768,
      columns: [],
      codeMap: {},
      maxRecords: 1000, // 最大记录数限制
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
    this.searchUser = this.$route.query.searchUser || null;
    this.getRankData(1);
  },
  methods: {
    getRankData(page) {
      this.loadingTable = true;

      // 计算最大页数以确保 limit * page 不超过 1000
      const maxPage = Math.floor(this.maxRecords / this.limit);
      if (this.page > maxPage) {
        this.page = Math.max(1, maxPage - 1); // 确保 page 不小于 1，并转换为整数
        page = this.page;
      }

      api.getUserRank(page, this.limit, RULE_TYPE.Code, this.searchUser).then(
        (res) => {
          this.dataRank = res.data.data.records;
          this.getLanguages();

          this.loadingTable = false;
        },
        (err) => {
          this.loadingTable = false;
        }
      );
    },
    getLanguages() {
      // 得到当页用户的排序后的语言列表
      const counts = {};

      this.codeMap = this.dataRank.reduce((map, row) => {
        if (row.listJson) {
          const codes = row.listJson.reduce((acc, item) => {
            acc[item.language] = item.code;
            return acc;
          }, {});
          map[row.uid] = codes;
        }
        return map;
      }, {});

      this.dataRank.forEach((item) => {
        if (item.json) {
          try {
            const config = JSON.parse(item.json).config;

            config.forEach(({ language, code }) => {
              if (language) {
                counts[language] = (counts[language] || 0) + code;
              }
            });
          } catch (e) {
            console.error("Error parsing JSON:", e);
          }
        }
      });

      // 按值降序排序并生成 columns
      this.columns = Object.entries(counts)
        .sort(([, countA], [, countB]) => countB - countA)
        .map(([language]) => ({ title: language, field: language }));
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
  },
  computed: {
    ...mapGetters(["isAuthenticated", "userInfo", "isMainAdminRole"]),
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
.filter-left {
  float: left;
  margin-top: 25px;
}
</style>
