<template>
  <div>
    <el-form :model="formLogin" :rules="rules" ref="formLogin" label-width="100px">
      <el-form-item prop="username">
        <el-input
          v-model="formLogin.username"
          prefix-icon="el-icon-user-solid"
          :placeholder="$t('m.Login_Username')"
          width="100%"
          @keyup.enter.native="enterHandleLogin"
        ></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="formLogin.password"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.Login_Password')"
          type="password"
          show-password
          @keyup.enter.native="enterHandleLogin"
        ></el-input>
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button
        type="primary"
        v-if="!needVerify"
        @click="handleLogin"
        :loading="btnLoginLoading"
      >{{ $t('m.Login_Btn') }}</el-button>
      <template v-else>
        <el-button
          type="primary"
          :loading="btnLoginLoading"
          slot="reference"
          @click="verify.loginSuccess = false"
        >{{ $t('m.Login_Btn') }}</el-button>
        <Vcode :show="verify.loginSuccess === false" style="z-index: 9999;" @success="handleLogin" />
        <el-alert
          :title="$t('m.Slide_Verify_Success')"
          type="success"
          v-show="verify.loginSuccess"
          :center="true"
          :closable="false"
          show-icon
        ></el-alert>
      </template>
      <el-link
        v-if="websiteConfig.register"
        type="primary"
        @click="switchMode('Register')"
      >{{ $t('m.Login_No_Account') }}</el-link>
      <el-link
        type="primary"
        @click="switchMode('ResetPwd')"
        style="float: right"
      >{{ $t('m.Login_Forget_Password') }}</el-link>
    </div>
  </div>
</template>
<script>
import { mapGetters, mapActions } from "vuex";
import api from "@/common/api";
import mMessage from "@/common/message";
import Vcode from "vue-puzzle-vcode";

export default {
  components: {
    Vcode,
  },
  data() {
    return {
      formProfile: {
        uiLanguage: "",
        uiTheme: "",
      },
      btnLoginLoading: false,
      verify: {
        loginSuccess: null,
      },
      needVerify: false,
      formLogin: {
        username: "",
        password: "",
      },
      rules: {
        username: [
          {
            required: true,
            message: this.$i18n.t("m.Username_Check_Required"),
            trigger: "blur",
          },
          {
            max: 20,
            message: this.$i18n.t("m.Username_Check_Max"),
            trigger: "blur",
          },
        ],
        password: [
          {
            required: true,
            message: this.$i18n.t("m.Password_Check_Required"),
            trigger: "blur",
          },
          {
            min: 6,
            max: 20,
            message: this.$i18n.t("m.Password_Check_Between"),
            trigger: "blur",
          },
        ],
      },
    };
  },
  methods: {
    ...mapActions(["changeModalStatus"]),
    switchMode(mode) {
      this.changeModalStatus({
        mode,
        visible: true,
      });
    },
    enterHandleLogin() {
      if (this.needVerify) {
        this.visible.loginSlideBlock = true;
      } else {
        this.handleLogin();
      }
    },
    handleLogin() {
      if (this.needVerify) {
        this.verify.loginSuccess = true;
      }
      this.$refs["formLogin"].validate((valid) => {
        if (valid) {
          this.btnLoginLoading = true;
          let formData = Object.assign({}, this.formLogin);
          api.login(formData).then(
            (res) => {
              this.btnLoginLoading = false;
              setTimeout(() => {
                this.verify.loginSuccess = null;
              }, 1000);
              this.changeModalStatus({ visible: false });
              const jwt = res.headers["authorization"];
              this.$store.commit("changeUserToken", jwt);
              this.$store.dispatch("setUserInfo", res.data.data);
              this.$store.dispatch("incrLoginFailNum", true);
              let profile = this.$store.getters.userInfo;
              Object.keys(this.formProfile).forEach((element) => {
                if (profile[element] !== undefined) {
                  this.formProfile[element] = profile[element];
                  this.$store.commit("changeWebLanguage", {
                    language: this.formProfile.uiLanguage,
                  });
                  this.$store.commit("changeWebTheme", {
                    theme: this.formProfile.uiTheme,
                  });
                }
              });
              mMessage.success(this.$i18n.t("m.Welcome_Back"));
            },
            (_) => {
              let status = _.data.status;
              // 禁止登录
              if (status === 403) {
                // 切换到比赛专用登录界面
                this.switchMode("ContestAccountLogin");
              }
              this.$store.dispatch("incrLoginFailNum", false);
              this.btnLoginLoading = false;
              setTimeout(() => {
                this.verify.loginSuccess = null;
              }, 1000);
            }
          );
        }
      });
    },
  },
  computed: {
    ...mapGetters(["modalStatus", "loginFailNum", "websiteConfig"]),
    visible: {
      get() {
        return this.modalStatus.visible;
      },
      set(value) {
        this.changeModalStatus({ visible: value });
      },
    },
  },
  watch: {
    loginFailNum(newVal, oldVal) {
      if (newVal >= 5) {
        this.needVerify = true;
      } else {
        this.needVerify = false;
      }
    },
  },
};
</script>
<style scoped>
.footer {
  overflow: auto;
  margin-top: 20px;
  margin-bottom: -15px;
  text-align: left;
}
/deep/.el-button {
  margin: 0 0 15px 0;
  width: 100%;
}

/deep/ .el-form-item__content {
  margin-left: 0px !important;
}
</style>
