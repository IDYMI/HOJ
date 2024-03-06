<template>
  <div>
    <el-form>
      <el-form-item :label="$t('m.Remote_OJ')">
        <el-select v-model="otherOJName" size="small">
          <el-option
            :label="remoteOj.name"
            :value="remoteOj.key"
            v-for="(remoteOj, index) in REMOTE_OJ"
            :key="index"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('m.Problem_ID')" required>
        <el-input v-model="otherOJProblemId" size="small"></el-input>
      </el-form-item>

      <el-form-item
        v-if="contestId"
        :label="$t('m.Enter_The_Problem_Display_ID_in_the_Contest')"
        required
      >
        <el-input v-model="displayId" size="small"></el-input>
      </el-form-item>

      <el-form-item style="text-align:center">
        <el-button
          type="primary"
          icon="el-icon-plus"
          @click="addRemoteOJProblem"
          :loading="addRemoteOJproblemLoading"
        >{{ $t('m.Add') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
import api from "@/common/api";
import { REMOTE_OJ } from "@/common/constants";
import myMessage from "@/common/message";

export default {
  name: "AddProblemFromRemote",
  props: {
    contestId: {
      type: Number,
      default: null,
    },
  },
  data() {
    return {
      loading: false,
      otherOJProblemId: "",
      otherOJName: "HDU",
      displayId: "",
      REMOTE_OJ: {},
      addRemoteOJproblemLoading: false,
    };
  },
  mounted() {
    this.REMOTE_OJ = Object.assign({}, REMOTE_OJ);
  },
  methods: {
    addRemoteOJProblem() {
      if (!this.otherOJProblemId) {
        myMessage.error(this.$i18n.t("m.Problem_ID_is_required"));
        return;
      }

      this.addRemoteOJproblemLoading = true;
      let funcName = "";
      if (this.contestId) {
        funcName = "admin_addContestRemoteOJProblem";
      } else {
        funcName = "admin_addRemoteOJProblem";
      }
      api[funcName](
        this.otherOJName,
        this.otherOJProblemId,
        this.$route.params.groupID,
        this.contestId,
        this.displayId
      ).then(
        (res) => {
          this.addRemoteOJproblemLoading = false;
          this.AddRemoteOJProblemDialogVisible = false;
          myMessage.success(this.$i18n.t("m.Add_Successfully"));
          if (this.contestId) {
            this.$emit("currentChangeProblem");
            this.$emit("handleRemotePage");
          } else {
            this.$emit("handleRemotePage");
            this.$emit("currentChange");
          }
        },
        (err) => {
          this.addRemoteOJproblemLoading = false;
        }
      );
    },
  },
};
</script>
