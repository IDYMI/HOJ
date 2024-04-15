<template>
  <div>
    <el-form :model="loginForm" :rules="rules" ref="loginForm">
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          prefix-icon="el-icon-user-solid"
          :placeholder="$t('m.Login_Username')"
          @keyup.enter.native="handleLogin"
          width="100%"
        ></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.New_Password')"
          @keyup.enter.native="handleLogin"
          type="password"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item prop="newPassword">
        <el-input
          v-model="loginForm.newPassword"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.New_Password')"
          @keyup.enter.native="handleLogin"
          type="password"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item prop="passwordAgain">
        <el-input
          v-model="loginForm.passwordAgain"
          prefix-icon="el-icon-lock"
          :placeholder="$t('m.Confirm_New_Password')"
          @keyup.enter.native="handleLogin"
          type="password"
          show-password
        ></el-input>
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button
        type="primary"
        @click="handleLogin()"
        :loading="btnLoginLoading"
      >{{ $t('m.Login_Btn') }}</el-button>
    </div>
  </div>
</template>
<script>
import { mapGetters, mapActions } from "vuex";
import api from "@/common/api";
import mMessage from "@/common/message";
export default {
  data() {
    const CheckPassword = (rule, value, callback) => {
      if (this.loginForm.newPassword !== "") {
        // 对第二个密码框再次验证
        this.$refs.loginForm.validateField("passwordAgain");
      }
      callback();
    };
    const CheckAgainPassword = (rule, value, callback) => {
      if (value !== this.loginForm.newPassword) {
        callback(new Error(this.$i18n.t("m.Password_does_not_match")));
      }
      callback();
    };

    return {
      btnLoginLoading: false,
      loginForm: {
        username: "",
        password: "",
        passwordAgain: "",
        newPassword: "",
      },
      rules: {
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
        newPassword: [
          {
            required: true,
            message:
              this.$i18n.t("m.New_Password") + this.$i18n.t("m.is_required"),
            trigger: "blur",
          },
          {
            min: 6,
            max: 20,
            message: this.$i18n.t("m.Password_Check_Between"),
            trigger: "blur",
          },
          { validator: CheckPassword, trigger: "blur" },
        ],
        passwordAgain: [
          {
            required: true,
            message: this.$i18n.t("m.NewPassword_Again_Check_Required"),
            trigger: "blur",
          },
          { validator: CheckAgainPassword, trigger: "change" },
        ],
      },
    };
  },
  methods: {
    ...mapActions([
      "startTimeOut",
      "changeRegisterTimeOut",
      "changeModalStatus",
    ]),

    handleLogin() {
      this.$refs["loginForm"].validate((valid) => {
        if (valid) {
          const _this = this;
          let formData = Object.assign({}, this.loginForm);
          delete formData["passwordAgain"];
          this.btnLoginLoading = true;
          api.login(formData).then(
            (res) => {
              this.btnLoginLoading = false;
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
              this.$store.dispatch("incrLoginFailNum", false);
              this.btnLoginLoading = false;
            }
          );
        }
      });
    },
  },
};
</script>
<style scoped>
.footer {
  overflow: auto;
  margin-top: 20px;
  margin-bottom: -15px;
  text-align: center;
}
/deep/ .el-input-group__append {
  color: #fff;
  background: #25bb9b;
}
/deep/.footer .el-button--primary {
  margin: 0 0 15px 0;
  width: 100%;
}

/deep/ .el-form-item__content {
  margin-left: 0px !important;
}
</style>
