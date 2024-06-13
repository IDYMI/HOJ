<template>
  <el-card shadow="always">
    <div slot="header">
      <span class="panel-title">{{ $t('m.Reset_Ip') }}</span>
    </div>
    <vxe-table border="inner" stripe auto-resize align="center" :data="ipList">
      <vxe-table-column field="username" :title="$t('m.Username')" min-width="100"></vxe-table-column>
      <vxe-table-column field="realname" :title="$t('m.RealName')" min-width="100" show-overflow></vxe-table-column>
      <vxe-table-column
        field="ipList"
        :title="$t('m.SubmitIp_List')"
        min-width="150"
        :formatter="formatIpList"
      ></vxe-table-column>
      <vxe-table-column field="option" :title="$t('m.Option')" min-width="150">
        <template v-slot="{ row }">
          <el-button
            type="primary"
            size="small"
            :loading="btnLoading"
            icon="el-icon-refresh-right"
            @click="rejudgeProblem(row)"
            round
          >{{ $t('m.Reset') }}</el-button>
        </template>
      </vxe-table-column>
    </vxe-table>
  </el-card>
</template>
<script>
import { mapState, mapActions } from "vuex";
import api from "@/common/api";
import myMessage from "@/common/message";

export default {
  name: "Contest-Rejudge-Admin",
  data() {
    return {
      btnLoading: false,
      ipList: [],
    };
  },
  mounted() {
    this.contestID = this.$route.params.contestID;
    this.getContestIpList();
  },
  methods: {
    formatIpList({ cellValue }) {
      if (cellValue) {
        return cellValue.split(",").join("\n");
      }
      return "";
    },
    getContestIpList() {
      let params = { cid: this.contestID };
      api
        .getContestIpList(params)
        .then((res) => {
          this.ipList = res.data.data;
        })
        .catch(() => {});
    },
    rejudgeProblem(row) {
      this.$confirm(this.$i18n.t("m.Contest_ResetIp_Tips"), "Tips", {
        confirmButtonText: this.$i18n.t("m.OK"),
        cancelButtonText: this.$i18n.t("m.Cancel"),
        type: "warning",
      }).then(
        () => {
          let params = {
            cid: this.contestID,
            uid: row.uid,
          };
          this.btnLoading = true;
          api
            .ContestResetIp(params)
            .then((res) => {
              myMessage.success(this.$i18n.t("m.Reset_successfully"));
              this.btnLoading = false;
            })
            .catch(() => {
              this.btnLoading = false;
            });
        },
        () => {}
      );
    },
  },
};
</script>
<style scoped>
@media screen and (min-width: 1050px) {
  /deep/ .vxe-table--body-wrapper {
    overflow-x: hidden !important;
  }
}
</style>
