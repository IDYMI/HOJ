<template>
  <div class="view">
    <el-card shadow :padding="10">
      <div slot="header">
        <template v-if="isAdmin">
          <span class="panel-title home-title">{{ $t("m.ACM_StatisticRank") }}</span>
        </template>
        <template v-else>
          <ul class="nav-list">
            <li>
              <span
                class="panel-title-acm"
              >{{ title ? $t("m.ACM_StatisticRank") + " - " + title : $t("m.ACM_StatisticRank") }}</span>
            </li>
          </ul>
        </template>
      </div>
      <el-form label-position="top" @submit.native.prevent>
        <el-row :gutter="20">
          <template v-if="isAdmin">
            <el-col>
              <!-- 选择的cids -->
              <el-form-item :label="$t('m.Generate_Title')" required>
                <el-input style="width:50%;" v-model="title" :placeholder="$t('m.Generate_Title')"></el-input>
              </el-form-item>
              <el-form-item required>
                <template #label>
                  {{ $t('m.The_Selected_Cids') }}
                  <el-popover placement="right" trigger="hover">
                    <p>{{ $t('m.The_Selected_Cids_Tips') }}</p>
                    <i slot="reference" class="el-icon-question"></i>
                  </el-popover>
                </template>
                <!-- 快速输入框 -->
                <el-input
                  :placeholder="$t('m.The_Input_Cids')"
                  size="medium"
                  class="input-new-cids"
                  v-model="cids"
                  :trigger-on-focus="true"
                  clearable
                  @keyup.enter.native="getStatisticRankCids"
                  @blur="getStatisticRankCids"
                ></el-input>
                <el-tag
                  v-for="(cid, index) in contestCids"
                  closable
                  :close-transition="false"
                  :key="cid"
                  type="warning"
                  size="medium"
                  @close="removeCid(index, cid)"
                  style="margin-right: 7px; margin-top: 4px"
                >{{ cid }}</el-tag>
                <el-input
                  v-if="inputVisible"
                  size="medium"
                  class="input-new-cid"
                  v-model="cid"
                  :trigger-on-focus="true"
                  @keyup.enter.native="addCid"
                  @blur="addCid"
                ></el-input>
                <el-tooltip effect="dark" :content="$t('m.Add')" placement="top" v-else>
                  <el-button
                    class="button-new-tag"
                    size="small"
                    @click="inputVisible = true"
                    icon="el-icon-plus"
                  ></el-button>
                </el-tooltip>
                <!-- 快速删除 -->
                <el-tooltip
                  style="margin-left: 7px;"
                  effect="dark"
                  :content="$t('m.Delete_All')"
                  placement="top"
                >
                  <el-button size="small" @click="clearAllCid()" icon="el-icon-delete"></el-button>
                </el-tooltip>
              </el-form-item>
            </el-col>

            <el-col v-if="cfVisible || vjVisible || hduVisible">
              <el-form-item>
                <template #label>
                  {{ $t('m.Ranks_Account') }}
                  <el-popover placement="right" trigger="hover">
                    <p>{{ $t('m.Ranks_Account_Tips') }}</p>
                    <i slot="reference" class="el-icon-question"></i>
                  </el-popover>
                </template>

                <div
                  v-for="(visible, platform) in { cf: cfVisible, vj: vjVisible, hdu: hduVisible }"
                  :key="platform"
                >
                  <el-col v-if="visible" :xs="8" :md="8">
                    <div style="display: flex; align-items: center;">
                      <span>{{ platform.toUpperCase() }}</span>
                      <el-select
                        style="margin-left: 15px;"
                        size="small"
                        class="difficulty-select"
                        placeholder="Enter the username"
                        v-model="account[platform]"
                      >
                        <el-option
                          :label="value"
                          :value="value"
                          v-for="(value, index) in switchConfig[`${platform}UsernameList`]"
                          :key="index"
                        ></el-option>
                      </el-select>
                    </div>
                  </el-col>
                </div>
              </el-form-item>
            </el-col>

            <el-col>
              <el-card>
                <p>1. {{ $t('m.Import_User_Tips1') }}</p>
                <p>2. {{ $t('m.Import_User_Tips9') }}</p>
                <p>3. {{ $t('m.Import_User_Tips10') }}</p>
                <p>4. {{ $t('m.Import_User_Tips5') }}</p>
                <el-upload
                  action
                  :show-file-list="false"
                  accept=".csv"
                  :before-upload="handleUsersCSV"
                >
                  <el-button
                    size="small"
                    icon="el-icon-folder-opened"
                    type="primary"
                  >{{ $t('m.Choose_File') }}</el-button>
                </el-upload>
              </el-card>
            </el-col>
          </template>
          <el-col :md="8" :xs="24" v-if="isAdmin">
            <div class="contest-rank-search contest-rank-filter">
              <el-input
                :placeholder="$t('m.Enter_Contest_Percents')"
                v-model="percents"
                @keyup.enter.native="getStatisticRankData(page)"
              ></el-input>
            </div>
          </el-col>
          <el-col :md="8" :xs="24">
            <div class="contest-rank-search contest-rank-filter">
              <el-input
                :placeholder="$t('m.Contest_Rank_Search_Placeholder')"
                v-model="keyword"
                @keyup.enter.native="getStatisticRankData(page)"
              >
                <el-button
                  slot="append"
                  icon="el-icon-search"
                  class="search-btn"
                  @click="getStatisticRankData(page)"
                ></el-button>
              </el-input>
            </div>
          </el-col>
          <el-col :md="8" :xs="24" v-if="!isAdmin">
            <div class="contest-rank-config">
              <el-popover trigger="hover" placement="left-start">
                <el-button round size="small" slot="reference">{{ $t("m.Contest_Rank_Setting") }}</el-button>
                <div id="switches">
                  <el-row>
                    <el-col :span="24">
                      <el-button
                        type="primary"
                        size="small"
                        @click="downloadRankCSV"
                      >{{ $t("m.Download_as_CSV") }}</el-button>
                    </el-col>
                  </el-row>
                </div>
              </el-popover>
            </div>
          </el-col>
        </el-row>
        <div>
          <vxe-grid
            round
            border
            auto-resize
            size="medium"
            align="center"
            ref="ACMContestStatic"
            :data="dataRank"
            :cell-class-name="cellClassName"
          >
            <vxe-table-column
              field="rank"
              width="50"
              fixed="left"
              :title="$t('m.Contest_Rank_Seq')"
            >
              <template v-slot="{ row }">
                <template>
                  <RankBox :num="row.rank"></RankBox>
                </template>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="username"
              fixed="left"
              v-if="!isMobileView"
              min-width="300"
              :title="$t('m.User')"
              header-align="center"
              align="left"
            >
              <template v-slot="{ row }">
                <div class="contest-rank-user-box">
                  <span>
                    <avatar
                      :username="row.rankShowName"
                      :inline="true"
                      :size="37"
                      color="#FFF"
                      :src="row.avatar"
                      :title="row.rankShowName"
                    ></avatar>
                  </span>
                  <span class="contest-rank-user-info">
                    <a @click=" getUserHomeByUsername(row.uid, row.username, row.synchronous)  ">
                      <span class="contest-username" :title="row.rankShowName">
                        <span class="contest-rank-flag" v-if="row.uid == userInfo.uid">Own</span>
                        <span class="contest-rank-flag" v-if="row.rank == -1">Star</span>
                        <span class="contest-rank-flag" v-if="row.gender == 'female'">Girl</span>
                        {{ row.rankShowName }}
                      </span>
                      <span
                        class="contest-school"
                        v-if="row.school"
                        :title="row.school"
                      >{{ row.school }}</span>
                    </a>
                  </span>
                </div>
              </template>
            </vxe-table-column>
            <vxe-table-column
              field="username"
              v-else
              min-width="300"
              :title="$t('m.User')"
              header-align="center"
              align="left"
            >
              <template v-slot="{ row }">
                <div class="contest-rank-user-box">
                  <span>
                    <avatar
                      :username="row.rankShowName"
                      :inline="true"
                      :size="37"
                      color="#FFF"
                      :src="row.avatar"
                      :title="row.rankShowName"
                    ></avatar>
                  </span>
                  <span class="contest-rank-user-info">
                    <a @click="getUserHomeByUsername(row.uid, row.username, row.synchronous)">
                      <span class="contest-username" :title="row.rankShowName">
                        <span class="contest-rank-flag" v-if="row.uid == userInfo.uid">Own</span>
                        <span class="contest-rank-flag" v-if="row.rank == -1">Star</span>
                        <span class="contest-rank-flag" v-if="row.gender == 'female'">Girl</span>
                        {{ row.rankShowName }}
                      </span>
                      <span
                        class="contest-school"
                        v-if="row.school"
                        :title="row.school"
                      >{{ row.school }}</span>
                    </a>
                  </span>
                </div>
              </template>
            </vxe-table-column>
            <vxe-table-column field="realname" width="150" fixed="left" :title="$t('m.RealName')">
              <template v-slot="{ row }">
                <span>{{ row.realname }}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="rating" :title="$t('m.AC')" min-width="60">
              <template v-slot="{ row }">
                <span>{{ row.ac }}</span>
              </template>
            </vxe-table-column>
            <vxe-table-column field="totalTime" :title="$t('m.TotalTime')" min-width="60">
              <template v-slot="{ row }">
                <el-tooltip effect="dark" placement="top">
                  <div slot="content">{{ parseTimeToSpecific(row.totalTime) }}</div>
                  <span>{{ parseInt(row.totalTime / 60) }}</span>
                </el-tooltip>
              </template>
            </vxe-table-column>
            <vxe-table-column
              min-width="74"
              v-for="(cid, index) in contestCids"
              :key="index"
              :field="index"
            >
              <template v-slot:header>
                <span>
                  <el-tooltip v-if="getContestTitle(cid)" effect="dark" placement="top">
                    <div slot="content">{{ getContestTitle(cid) }}</div>
                    <span
                      class="emphasis"
                      :style="{ color: '#495060', cursor: 'pointer' }"
                      @click="getContestDetailsById(cid)"
                    >{{ cid }}</span>
                  </el-tooltip>
                  <span
                    v-else
                    class="emphasis"
                    :style="{ color: '#495060', cursor: 'pointer' }"
                    @click="getContestDetailsById(cid)"
                  >{{ cid }}</span>
                </span>
              </template>
              <template v-slot="{ row }">
                <span v-if="row.submissionInfo[cid]">
                  <span v-if="row.submissionInfo[cid].link">
                    <span
                      v-if="row.submissionInfo[cid].ac"
                      class="submission-time"
                      style="font-weight: 600; font-size: 14px;"
                    >
                      {{ row.submissionInfo[cid].ac }}
                      <br />
                    </span>
                  </span>
                  <span v-else @click="getUserACSubmit(row.username, cid)" class="submission-hover">
                    <span
                      v-if="row.submissionInfo[cid].ac"
                      class="submission-time"
                      style="color: rgb(87, 163, 243); font-weight: 600; font-size: 14px;"
                    >
                      {{ row.submissionInfo[cid].ac }}
                      <br />
                    </span>
                  </span>
                </span>
              </template>
            </vxe-table-column>
          </vxe-grid>
        </div>
        <div style="display: flex; justify-content: flex-end;">
          <Pagination
            :total="total"
            :page-size.sync="limit"
            :page-sizes="[10, 30, 50, 100, 300, 500]"
            :current.sync="page"
            @on-change="getStatisticRankData"
            @on-page-size-change="getStatisticRankData(1)"
            :layout="'prev, pager, next, sizes'"
          ></Pagination>
        </div>
        <div v-if="isAdmin">
          <div v-if="captcha_img">
            <div style="margin-bottom: 10px;">
              <el-image :src="captcha_img" fit="cover" style="width: 200px;"></el-image>
            </div>
            <el-form-item :label="$t('m.Enter_Captcha')" required>
              <el-input
                size="medium"
                class="input-new-cid"
                v-model="captcha"
                placeholder="Enter captcha"
              ></el-input>
            </el-form-item>
          </div>
          <el-button
            type="primary"
            size="small"
            @click="getStatisticRankData(page, true)"
          >{{ $t("m.Generate") }}</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import Avatar from "vue-avatar";
import { mapActions } from "vuex";
const Pagination = () => import("@/components/oj/common/Pagination");
const RankBox = () => import("@/components/oj/common/RankBox");
import time from "@/common/time";
import utils from "@/common/utils";
import api from "@/common/api";
import myMessage from "@/common/message";
import papa from "papaparse"; // csv插件

export default {
  name: "ACMContestStatic",
  components: {
    Pagination,
    RankBox,
    Avatar,
  },
  data() {
    return {
      total: 0,
      page: 1,
      limit: 30,
      dataRank: [],
      keyword: null,
      contestCids: [],
      contestPercents: [],
      cid: "",
      cids: "",
      percents: "",
      title: null,
      inputVisible: false,
      cfVisible: false,
      vjVisible: false,
      hduVisible: false,
      switchConfig: {
        cfUsernameList: [],
        vjUsernameList: [],
        hduUsernameList: [],
      },
      account: {
        cf: null,
        vj: null,
        hdu: null,
      },
      captcha_img: null,
      captcha: null,
      username_dir: {},
      isAdmin: false,
      uid: null,
    };
  },
  mounted() {
    this.routeName = this.$route.name;
    if (this.routeName === "ACM Static Rank") {
      this.cids = this.$route.params.cids;
      this.isAdmin = false;
      this.getStatisticRankCids(true);
    } else {
      this.isAdmin = true;
      this.getAdminSwitchConfig();
    }
  },
  methods: {
    ...mapActions(["userInfo"]),
    getAdminSwitchConfig() {
      api.admin_getSwitchConfig().then((res) => {
        let profile = res.data.data;
        Object.keys(this.switchConfig).forEach((element) => {
          if (profile[element] !== undefined) {
            this.switchConfig[element] = profile[element];
          }
        });
      });
    },
    removeCid(index, cid) {
      this.contestCids.splice(
        this.contestCids.map((item) => item).indexOf(cid),
        1
      );
      this.contestPercents.splice(index, 1);
      this.percents = this.contestPercents.join("-");

      if (cid.startsWith("cf") || cid.startsWith("gym")) {
        this.cfVisible = false;
      } else if (cid.startsWith("vj")) {
        this.vjVisible = false;
      } else if (cid.startsWith("hdu")) {
        this.hduVisible = false;
      }
    },
    addCid(out_error = true) {
      this.cid = this.cid.replace(/(^\s*)|(\s*$)/g, "");

      const validCidPattern =
        /^(?:\d+|(?:cf|gym|vj|hdu|nowcoder|pta)\d+|xcpc.+)$/;

      if (this.cid) {
        if (validCidPattern.test(this.cid)) {
          for (var i = 0; i < this.contestCids.length; i++) {
            if (this.contestCids[i] == this.cid) {
              myMessage.warning(this.$i18n.t("m.Add_Cid_Error"));
              this.cid = "";
              return;
            }
          }
          if (this.cid.startsWith("cf") || this.cid.startsWith("gym")) {
            this.cfVisible = true;
          } else if (this.cid.startsWith("vj")) {
            this.vjVisible = true;
          } else if (this.cid.startsWith("hdu")) {
            this.hduVisible = true;
          }

          this.contestCids.push(this.cid);
          this.contestPercents.push("100%");
        } else {
          if (!out_error) {
            myMessage.warning(this.$i18n.t("m.Invalid_Cid_Error"));
          }
        }
        this.cid = "";
        this.inputVisible = false;
      }
    },
    clearAllCid() {
      this.contestCids = [];
      this.contestPercents = [];
    },
    getStatisticRankData(page = 1, isSave = false) {
      let cids = this.contestCids.join("+") + "+";

      // 提前处理 keyword 和 captcha
      const trimmedKeyword = this.keyword ? this.keyword.trim() : null;
      const trimmedCaptcha = this.captcha ? this.captcha.trim() : null;

      if (this.isAdmin) {
        if (this.captcha_img && !trimmedCaptcha) {
          myMessage.warning(this.$i18n.t("m.Enter_Captcha"));
          return;
        }

        const data = {
          currentPage: page,
          limit: this.limit,
          cids: cids,
          percents: this.percents,
          statisticAccountDto: this.account,
          keyword: trimmedKeyword,
          captcha: trimmedCaptcha,
          data: this.username_dir,
          title: this.title,
          isSave: isSave,
        };

        api
          .admin_addStatisticRank(data)
          .then((response) => {
            this.handleResponse(response);
            if (isSave) {
              this.$router.push({ name: "admin-ranks-list" });
            }
          })
          .catch(this.handleCaptchaError); // 捕获并处理错误
      } else {
        api
          .getStatisticRank(page, this.limit, this.cids, trimmedKeyword)
          .then(this.handleResponse, this.clearCaptcha);
      }
    },
    handleResponse(res) {
      this.total = res.data.data.total;
      this.applyToTable(res.data.data.records);
      this.clearCaptcha();
    },
    handleCaptchaError(error) {
      const msg = error.data.msg;
      if (msg.startsWith("[VJ] Your Captcha :")) {
        this.captcha_img = msg.split(":")[1].trim();
        this.captcha = null;
      } else {
        this.clearCaptcha();
      }
    },
    clearCaptcha() {
      this.captcha_img = null;
      this.captcha = null;
    },

    getStatisticRankCids(out_error = false) {
      api.getStatisticRankCids(this.cids).then(
        (res) => {
          let cids = res.data.msg || this.cids;

          cids.split("+").forEach((cid) => {
            if (cid) {
              this.cid = cid;
              this.addCid(out_error);
            }
          });
          this.percents = this.contestPercents.join("-");
          if (!this.isAdmin) this.getStatisticRankData(1);
        },
        (_) => {}
      );
    },
    getRankShowName(rankShowName, username) {
      let finalShowName = rankShowName;
      if (
        rankShowName == null ||
        rankShowName == "" ||
        rankShowName.trim().length == 0
      ) {
        finalShowName = username;
      }
      return finalShowName;
    },
    getUserACSubmit(username, contestID) {
      this.$router.push({
        path: "/contest/" + contestID + "/submissions",
        query: { username: username, status: 0 },
      });
    },
    getUserHomeByUsername(uid, username, synchronous = false) {
      if (!synchronous) {
        this.$router.push({
          name: "UserHome",
          query: { username: username, uid: uid },
        });
      }
    },
    getContestDetailsById(cid) {
      let foundLink = null;
      this.dataRank.forEach((rank, i) => {
        let info = rank.submissionInfo;
        Object.keys(info).forEach((contestID) => {
          if (contestID.toString() == cid.toString()) {
            foundLink = info[contestID].link;
            return;
          }
        });
        if (foundLink) {
          return;
        }
      });
      if (foundLink) {
        window.open(foundLink, "_blank");
      } else {
        this.$router.push({
          name: "ContestDetails",
          params: {
            contestID: cid,
          },
        });
      }
    },
    cellClassName({ row, rowIndex, column, columnIndex }) {
      if (row.username == this.userInfo.username) {
        if (
          column.property == "rank" ||
          column.property == "rating" ||
          column.property == "totalTime" ||
          column.property == "username" ||
          column.property == "realname"
        ) {
          return "own-submit-row";
        }
      }

      if (column.property == "username" && row.userCellClassName) {
        return row.userCellClassName;
      }
    },
    getContestTitle(cid) {
      let foundTitle = null;
      this.dataRank.forEach((rank, i) => {
        let info = rank.submissionInfo;
        Object.keys(info).forEach((contestID) => {
          if (contestID.toString() == cid.toString()) {
            foundTitle = info[contestID].title;
            return;
          }
        });
        if (foundTitle) {
          return;
        }
      });
      return foundTitle;
    },
    applyToTable(dataRank) {
      dataRank.forEach((rank, i) => {
        if (!this.isAdmin) {
          if (rank.title) this.title = rank.title;
          if (rank.percents) this.percents = rank.percents;
        }
        let info = rank.submissionInfo;
        let cellClass = {};
        Object.keys(info).forEach((contestID) => {
          rank[contestID] = info[contestID];
        });
        rank.cellClassName = cellClass;
        if (rank.gender == "female") {
          rank.userCellClassName = "bg-female";
        }
        rank.rankShowName = this.getRankShowName(rank[false], rank.username);
      });
      this.dataRank = dataRank;
    },
    parseTimeToSpecific(totalTime) {
      return time.secondFormat(totalTime);
    },
    downloadRankCSV() {
      let cids = "";
      cids = this.cids.replace(/\+/g, "%2B");

      let url = `/api/file/download-statistic-rank?cids=${cids}`;

      if (this.keyword) url += `&keyword=${keyword}`;

      utils.downloadFile(url);
    },
    handleUsersCSV(file) {
      papa.parse(file, {
        complete: (results) => {
          let data = results.data.filter((user) => {
            return user[0] && user[1];
          });
          let delta = results.data.length - data.length;
          if (delta > 0) {
            myMessage.warning(
              delta + this.$i18n.t("m.Generate_Skipped_Reason2")
            );
          }
          let transformedData = data.reduce((acc, item) => {
            let key = item[0];
            let value = item[1];
            if (!acc[key]) {
              acc[key] = value;
            }
            return acc;
          }, {});

          // 直接将转换后的数据赋值给 username_dir
          this.username_dir = transformedData;
        },
        error: (error) => {
          myMessage.error(error);
        },
      });
    },
  },
  computed: {
    showTable: {
      get() {
        return this.$store.state.contest.itemVisible.table;
      },
      set(value) {
        this.$store.commit("changeContestItemVisible", {
          table: value,
        });
      },
    },
    isMobileView() {
      return window.screen.width < 768;
    },
  },
};
</script>

<style scoped>
.nav-list {
  display: flex;
  /* justify-content: center;
  align-items: center; */
  list-style: none;
}

.panel-title-acm {
  font-size: 2em;
  font-weight: 500;
  line-height: 30px;
}

.nav-list li {
  display: inline-block;
  margin-right: 100px;
}

label {
  display: inline-block;
  margin-right: 5px;
  text-align: center; /* 将文字居中 */
}

/* 可选样式，用于将复选框和文字垂直居中 */
input[type="checkbox"] {
  vertical-align: middle;
}
.echarts {
  margin: 20px auto;
  height: 400px;
  width: 100%;
}
/deep/.el-card__body {
  padding: 20px !important;
  padding-top: 0px !important;
}

.screen-full {
  margin-right: 8px;
}

#switches p {
  margin-top: 5px;
}
#switches p:first-child {
  margin-top: 0;
}
#switches p span {
  margin-left: 8px;
  margin-right: 4px;
}
.vxe-cell p,
.vxe-cell span {
  margin: 0;
  padding: 0;
}

/deep/.vxe-table .vxe-header--column:not(.col--ellipsis) {
  padding: 4px 0 !important;
}

/deep/.vxe-table .vxe-body--column {
  line-height: 20px !important;
  padding: 0 !important;
}
@media screen and (max-width: 768px) {
  /deep/.el-card__body {
    padding: 0 !important;
  }
}
a.emphasis {
  color: #495060 !important;
}
a.emphasis:hover {
  color: #2d8cf0 !important;
}
/deep/.vxe-body--column {
  min-width: 0;
  height: 48px;
  box-sizing: border-box;
  text-align: left;
  text-overflow: ellipsis;
  vertical-align: middle;
}
/deep/.vxe-table .vxe-cell {
  padding-left: 5px !important;
  padding-right: 5px !important;
}
.submission-time {
  font-size: 15.6px;
  font-family: Roboto, sans-serif;
}
.submission-error {
  font-weight: 400;
}
.input-new-cid {
  width: 200px;
}
.input-new-cids {
  width: 400px;
  margin-right: 10px;
}
</style>
