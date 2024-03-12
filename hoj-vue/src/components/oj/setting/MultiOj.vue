<template>
  <el-form ref="formProfile" :model="formProfile">
    <el-row :gutter="30" justify="space-around">
      <el-col :md="10" :xs="24">
        <div class="left">
          <p class="section-title">{{ $t("m.Home_Multi_Oj") }}</p>
        </div>
      </el-col>
      <el-col :md="4" :lg="4">
        <p></p>
      </el-col>
      <el-col :md="10" :xs="24">
        <div class="right">
          <p class="section-title">{{ $t("m.Abroad_Multi_Oj") }}</p>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="30" justify="space-around">
      <el-col :md="10" :xs="24">
        <div class="left">
          <el-form-item prop="nowcoder" :label="$t('m.NC_Username')">
            <el-input v-model="formProfile.nowcoder" :maxlength="100" />
          </el-form-item>
          <el-form-item prop="vjudge" :label="$t('m.VJ_Username')">
            <el-input v-model="formProfile.vjudge" :maxlength="100" />
          </el-form-item>
          <el-form-item prop="poj" :label="$t('m.PK_Username')">
            <el-input v-model="formProfile.poj" :maxlength="100" />
          </el-form-item>
        </div>
      </el-col>
      <el-col :md="4" :lg="4">
        <div class="separator hidden-md-and-down"></div>
        <p></p>
      </el-col>
      <el-col :md="10" :xs="24">
        <div class="right">
          <el-form-item prop="codeforces" :label="$t('m.CF_Username')">
            <el-input v-model="formProfile.codeforces" :maxlength="100" />
          </el-form-item>
          <el-form-item prop="atcode" :label="$t('m.AT_Username')">
            <el-input v-model="formProfile.atcode" :maxlength="100" />
          </el-form-item>
          <el-form-item prop="leetcode" :label="$t('m.LT_Username')">
            <el-input v-model="formProfile.leetcode" :maxlength="100" />
          </el-form-item>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="30" justify="space-around">
      <el-col>
        <el-form-item prop="see" :label="$t('m.See_Status')">
          <el-col>
            <el-switch
              v-model="formProfile.see"
              :active-text="$t('m.See')"
              :inactive-text="$t('m.Hide')"
            ></el-switch>
          </el-col>
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="30" justify="space-around"></el-row>
    <div style="text-align: center; margin-top: 10px">
      <el-button
        type="primary"
        @click="updateUserMultiOj"
        :loading="loadingSaveBtn"
      >{{ $t("m.Save") }}</el-button>
    </div>
  </el-form>
</template>

<script>
import api from "@/common/api";
import myMessage from "@/common/message";
import "element-ui/lib/theme-chalk/display.css";
import { mapGetters } from "vuex";
import utils from "@/common/utils";

export default {
  data() {
    return {
      loadingSaveBtn: false,
      formProfile: {
        codeforces: "",
        nowcoder: "",
        vjudge: "",
        poj: "",
        atcode: "",
        leetcode: "",
        see: false,
      },
    };
  },
  mounted() {
    let profile = this.$store.getters.userInfo;
    Object.keys(this.formProfile).forEach((element) => {
      if (profile[element] !== undefined) {
        this.formProfile[element] = profile[element];
      }
    });
  },
  methods: {
    updateUserMultiOj() {
      this.$refs["formProfile"].validate((valid) => {
        if (valid) {
          this.loadingSaveBtn = true;
          let updateData = utils.filterEmptyValue(
            Object.assign({}, this.formProfile)
          );
          api.changeUserMultiOj(updateData).then(
            (res) => {
              myMessage.success(this.$i18n.t("m.Update_Successfully"));
              this.$store.dispatch("setUserInfo", res.data.data);
              this.loadingSaveBtn = false;
            },
            (_) => {
              this.loadingSaveBtn = false;
            }
          );
        }
      });
    },
  },
  computed: {
    ...mapGetters(["webLanguage", "token", "isAuthenticated"]),
  },
};
</script>

<style scoped>
.form-item-wrapper {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 21px;
  font-weight: 500;
  padding-top: 10px;
  padding-bottom: 20px;
  line-height: 30px;
}
.left {
  text-align: center;
}
.right {
  text-align: center;
}
/deep/ .el-input__inner {
  height: 32px;
}
/deep/ .el-form-item__label {
  font-size: 12px;
  line-height: 20px;
}
.separator {
  display: block;
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  border: 1px dashed #eee;
}
.template_code {
  text-align: left;
  margin-left: 10px;
}
</style>
