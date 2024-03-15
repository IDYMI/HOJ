<template>
  <el-card class="box-card">
    <div slot="header" class="clearfix">
      <span class="panel-title">{{$t('m.ScrollBoard_Parameter_Config')}}</span>
    </div>
    <el-alert
      :title="$t('m.Formula_for_calculating_the_number_of_medals')"
      type="success"
      show-icon
    >
      <template slot>
        <p>{{ $i18n.t('m.Number_of_gold_medals') }} : {{this.total}} × 10% = {{this.goldMedal}}</p>
        <p>{{ $i18n.t('m.Number_of_silver_medals') }} : {{this.total}} × 20% = {{this.silverMedal}}</p>
        <p>{{ $i18n.t('m.Number_of_bronze_medals') }} : {{this.total}} × 30% = {{this.bronzeMedal}}</p>
      </template>
    </el-alert>
    <el-form>
      <el-form-item :label="$t('m.Contest_ID')">
        <el-input v-model="cid" size="small" disabled></el-input>
      </el-form-item>
      <el-form-item :label="$t('m.Number_of_gold_medals')">
        <el-input v-model="goldMedal" size="small"></el-input>
      </el-form-item>
      <el-form-item :label="$t('m.Number_of_silver_medals')">
        <el-input v-model="silverMedal" size="small"></el-input>
      </el-form-item>
      <el-form-item :label="$t('m.Number_of_bronze_medals')">
        <el-input v-model="bronzeMedal" size="small"></el-input>
      </el-form-item>
      <el-form-item :label="$t('m.Whether_to_remove_the_star_user')">
        <el-switch v-model="removeStar"></el-switch>
      </el-form-item>
    </el-form>
    <div style="text-align:center">
      <template v-if="!contestEnded">
        <el-popconfirm
          :title="$t('m.Contest_Non_Ended_But_Want_to_Scroll_Board')"
          @confirm="goScrollBoard"
        >
          <el-button
            style="padding: 3px 0; font-size: 16px;"
            type="text"
            slot="reference"
          >{{$t('m.Start_Rolling')}}</el-button>
          <el-button
            style="padding: 3px 0; font-size: 16px;"
            type="text"
            @click="goResolver"
          >{{$t('m.Resolver_Rolling')}}</el-button>
        </el-popconfirm>
      </template>
      <template v-else>
        <el-button
          style="padding: 3px 0; font-size: 16px;"
          type="text"
          @click="goScrollBoard"
        >{{$t('m.Start_Rolling')}}</el-button>
        <el-button
          style="padding: 3px 0; font-size: 16px;"
          type="text"
          @click="goResolver"
        >{{$t('m.Resolver_Rolling')}}</el-button>
      </template>
    </div>
    <div></div>
    <el-dialog :width="dialogWith" :visible.sync="openVisible" :close-on-click-modal="false">
      <el-form>
        <el-form-item :label="$t('m.Resolver_Rolling')">
          <a
            v-if="data"
            :href="resolverOnlineUrl"
            class="el-icon-document-copy"
            @click="doCopy"
          >{{ $t('m.Copy') }}</a>
        </el-form-item>
      </el-form>
    </el-dialog>
  </el-card>
</template>

<script>
import api from "@/common/api";
import { mapGetters } from "vuex";
import { CONTEST_STATUS } from "@/common/constants";
import myMessage from "@/common/message";

export default {
  name: "ScrollBoard",
  data() {
    return {
      cid: null,
      total: 0,
      goldMedal: 0,
      silverMedal: 0,
      bronzeMedal: 0,
      removeStar: false,
      dialogWith: "80%",
      openVisible: false,
      resolverOnlineUrl: "https://resolver.xcpcio.com",
      data: "",
    };
  },
  created() {
    this.cid = this.$route.params.contestID;
    this.getContestRankListCount();
  },
  methods: {
    getContestRankListCount() {
      let data = {
        currentPage: 1,
        limit: 10,
        cid: this.cid,
        forceRefresh: true,
        removeStar: true,
      };
      api.getContestRank(data).then((res) => {
        this.total = res.data.data.total;
        this.goldMedal = Math.floor(this.total * 0.1);
        this.silverMedal = Math.floor(this.total * 0.2);
        this.bronzeMedal = Math.floor(this.total * 0.3);
      });
    },
    goScrollBoard() {
      let url = `/scrollBoard?cid=${this.cid}&removeStar=${this.removeStar}&medals[]=${this.goldMedal}&medals[]=${this.silverMedal}&medals[]=${this.bronzeMedal}`;
      window.open(url);
    },
    goResolver() {
      this.openVisible = true;
      api
        .getContestResolverOnlineInfo(this.cid, this.removeStar)
        .then((res) => {
          let data = res.data.data;

          var jsonString = JSON.stringify({
            config: JSON.stringify(data.config),
            run: JSON.stringify(data.run),
            team: JSON.stringify(data.team),
          });

          this.data = jsonString;
        });
    },
    doCopy() {
      this.$copyText(this.data).then(
        () => {
          myMessage.success(this.$i18n.t("m.Copied_successfully"));
          this.openVisible = false;
        },
        () => {
          myMessage.success(this.$i18n.t("m.Copied_failed"));
          this.openVisible = false;
        }
      );
    },
  },
  computed: {
    ...mapGetters(["contestStatus"]),
    contestEnded() {
      return this.contestStatus === CONTEST_STATUS.ENDED;
    },
  },
};
</script>

<style>
</style>