<template>
  <div class="markdown-body submission-detail">
    <div @click="collapsedClick()" style="cursor: pointer;">
      <pre ref="codeContainer" v-highlight="code" :style="styleObject" v-if="collapsed"><code :class="language"></code><span v-if="canFold"><i class="el-icon-caret-top"></i>{{ $t('m.Fold') }}</span></pre>
      <pre v-if="!collapsed"><span><i class="el-icon-caret-bottom"></i>{{ $t('m.Unfold') }}</span></pre>
    </div>
  </div>
</template>

<script>
export default {
  name: "highlight",
  data() {
    return {
      styleObject: {
        "border-left": "3px solid " + this.borderColor,
      },
    };
  },
  props: {
    language: {
      type: String,
    },
    code: {
      required: true,
      type: String,
    },
    borderColor: {
      type: String,
      default: "#19be6b",
    },
    canFold: {
      type: Boolean,
      default: false,
    },
    collapsed: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    collapsedClick() {
      if (this.canFold) {
        this.collapsed = !this.collapsed;
      }
    },
  },
  watch: {
    borderColor(newval, oldval) {
      if (newval != oldval) {
        this.styleObject["border-left"] = "3px solid " + newval;
      }
    },
  },
  updated() {
    this.$nextTick(() => {
      if (this.$refs.codeContainer) {
        this.$emit(
          "height-change",
          this.$refs.codeContainer.clientHeight,
          this.$refs.codeContainer.scrollHeight
        );
      }
    });
  },
};
</script>

<style scoped>
.hljs {
  padding: 0 !important;
}
.submission-detail pre {
  padding-left: 50px !important;
}
</style>
