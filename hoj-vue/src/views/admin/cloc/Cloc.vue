<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <div class="panel-title home-title">
          {{ $t('m.Cloc_Query') }}
          <el-popover placement="right" trigger="hover">
            <p>{{ $t('m.Query_User_Tips') }}</p>
            <i slot="reference" class="el-icon-question"></i>
          </el-popover>
        </div>
        <div class="filter-row">
          <span>
            <el-button
              size="small"
              type="primary"
              @click="updateUsersCode(null)"
            >{{ $t('m.To_Query') }}</el-button>
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
            <el-date-picker
              v-model="selectStartTime"
              size="small"
              type="datetime"
              @change="changeDuration"
              :placeholder="$t('m.Start_Time')"
            ></el-date-picker>
          </span>
          <span>
            <el-date-picker
              v-model="selectEndTime"
              size="small"
              type="datetime"
              @change="changeDuration"
              :placeholder="$t('m.End_Time')"
            ></el-date-picker>
          </span>

          <span>
            <el-switch
              v-model="onlyNew"
              :active-text="$t('m.OnlyNew')"
              :width="40"
              @change="filterByNew"
              :inactive-text="$t('m.All')"
            ></el-switch>
          </span>
        </div>
      </div>
      <vxe-table
        stripe
        auto-resize
        :data="userList"
        ref="xTable"
        :loading="loadingTable"
        :checkbox-config="{ labelField: 'id', highlight: true, range: true }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"></vxe-table-column>
        <vxe-table-column field="username" :title="$t('m.User')" min-width="200" show-overflow>
          <template v-slot="{ row }">
            <span>{{ row.username }}</span>
            <span style="margin-left:2px">
              <el-tag
                effect="dark"
                size="small"
                v-if="row.titleName"
                :color="row.titleColor"
              >{{ row.titleName }}</el-tag>
            </span>
          </template>
        </vxe-table-column>
        <vxe-table-column field="realname" :title="$t('m.RealName')" min-width="140" show-overflow></vxe-table-column>
        <vxe-table-column field="email" :title="$t('m.Email')" min-width="150" show-overflow></vxe-table-column>
        <vxe-table-column field="gmtCreate" :title="$t('m.Created_Time')" min-width="150">
          <template v-slot="{ row }">{{ row.gmtCreate | localtime }}</template>
        </vxe-table-column>
        <vxe-table-column field="role" :title="$t('m.User_Type')" min-width="100">
          <template v-slot="{ row }">{{ getRole(row.roles) | parseRole }}</template>
        </vxe-table-column>
        <vxe-table-column field="sum" :title="$t('m.Code_Lines')" min-width="100">
          <template v-slot="{ row }">{{ row.sum }}</template>
        </vxe-table-column>
      </vxe-table>
      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next, sizes"
          @current-change="currentChange"
          :page-size="pageSize"
          :total="total"
          @size-change="onPageSizeChange"
          :page-sizes="[10, 30, 50, 100]"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import time from "@/common/time";
export default {
  name: "user",
  data() {
    return {
      // 一页显示的用户数
      pageSize: 10,
      // 用户总数
      total: 0,
      // 数据库查询的用户列表
      userList: [],
      // 搜索关键字
      keyword: "",
      onlyNew: false,
      loadingTable: false,
      // 当前页码
      currentPage: 1,
      selectedUsers: [],
      selectStartTime: null,
      selectEndTime: null,
      userCode: null,
      duration: 1,
    };
  },
  mounted() {
    this.getUserList(1);
  },
  methods: {
    // 切换页码回调
    currentChange(page) {
      this.currentPage = page;
      this.getUserList(page);
    },
    onPageSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.getUserList(this.currentPage);
    },
    filterByKeyword() {
      this.currentChange(1);
    },
    filterByNew() {
      this.currentChange(1);
    },
    getRole(roles) {
      return roles[0]["id"];
    },
    // 获取用户列表
    getUserList(page) {
      this.loadingTable = true;
      let type = this.onlyNew ? 2 : 1;
      api.admin_getUserList(page, this.pageSize, this.keyword, type).then(
        (res) => {
          this.loadingTable = false;
          this.total = res.data.data.total;
          this.userList = res.data.data.records;
        },
        (res) => {
          this.loadingTable = false;
        }
      );
    },
    updateUsersCode(ids) {
      if (!ids) {
        ids = this.selectedUsers;
      }
      if (ids.length > 0) {
        if (!this.duration || this.duration <= 0) {
          myMessage.error(this.$i18n.t("m.Contets_Time_Check"));
          return;
        }

        // 获取用户时间段内的代码量
        let data = {
          uidList: ids,
          startTime: this.selectStartTime,
          endTime: this.selectEndTime,
        };

        // 发送查询请求
        this.loadingTable = true;
        api.getUserCodeRecord(data).then(
          (res) => {
            this.userCode = res.data.data;

            // 构建 uid 到 sum 的映射
            const uidToSum = this.userCode.reduce((acc, item) => {
              acc[item.uid] = item.sum;
              return acc;
            }, {});

            // 给 userList 中的字典赋值
            this.userList.forEach((user) => {
              if (uidToSum[user.uid] !== undefined) {
                user["sum"] = uidToSum[user.uid];
              }
            });

            // 强制刷新 vxe-table
            this.$nextTick(() => {
              if (this.$refs.xTable) {
                this.$refs.xTable.refreshData();
              }
            });
            this.loadingTable = false;
          },
          (err) => {
            this.loadingTable = false;
          }
        );
      } else {
        myMessage.warning(
          this.$i18n.t("m.The_number_of_users_selected_cannot_be_empty")
        );
      }
    },
    // 用户表部分勾选 改变选中的内容
    handleSelectionChange({ records }) {
      this.selectedUsers = [];
      for (let num = 0; num < records.length; num++) {
        this.selectedUsers.push(records[num].uid);
      }
    },
    // 一键全部选中，改变选中的内容列表
    handlechangeAll() {
      let userList = this.$refs.xTable.getCheckboxRecords();
      this.selectedUsers = [];
      for (let num = 0; num < userList.length; num++) {
        this.selectedUsers.push(userList[num].uid);
      }
    },
    changeDuration() {
      let start = this.selectStartTime;
      let end = this.selectEndTime;
      if (start !== null && end !== null) {
        let durationMS = time.durationMs(start, end);
        if (durationMS < 0) {
          this.durationText = this.$i18n.t("m.Contets_Time_Check");
          this.duration = 0;
          return;
        }
        if (start != "" && end != "") {
          this.durationText = time.formatSpecificDuration(start, end);
          this.duration = durationMS;
        }
      }
    },
  },
  computed: {
    selectedUserIDs() {
      let ids = [];
      for (let user of this.selectedUsers) {
        ids.push(user.id);
      }
      return ids;
    },
    userInfo() {
      return this.$store.getters.userInfo;
    },
  },
};
</script>

<style scoped>
.import-user-icon {
  color: #555555;
  margin-left: 4px;
}

.userPreview {
  padding-left: 10px;
}

/deep/ .el-tag--dark {
  border-color: #fff;
}
/deep/.el-dialog__body {
  padding-bottom: 0;
}
/deep/.el-form-item {
  margin-bottom: 10px !important;
}
.notification p {
  margin: 0;
  text-align: left;
}
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
</style>
