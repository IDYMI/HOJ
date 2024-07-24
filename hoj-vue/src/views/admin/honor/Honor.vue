<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ title }}</span>
      </div>
      <el-form label-position="top">
        <el-row :gutter="20">
          <!-- 荣誉时间 -->
          <el-col :span="24">
            <el-col :span="8">
              <el-form-item :label="$t('m.Title')" required>
                <el-input v-model="honor.title" :placeholder="$t('m.Title')"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('m.Honor_Award')" required>
                <el-select v-model="honor.type" style="width: 100%">
                  <el-option :label="$t('m.Honor_Gold')" value="Gold"></el-option>
                  <el-option :label="$t('m.Honor_Silver')" value="Silver"></el-option>
                  <el-option :label="$t('m.Honor_Bronze')" value="Bronze"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('m.Honor_Date')" required>
                <el-date-picker
                  style="width: 100%"
                  v-model="honor.date"
                  type="date"
                  :placeholder="$t('m.Honor_Date')"
                ></el-date-picker>
              </el-form-item>
            </el-col>
          </el-col>
          <el-col :span="24">
            <el-col :span="8">
              <el-form-item :label="$t('m.Level')" required>
                <el-select v-model="honor.level" style="width: 100%">
                  <el-option label="全球赛" value="全球赛"></el-option>
                  <el-option label="国赛" value="国赛"></el-option>
                  <el-option label="省赛" value="省赛"></el-option>
                  <el-option label="校赛" value="校赛"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('m.Honor_TeamMember')">
                <el-input v-model="honor.teamMember" :placeholder="$t('m.Honor_TeamMember')"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('m.See_Status')" required>
                <el-col>
                  <el-switch
                    v-model="honor.status"
                    :active-text="$t('m.See')"
                    :inactive-text="$t('m.Hide')"
                  ></el-switch>
                </el-col>
              </el-form-item>
            </el-col>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('m.Honor_Description')">
              <Editor :value.sync="honor.description"></Editor>
            </el-form-item>
            <span>
              {{ $t('m.Create_Honor_Tips') }}
              <p></p>
            </span>
          </el-col>
        </el-row>
      </el-form>
      <el-button type="primary" @click.native="saveHonor">
        {{
        $t('m.Save')
        }}
      </el-button>
    </el-card>
  </div>
</template>

<script>
import api from "@/common/api";
import { mapGetters } from "vuex";
import myMessage from "@/common/message";
const Editor = () => import("@/components/admin/Editor.vue");
export default {
  name: "Create Honor",
  components: {
    Editor,
  },
  data() {
    const today = new Date();
    today.setHours(0, 0, 0, 0); // 设置时间为当天的0点0分
    return {
      title: "Create Honor",
      honor: {
        title: "",
        description: "",
        date: today, // 设置默认日期为当前时间,
        type: "Gold",
        level: "国赛",
        status: true,
        teamMember: "",
      },
      trainingCategoryId: null,
      trainingCategoryList: [],
    };
  },
  mounted() {
    this.init();
  },
  watch: {
    $route() {
      if (this.$route.name === "admin-edit-honor") {
        this.title = this.$i18n.t("m.Edit_Honor");
        this.getHonor();
      } else if (this.$route.name === "admin-create-honor") {
        this.title = this.$i18n.t("m.Create_Honor");
      }
    },
  },
  computed: {
    ...mapGetters(["userInfo"]),
  },
  methods: {
    init() {
      if (this.$route.name === "admin-edit-honor") {
        this.title = this.$i18n.t("m.Edit_Honor");
        this.getHonor();
      } else {
        this.title = this.$i18n.t("m.Create_Honor");
      }
    },

    getHonor() {
      api
        .admin_getHonor(this.$route.params.honorId)
        .then((res) => {
          let data = res.data.data;
          this.honor = data || {};
        })
        .catch(() => {});
    },

    saveHonor() {
      if (!this.honor.title) {
        myMessage.error(
          this.$i18n.t("m.Honor_Short_Name") +
            " " +
            this.$i18n.t("m.is_required")
        );
        return;
      }
      if (!this.honor.type) {
        myMessage.error(
          this.$i18n.t("m.Honor_Award") + " " + this.$i18n.t("m.is_required")
        );
        return;
      }
      if (!this.honor.date) {
        myMessage.error(
          this.$i18n.t("m.Honor_Date") + " " + this.$i18n.t("m.is_required")
        );
        return;
      }

      let funcName =
        this.$route.name === "admin-edit-honor"
          ? "admin_editHonor"
          : "admin_createHonor";

      let data = Object.assign({}, this.honor);
      if (funcName === "admin_createHonor") {
        data["author"] = this.userInfo.username;
      }

      api[funcName](data)
        .then((res) => {
          myMessage.success("success");
          this.$router.push({
            name: "admin-honor-list",
            query: { refresh: "true" },
          });
        })
        .catch(() => {});
    },
  },
};
</script>
<style scoped>
.userPreview {
  padding-left: 10px;
  padding-top: 20px;
  padding-bottom: 20px;
  color: red;
  font-size: 16px;
  margin-bottom: 10px;
}
</style>
