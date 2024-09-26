<template>
  <div>
    <el-card>
      <div slot="header">
        <template v-if="isAdmin">
          <span class="panel-title home-title">{{ $t("m.Ranks_Admin") }}</span>
        </template>
        <template v-else>
          <ul class="nav-list">
            <span class="panel-title-acm">{{ $t("m.Ranks_Admin") }}</span>
          </ul>
        </template>
        <div class="filter-row" v-if="isAdmin">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="goCreateStatistic"
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
        </div>
      </div>
      <vxe-table
        :loading="loading"
        ref="xTable"
        :data="statisticList"
        auto-resize
        stripe
        align="center"
      >
        <vxe-table-column field="scid" width="250" title="ID"></vxe-table-column>
        <vxe-table-column field="title" min-width="150" :title="$t('m.Title')" show-overflow></vxe-table-column>
        <vxe-table-column field="cids" min-width="150" :formatter="formatCids"></vxe-table-column>
        <vxe-table-column field="percents" min-width="150" :formatter="percents"></vxe-table-column>

        <vxe-table-column :title="$t('m.Visible')" min-width="80" v-if="isAdmin">
          <template v-slot="{ row }">
            <el-switch
              v-model="row.visible"
              :disabled="!isMainAdminRole && userInfo.username != row.author"
              @change="changeStatisticVisible(row.scid, row.visible, row.author)"
            ></el-switch>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <div v-if="isAdmin">
              <template v-if="isMainAdminRole || userInfo.username == row.author">
                <div style="margin-bottom:10px">
                  <el-tooltip
                    effect="dark"
                    :content="$t('m.Delete')"
                    placement="top"
                    v-if="isSuperAdmin || userInfo.username == row.author"
                  >
                    <el-button
                      icon="el-icon-delete"
                      size="mini"
                      @click.native="deleteStatistic(row.scid)"
                      type="danger"
                    ></el-button>
                  </el-tooltip>
                </div>
              </template>
            </div>
            <div v-else>
              <el-tooltip effect="dark" :content="$t('m.Moss_View')" placement="top">
                <el-button
                  icon="el-icon-edit"
                  size="mini"
                  @click.native="goView(row.scid)"
                  type="primary"
                ></el-button>
              </el-tooltip>
            </div>
          </template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import { mapGetters } from "vuex";
import myMessage from "@/common/message";
export default {
  name: "HonorList",
  data() {
    return {
      pageSize: 10,
      total: 0,
      statisticList: [],
      keyword: "",
      loading: false,
      currentPage: 1,
      isAdmin: true,
    };
  },
  mounted() {
    this.route_name = this.$route.name;
    if (this.route_name == "admin-ranks-list") {
      this.isAdmin = true;
    } else {
      this.isAdmin = false;
    }
    this.getStatisticList(this.currentPage);
  },
  watch: {
    $route() {
      let refresh = this.$route.query.refresh == "true" ? true : false;
      if (refresh) {
        this.getStatisticList(1);
      }
    },
  },
  computed: {
    ...mapGetters(["isSuperAdmin", "isMainAdminRole", "userInfo"]),
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      this.getStatisticList(page);
    },
    getStatisticList(page) {
      this.loading = true;

      let funcName = this.isAdmin
        ? "admin_getStatisticList"
        : "getStatisticList";

      console.log(funcName);
      api[funcName](page, this.pageSize, this.keyword).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.statisticList = res.data.data.records;
        },
        () => {
          this.loading = false;
        }
      );
    },

    deleteStatistic(scid) {
      this.$confirm(this.$i18n.t("m.Delete_Statistic_Tips"), "Tips", {
        confirmButtonText: this.$i18n.t("m.OK"),
        cancelButtonText: this.$i18n.t("m.Cancel"),
        type: "warning",
      }).then(() => {
        api.admin_deleteStatistic(scid).then((res) => {
          myMessage.success(this.$i18n.t("m.Delete_successfully"));
          this.currentChange(1);
        });
      });
    },
    changeStatisticVisible(scid, visible, author) {
      api.admin_changeStatisticVisible(scid, visible, author).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
      });
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    goCreateStatistic() {
      this.$router.push({ name: "admin-ranks" });
    },
    statisticListChangeFilter() {
      this.currentPage = 1;
      this.getStatisticList();
    },
    formatCids({ cellValue }) {
      // 以 "+" 分割字符串，并用换行符 "\n" 连接
      if (typeof cellValue === "string") {
        return cellValue.split("+").join("\n");
      }
      return cellValue;
    },
    goView(scid) {
      this.$router.push({
        name: "ACM Static Rank",
        params: { cids: scid },
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
.panel-title-acm {
  font-size: 2em;
  font-weight: 500;
  line-height: 30px;
}
</style>
