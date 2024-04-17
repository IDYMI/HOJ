<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.ExaminationRoom_List') }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="goCreateExaminationRoom"
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
        :data="examinationRoomList"
        auto-resize
        stripe
        align="center"
      >
        <vxe-table-column field="eid" width="80" title="ID"></vxe-table-column>
        <vxe-table-column field="school" width="150" :title="$t('m.School')"></vxe-table-column>
        <vxe-table-column field="building" width="80" :title="$t('m.Place')">
          <template v-slot="{ row }">{{ row.building }} # {{ row.room }}</template>
        </vxe-table-column>
        <vxe-table-column field="count" width="80" :title="$t('m.Count')"></vxe-table-column>
        <vxe-table-column :title="$t('m.Room_Size')" width="160" align="center">
          <template v-slot="{ row }">{{ row.maxRow }} / {{ row.maxCol }}</template>
        </vxe-table-column>
        <vxe-table-column min-width="210" :title="$t('m.Info')">
          <template v-slot="{ row }">
            <p>Created Time: {{ row.gmtCreate | localtime }}</p>
            <p>Update Time: {{ row.gmtModified | localtime }}</p>
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
                    @click.native="goEdit(row.eid)"
                    type="primary"
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
import { mapGetters } from "vuex";
import myMessage from "@/common/message";
export default {
  name: "ExaminationRoomList",
  data() {
    return {
      pageSize: 10,
      total: 0,
      examinationRoomList: [],
      keyword: "",
      loading: false,
      excludeAdmin: true,
      currentPage: 1,
      currentId: 1,
      downloadDialogVisible: false,
    };
  },
  mounted() {
    this.getExaminationRoomList(this.currentPage);
  },
  watch: {
    $route() {
      let refresh = this.$route.query.refresh == "true" ? true : false;
      if (refresh) {
        this.getExaminationRoomList(1);
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
      this.getExaminationRoomList(page);
    },
    getExaminationRoomList(page) {
      this.loading = true;
      api.getExaminationRoomList(page, this.pageSize, this.keyword, null).then(
        (res) => {
          this.loading = false;
          this.total = res.data.data.total;
          this.examinationRoomList = res.data.data.records;
        },
        (res) => {
          this.loading = false;
        }
      );
    },
    goEdit(examinationRoomId) {
      this.$router.push({
        name: "admin-edit-examination-room",
        params: { examinationRoomId },
      });
    },
    deleteExaminationSeat(examinationRoomId) {
      this.$confirm(this.$i18n.t("m.Delete_Training_Tips"), "Tips", {
        confirmButtonText: this.$i18n.t("m.OK"),
        cancelButtonText: this.$i18n.t("m.Cancel"),
        type: "warning",
      }).then(() => {
        api.admin_deleteExaminationSeat(examinationRoomId).then((res) => {
          myMessage.success(this.$i18n.t("m.Delete_successfully"));
          this.currentChange(1);
        });
      });
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    goCreateExaminationRoom() {
      this.$router.push({ name: "admin-create-examination-room" });
    },
    TraningListChangeFilter() {
      this.currentPage = 1;
      this.getExaminationRoomList();
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
