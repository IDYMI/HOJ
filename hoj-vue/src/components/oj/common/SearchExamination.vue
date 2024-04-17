<template>
  <div id="main">
    <el-card ref="card">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Search_ExaminationSeat') }}</span>
        <div class="filter-row">
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_RealnameOrNumber')"
              type="search"
              style="width: 80%"
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
        <vxe-table-column field="school" min-width="200" :title="$t('m.School')"></vxe-table-column>
        <vxe-table-column field="building" width="80" :title="$t('m.Place')">
          <template v-slot="{ row }">{{ row.building }} # {{ row.room }}</template>
        </vxe-table-column>
        <vxe-table-column min-width="80" :title="$t('m.View')">
          <template v-slot="{ row }">
            <div style="margin-bottom:10px">
              <el-button
                icon="el-icon-search"
                size="mini"
                @click.native="goToView(row.eid)"
                type="primary"
              ></el-button>
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
import mMessage from "@/common/message";

export default {
  name: "SearchExamination",
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
    this.getExaminationSeatList();
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      this.getExaminationSeatList(page);
    },
    getExaminationSeatList(page) {
      this.loading = true;
      let contestId = this.$route.params.contestId;
      api
        .getExaminationSeatList(page, this.pageSize, contestId, this.keyword)
        .then(
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
    filterByKeyword() {
      this.getExaminationSeatList();
    },
    goToView(examinationRoomId) {
      this.$router.push({
        name: "get-examination-room",
        params: { examinationRoomId },
        query: {
          keyword: this.keyword,
        },
      });
    },
  },
};
</script>


