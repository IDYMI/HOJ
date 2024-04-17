<template>
  <div class="view">
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ title }}</span>
      </div>
      <ExaminationRoomVue :isAdmin="true" />
    </el-card>
  </div>
</template>

<script>
import ExaminationRoomVue from "@/components/oj/common/ExaminationRoom.vue";

export default {
  name: "CreateExaminationRoom",
  components: {
    ExaminationRoomVue,
  },
  mounted() {
    this.getRoute();
  },
  data() {
    return {
      title: this.$i18n.t("m.Edit_ExaminationRoom"),
    };
  },
  watch: {
    $route() {
      this.getRoute();
    },
  },
  methods: {
    getRoute() {
      if (this.$route.name === "admin-create-examination-room") {
        this.title = this.$i18n.t("m.Create_ExaminationRoom");
      } else {
        this.title = this.$i18n.t("m.Edit_ExaminationRoom");
      }
    },
  },
};
</script>
<style scoped>
</style>
