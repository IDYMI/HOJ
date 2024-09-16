<template>
  <div class="honor-wall">
    <el-card>
      <el-row :gutter="20">
        <div class="title-container">
          <h2 class="title" :style="{ fontSize: '40px' }">荣 誉 墙</h2>
          <h2 class="title" :style="{ fontSize: '25px', marginBottom: '5px' }">HONOR WALL</h2>
        </div>
        <div class="honorList">
          <div v-for="(honor, index) in honorList" :key="index" class="honor-row">
            <div class="honor-container">
              <div v-for="(item, idx) in honor.honor" :key="idx" class="honor">
                <component
                  :is="item.link ? 'a' : 'div'"
                  :href="item.link"
                  target="_blank"
                  v-bind="item.link ? { href: item.link, target: '_blank' } : {}"
                >
                  <p :style="{ fontSize: '15px' }">{{ item.level }}</p>
                  <el-tooltip v-if="item.teamMember" :content="item.teamMember" placement="top">
                    <img
                      :src="imageSrc[item.type] || item.type"
                      :alt="item.title"
                      :style="imgStyles(item.type)"
                    />
                  </el-tooltip>
                  <img
                    v-else
                    :src="imageSrc[item.type] || item.type"
                    :alt="item.title"
                    :style="imgStyles(item.type)"
                  />
                  <p>{{ item.title }}</p>
                </component>
              </div>
            </div>
            <div class="data-container">
              <h2 class="date">{{ honor.year }}</h2>
            </div>
          </div>
        </div>
      </el-row>
    </el-card>
    <Pagination
      :total="total"
      :page-size="limit"
      @on-change="filterByPage"
      :current.sync="currentPage"
      :layout="'prev, pager, next'"
    ></Pagination>
  </div>
</template>

<script>
import Pagination from "@/components/oj/common/Pagination";
import api from "@/common/api";
import utils from "@/common/utils";
import { HONOR_TYPE } from "@/common/constants";

export default {
  metaInfo: {
    title: "Honor",
    meta: [
      {
        name: "keywords",
        content: "NYIST荣誉",
      },
      {
        name: "description",
        content:
          "现在您所看到的是全新版的 nyoj 3.0，nyoj 是南阳理工学院历史最早的在线评测系统，提供各种算法题目和竞赛，帮助学生提高编程技能。现域名https://xcpc.nyist.edu.cn，曾用域名http://acm.nyist.net/JudgeOnline，https://nyoj.online。",
      },
    ],
  },
  components: {
    Pagination,
  },
  data() {
    return {
      imageSrc: {
        Gold: require("@/assets/Gold.png"),
        Silver: require("@/assets/Silver.png"),
        Bronze: require("@/assets/Bronze.png"),
      },
      honorList: [],
      query: {
        keyword: "",
      },
      total: 0,
      currentPage: 1,
      limit: 2,
      HONOR_TYPE: {},
    };
  },
  created() {
    let route = this.$route.query;
    this.currentPage = parseInt(route.currentPage) || 1;
  },
  mounted() {
    this.HONOR_TYPE = Object.assign({}, HONOR_TYPE);
    this.init();
  },
  methods: {
    init() {
      let route = this.$route.query;
      this.query.keyword = route.keyword || "";
      this.currentPage = parseInt(route.currentPage) || 1;
      this.getHonorList();
    },

    filterByPage(page) {
      this.currentPage = page;
      this.filterByChange();
    },
    filterByChange() {
      let query = Object.assign({}, this.query);
      query.currentPage = this.currentPage;
      this.$router.push({
        path: "/honor",
        query: utils.filterEmptyValue(query),
      });
    },

    getHonorList() {
      this.loading = true;
      let query = Object.assign({}, this.query);
      api.getHonorList(this.currentPage, this.limit, query).then(
        (res) => {
          this.honorList = res.data.data.records;
          this.total = res.data.data.total;
          this.loading = false;
        },
        (err) => {
          this.loading = false;
        }
      );
    },
    imgStyles(type) {
      return {
        backgroundColor: this.HONOR_TYPE[type].color || "#CD7F32",
        border: "4px solid #8B4513", // 棕色边框
        padding: "5px", // 增加内边距以显示边框
        width: "200px", // 调整为横向长方形
        height: "150px", // 调整为横向长方形
        objectFit: "cover", // 确保图片以正确的比例显示
      };
    },
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.init();
      }
    },
  },
};
</script>

<style scoped>
.honor-wall {
  text-align: center;
}

.title {
  margin: 20px 0;
  color: red;
  position: relative;
  text-align: center;
}
.title-container {
  display: flex; /* 使用 Flexbox 布局，使子元素水平排列 */
  justify-content: center; /* 水平居中对齐容器内的内容 */
  align-items: center; /* 垂直居中对齐容器内的内容 */
  gap: 20px; /* 设置标题之间的间隔为 20px */
}
.title-container::before,
.title-container::after {
  content: "";
  position: absolute;
  width: calc(50% - 220px);
  border-top: 8px solid red;
}
.title-container::before {
  left: 20px;
  margin-right: 20px;
}
.title-container::after {
  right: 20px;
  margin-left: 20px;
}

.data-container {
  display: flex; /* 使用 Flexbox 布局，使子元素水平排列 */
  justify-content: center; /* 水平居中对齐容器内的内容 */
  align-items: center; /* 垂直居中对齐容器内的内容 */
  padding-top: 400px; /* 底部间距，确保下边框与内容间有距离 */
}
.data-container::before,
.data-container::after {
  content: "";
  position: absolute;
  width: calc(50% - 100px);
  background: linear-gradient(
    to bottom,
    red 5px,
    transparent 0
  ); /* 渐变效果，从上到下逐渐变透明 */
  border-bottom: 3px solid red; /* 上窄下宽的红色边框 */
  box-sizing: border-box; /* 确保边框在宽度计算内 */
  height: 10px; /* 横线的高度 */
}

.data-container::before {
  left: 20px;
  margin-right: 10px;
}
.data-container::after {
  right: 20px;
  margin-left: 10px;
}

.honorList {
  display: flex;
  flex-direction: column;
}

.honor-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.date {
  position: absolute;
  left: 50%;
  transform: translateX(-50%); /* 将日期水平居中 */
  padding: 0 10px; /* 增加内边距 */
  text-align: center;
  font-size: 30px;
  margin: 20px 0;
  color: red;
}

.honor {
  display: inline-block;
  margin-right: 50px; /* 证书之间的间距 */
  vertical-align: top;
  position: relative; /* 确保伪元素的定位相对于证书项 */
  padding-bottom: 10px; /* 底部间距，确保下边框与内容间有距离 */
}

.honor-container {
  flex: 1;
  overflow-x: auto; /* 启用水平滚动 */
  white-space: nowrap; /* 防止子元素换行 */
  margin-top: 150px;
  scrollbar-width: none; /* Firefox 隐藏滚动条 */
  width: 100%;
  margin-left: 30px;
  margin-right: 30px;
}

/* 隐藏 Webkit 浏览器中的滚动条 */
.honor-container::-webkit-scrollbar {
  display: none; /* 隐藏滚动条 */
}

img {
  max-width: auto; /* 限制图片的最大宽度为自动（可以根据需要调整） */
  height: 100px; /* 设置图片高度为 50px */
}
</style>
