<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Honor_List') }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="goCreateHonor"
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
              v-model="selectedYear"
              @change="honorListChangeFilter"
              size="small"
              style="width: 180px"
            >
              <el-option :label="$t('m.All_Honor')" :value="'All'"></el-option>
              <el-option v-for="year in years" :key="year" :label="year" :value="year"></el-option>
            </el-select>
          </span>
          <span>
            <el-select
              v-model="type"
              @change="honorListChangeFilter"
              size="small"
              style="width: 180px"
            >
              <el-option :label="$t('m.All_Honor')" :value="'All'"></el-option>
              <el-option :label="$t('m.Honor_Gold')" value="Gold"></el-option>
              <el-option :label="$t('m.Honor_Silver')" value="Silver"></el-option>
              <el-option :label="$t('m.Honor_Bronze')" value="Bronze"></el-option>
            </el-select>
          </span>
        </div>
      </div>
      <vxe-table
        :loading="loading"
        ref="xTable"
        :data="honorList"
        auto-resize
        stripe
        align="center"
      >
        <vxe-table-column field="id" width="80" title="ID"></vxe-table-column>
        <vxe-table-column field="title" min-width="150" :title="$t('m.Title')" show-overflow></vxe-table-column>
        <vxe-table-column field="level" width="100" :title="$t('m.Level')"></vxe-table-column>
        <vxe-table-column :title="$t('m.Type')" width="100">
          <template v-slot="{ row }">
            <el-tag
              :style="{ backgroundColor: HONOR_TYPE[row.type].color, borderColor: HONOR_TYPE[row.type].color }"
              effect="dark"
            >{{ row.type }}</el-tag>
          </template>
        </vxe-table-column>
        <vxe-table-column :title="$t('m.Visible')" min-width="80">
          <template v-slot="{ row }">
            <el-switch
              v-model="row.status"
              :disabled="!isMainAdminRole && userInfo.username != row.author"
              @change="changeHonorStatus(row.id, row.status, row.author)"
            ></el-switch>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="210" :title="$t('m.Info')">
          <template v-slot="{ row }">
            <p>Awarded Time: {{ row.date | localtime }}</p>
            <p>Team Member: {{ row.teamMember }}</p>
            <p>Creator: {{ row.author }}</p>
          </template>
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Option')">
          <template v-slot="{ row }">
            <template v-if="isMainAdminRole || userInfo.username == row.author">
              <div style="margin-bottom:10px">
                <el-tooltip effect="dark" :content="$t('m.Edit')" placement="top">
                  <el-button
                    icon="el-icon-edit"
                    size="mini"
                    @click.native="goEdit(row.id)"
                    type="primary"
                  ></el-button>
                </el-tooltip>
                <el-tooltip
                  effect="dark"
                  :content="$t('m.Delete')"
                  placement="top"
                  v-if="isSuperAdmin || userInfo.username == row.author"
                >
                  <el-button
                    icon="el-icon-delete"
                    size="mini"
                    @click.native="deleteHonor(row.id)"
                    type="danger"
                  ></el-button>
                </el-tooltip>
              </div>
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
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import { HONOR_TYPE } from "@/common/constants";
import { mapGetters } from "vuex";
import myMessage from "@/common/message";
export default {
  name: "HonorList",
  data() {
    return {
      pageSize: 10,
      total: 0,
      honorList: [],
      type: "All",
      selectedYear: "All",
      keyword: "",
      loading: false,
      currentPage: 1,
      HONOR_TYPE: {},
      years: [],
    };
  },
  mounted() {
    this.getHonorList(this.currentPage);
    this.HONOR_TYPE = Object.assign({}, HONOR_TYPE);
    this.generateYears();
  },
  watch: {
    $route() {
      let refresh = this.$route.query.refresh == "true" ? true : false;
      if (refresh) {
        this.getHonorList(1);
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
      this.getHonorList(page);
    },
    getHonorList(page) {
      this.loading = true;
      api
        .admin_getHonorList(
          page,
          this.pageSize,
          this.keyword,
          this.type,
          this.selectedYear
        )
        .then(
          (res) => {
            this.loading = false;
            this.total = res.data.data.total;
            this.honorList = res.data.data.records;
          },
          (res) => {
            this.loading = false;
          }
        );
    },
    goEdit(honorId) {
      this.$router.push({
        name: "admin-edit-honor",
        params: { honorId },
      });
    },
    deleteHonor(honorId) {
      this.$confirm(this.$i18n.t("m.Delete_Honor_Tips"), "Tips", {
        confirmButtonText: this.$i18n.t("m.OK"),
        cancelButtonText: this.$i18n.t("m.Cancel"),
        type: "warning",
      }).then(() => {
        api.admin_deleteHonor(honorId).then((res) => {
          myMessage.success(this.$i18n.t("m.Delete_successfully"));
          this.currentChange(1);
        });
      });
    },
    changeHonorStatus(honorId, status, author) {
      api.admin_changeHonorStatus(honorId, status, author).then((res) => {
        myMessage.success(this.$i18n.t("m.Update_Successfully"));
      });
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    goCreateHonor() {
      this.$router.push({ name: "admin-create-honor" });
    },
    honorListChangeFilter() {
      this.currentPage = 1;
      this.getHonorList();
    },
    generateYears() {
      const currentYear = new Date().getFullYear();
      for (let year = currentYear; year >= 2009; year--) {
        this.years.push(year);
      }
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
