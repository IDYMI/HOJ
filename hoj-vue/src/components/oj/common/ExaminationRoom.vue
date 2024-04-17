<template>
  <div id="main">
    <el-card ref="card">
      <el-form label-position="top" :model="roomInfo" :rules="rules">
        <div class="titleInfo">
          <div>{{ roomInfo.school }}</div>
          <div>{{ roomInfo.title }}</div>
        </div>
        <p></p>
        <!-- <el-card> -->
        <p
          v-if="messageRow != null && messageCol != null"
          style="font-weight: bold; font-size: 20px; text-align: center; line-height: 100px;"
        >{{ $t("m.Your_Place") + "：“ " + messageBuilding + " # " + messageRoom + " - " + messageRow + " " + $t("m.Grow") + " " + messageCol + " " + $t("m.Gcol") + " ” " }}</p>
        <!-- </el-card> -->
        <div v-if="isAdmin">
          <el-row :gutter="20">
            <el-col :md="8" :xs="24">
              <el-form-item prop="school" :label="$t('m.School')" required>
                <el-select
                  v-model="roomInfo.school"
                  filterable
                  remote
                  reserve-keyword
                  :placeholder="$t('m.Enter_Your_School')"
                  :remote-method="fetchStates"
                  :loading="loading"
                  style="width: 100%;"
                >
                  <el-option
                    v-for="state in filteredStates"
                    :key="state.value"
                    :label="state.label"
                    :value="state.label"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :md="8" :xs="24">
              <el-form-item prop="building" :label="$t('m.Building')" required>
                <el-input v-model="roomInfo.building"></el-input>
              </el-form-item>
            </el-col>
            <el-col :md="8" :xs="24">
              <el-form-item prop="room" :label="$t('m.Room')" required>
                <el-input v-model="roomInfo.room"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-form-item :label="$t('m.Room_Size')" required>
              <el-col :md="8" :xs="24">
                <el-input-number style="width: 100%;" v-model="maxRow"></el-input-number>
              </el-col>
              <el-col :md="8" :xs="24">
                <el-input-number style="width: 100%;" v-model="maxCol"></el-input-number>
              </el-col>
              <el-col :md="8" :xs="24">
                <el-button
                  type="primary"
                  style="width: 100%;"
                  @click.native="addRectangle"
                >{{ $t('m.Create_Room') }}</el-button>
              </el-col>
            </el-form-item>
          </el-row>
        </div>
        <div class="centerInfo">
          <div class="centerInfo2" v-if="isAdmin">
            <span>{{ $t(seatTypeList[0].name) }}`</span>
            <PlaceRemoved :smallIconSize="iconSize" />
          </div>
          <div class="centerInfo2" v-if="isAdmin">
            <span>{{ $t(seatTypeList[1].name) }}`</span>
            <PlaceAvailable :smallIconSize="iconSize" />
          </div>
          <div class="centerInfo2">
            <span>{{ $t(seatTypeList[2].name) }}</span>
            <PlaceSelected :smallIconSize="iconSize" />
          </div>
          <div class="centerInfo2" v-if="!isAdmin">
            <span>{{ $t(seatTypeList[3].name) }}</span>
            <PlaceUsed :smallIconSize="iconSize" />
          </div>
          <div class="centerInfo2">
            <span>{{ $t(seatTypeList[4].name) }}</span>
            <PlaceMaintenance :smallIconSize="iconSize" />
          </div>
        </div>

        <div class="screen" :style="{ width: `${boxWidth / 2}rem` }">
          <div class="screen-text">{{ $t("m.ScreenOrientation") }}</div>
        </div>
        <div :style="{ minHeight: `${boxHeight}rem`}">
          <div class="box">
            <div
              v-for="(seatItem, index) in roomInfo.seatList"
              class="seatClass"
              :key="seatItem.id"
              @click="clickzuowei(seatItem)"
              :style="{
                top: seatItem.row * positionDistin + 'rem',
                left: seatItem.col * positionDistin + 'rem',
              }"
            >
              <template class="seatImgClass">
                <div v-if="seatItem.type === 0" :seatIndex="index">
                  <PlaceAvailable :smallIconSize="smallIconSize" />
                </div>
                <div v-else-if="seatItem.type === 1" :seatIndex="index">
                  <PlaceSelected :smallIconSize="smallIconSize" />
                </div>
                <div v-else-if="seatItem.type === 2" :seatIndex="index">
                  <el-tooltip
                    :content="seatItem.username ? $t('m.Your_Username') + ' : ' + seatItem.username : $t('m.Your_Place')"
                    v-model="visible"
                    manuanual="true"
                    visible:true
                  >
                    <PlaceUsed :smallIconSize="smallIconSize" />
                  </el-tooltip>
                </div>
                <div v-else-if="seatItem.type === 3" :seatIndex="index">
                  <PlaceMaintenance :smallIconSize="smallIconSize" />
                </div>
                <div v-else-if="isAdmin">
                  <PlaceRemoved :smallIconSize="smallIconSize" />
                </div>
              </template>
            </div>
          </div>
        </div>
        <p></p>
        <div v-if="isAdmin">
          <el-button type="primary" @click.native="saveExaminationRoom">{{ $t('m.Save') }}</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import PlaceRemoved from "@/components/oj/place/PlaceRemoved";
import PlaceAvailable from "@/components/oj/place/PlaceAvailable";
import PlaceSelected from "@/components/oj/place/PlaceSelected";
import PlaceUsed from "@/components/oj/place/PlaceUsed";
import PlaceMaintenance from "@/components/oj/place/PlaceMaintenance";
import PlaceAdd from "@/components/oj/place/PlaceAdd";
import api from "@/common/api";
import mMessage from "@/common/message";

export default {
  name: "ExaminationRoom",
  components: {
    PlaceRemoved,
    PlaceAvailable,
    PlaceSelected,
    PlaceUsed,
    PlaceMaintenance,
    PlaceAdd,
  },
  props: {
    isAdmin: {
      type: Boolean,
      default: false,
    },
    version: {
      type: String,
      default: "create",
    },
  },
  data() {
    return {
      keyword: null,
      routeName: "",
      maxRow: 7,
      maxCol: 7,
      nowId: 1,
      cardWidth: 0,
      boxWidth: 0,
      boxHeight: 100,
      iconSize: 30,
      smallIconSize: 16, // 每个座位的宽，高
      positionDistin: 8, // 每个座位偏移距离
      seatTypeList: [
        {
          name: "m.PlaceRemoved",
          type: 0,
          isShow: "1",
        },
        {
          name: "m.PlaceAvailable",
          type: 1,
          isShow: "1",
        },
        {
          name: "m.PlaceSelected",
          type: 2,
          isShow: "1",
        },
        {
          name: "m.PlaceUsed",
          type: 3,
          isShow: "1",
        },
        {
          name: "m.PlaceMaintenance",
          type: 4,
          isShow: "1",
        },
      ], // 座位信息
      roomInfo: {
        school: "南阳理工学院",
        title: "",
        building: "12",
        room: "502",
        seatList: [], // 排位置信息
      },
      loading: false,
      selectSchool: {
        label: "南阳理工学院",
        value: 1511,
      },
      filteredStates: [
        {
          label: "南阳理工学院",
          value: 1511,
        },
      ],
      seatInfo: {
        school: "南阳理工学院",
        title: "",
        building: "12",
        room: "502",
        seatList: [], // 排位置信息
      },
      states: [],
      rules: {
        school: [
          {
            pattern: /^[\u4e00-\u9fa5\d]*[\u4e00-\u9fa5]+[\u4e00-\u9fa5\d]*$/,
            min: 2,
            max: 15,
            message: this.$i18n.t("m.School_Check_length"),
            trigger: "blur",
          },
          {
            validator: (rule, value, callback) => {
              if (value === null || value === "") {
                callback();
              } else if (!this.states.find((item) => item.name === value)) {
                callback(new Error(this.$i18n.t("m.Not_Find_School")));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ],
      },
      messageCol: null,
      messageRow: null,
      messageBuilding: null,
      messageRoom: null,
    };
  },
  mounted() {
    // 获取全部学校
    this.getSchoolList();

    this.routeName = this.$route.name;
    if (this.routeName === "admin-create-examination-room") {
      if (this.roomInfo.seatList.length === 0) {
        // 初始化加载图
        this.rectangle();
      }
    } else if (this.routeName === "admin-edit-examination-room") {
      this.getExaminationRoom();
    } else if (this.routeName === "get-examination-room") {
      this.getExaminationSeat();
    }

    this.$nextTick(() => {
      this.updateCardSize(); // 初始化时获取一次大小
      window.addEventListener("resize", this.updateCardSize); // 监听窗口大小变化
    });
  },
  watch: {
    maxCol() {
      this.updateCardSize();
    },
    maxRow() {
      this.updateCardSize();
    },
    $route() {
      this.routeName = this.$route.name;
    },
  },
  computed: {
    selectedLabel() {
      const selectedState = this.filteredStates.find(
        (state) => state.label === this.roomInfo.school
      );
      return selectedState ? selectedState.value : null;
    },
  },
  methods: {
    updateCardSize() {
      const card = this.$refs.card.$el; // 获取 el-card 元素
      const rect = card.getBoundingClientRect(); // 获取元素的尺寸信息
      this.cardWidth = rect.width / 16;

      // 平均分配间距
      this.positionDistin = this.cardWidth / this.maxCol;
      this.smallIconSize = this.positionDistin * 10;
      // 计算boxHeight
      this.boxHeight = (this.maxRow + 1) * this.positionDistin;
      this.boxWidth = this.cardWidth;
    },
    //点击座位的时候切换图片
    clickzuowei(val) {
      // console.log(val, "座位信息");
      //循环影厅内座位信息
      this.roomInfo.seatList.forEach((item) => {
        //判断：选中座位和数组内的id是否一致
        if (item.id == val.id) {
          if (this.isAdmin) {
            if (item.type == 0) {
              item.type = 4;
            } else if (item.type == 4) {
              item.type = 3;
            } else if (item.type == 3) {
              item.type = 0;
            }
          }
        }
      });
    },
    // 新建座位矩阵
    rectangle() {
      this.nowId = 0;
      this.roomInfo.seatList = []; // 清空
      // 插入矩阵
      for (let i = 0; i < this.maxRow; i++) {
        for (let j = 0; j < this.maxCol; j++) {
          const newRow = {
            id: this.nowId,
            row: i,
            col: j,
            type: 0, // 根据需要设置类型
          };
          this.roomInfo.seatList.push(newRow);
          this.nowId += 1;
        }
      }
      this.updateCardSize();
    },
    // 点击添加行
    addRectangle() {
      this.$confirm(this.$i18n.t("m.Resize_Room"), "Tips", {
        type: "warning",
      }).then(
        () => {
          this.rectangle();
        },
        () => {}
      );
    },
    getSchoolList() {
      api.getSchoolList().then(
        (res) => {
          this.states = res.data.data;
        },
        (_) => {
          this.states = [];
        }
      );
    },
    getExaminationRoom() {
      let eid = this.$route.params.examinationRoomId;
      api.getExaminationRoom(eid).then(
        (res) => {
          let data = res.data.data;
          let school = {
            label: data.school,
            value: data.schoolId,
          };
          this.filteredStates.push(school);
          this.maxRow = data.maxRow;
          this.maxCol = data.maxCol;
          this.roomInfo = data;
        },
        (_) => {
          this.roomInfo = this.seatInfo;
          this.filteredStates = this.selectSchool;
        }
      );
    },
    getExaminationSeat() {
      let eid = this.$route.params.examinationRoomId;
      api.getExaminationSeat(eid, null).then(
        (res) => {
          let data = res.data.data;
          this.roomInfo = data;
          this.maxRow = data.maxRow;
          this.maxCol = data.maxCol;
          let keyword = this.$route.query.keyword;
          this.checkRealnameKeyword(keyword);
        },
        (_) => {
          this.roomInfo = this.seatInfo;
        }
      );
    },
    fetchStates(query) {
      if (query !== "") {
        this.loading = true;
        setTimeout(() => {
          this.loading = false;
          this.filteredStates = this.states
            .filter((state) => {
              return state.name.toLowerCase().indexOf(query.toLowerCase()) > -1;
            })
            .map((state) => ({ label: state.name, value: state.id }));
        }, 200);
      } else {
        this.filteredStates = this.selectSchool;
      }
    },
    checkRealnameKeyword(keyword) {
      if (keyword != null && keyword != undefined) {
        this.roomInfo.seatList.forEach((item, index) => {
          if (item.realname === keyword || item.number === keyword) {
            let userInfo = this.roomInfo.seatList[index];
            userInfo["type"] = 2; // 为 keyword 选手添加不同效果
            this.messageRow = userInfo["row"] + 1;
            this.messageCol = userInfo["col"] + 1;
            this.messageBuilding = this.roomInfo.building;
            this.messageRoom = this.roomInfo.room;
          }
        });
      }
    },
    saveExaminationRoom() {
      if (this.selectedLabel == null) {
        mMessage.warning(this.$i18n.t("m.Enter_Your_School"));
        return;
      }
      let examinationRoomVo = {
        schoolId: this.selectedLabel,
        building: this.roomInfo.building,
        room: this.roomInfo.room,
        maxRow: this.maxRow,
        maxCol: this.maxCol,
        seatList: this.roomInfo.seatList,
      }; // 上传给后台的数据

      if (this.routeName === "admin-edit-examination-room") {
        let eid = this.$route.params.examinationRoomId;
        examinationRoomVo["eid"] = eid;
      }

      let funcName = {
        "admin-create-examination-room": "admin_createExaminationRoom",
        "admin-edit-examination-room": "admin_editExaminationRoom",
      }[this.routeName];

      api[funcName](examinationRoomVo)
        .then((res) => {
          if (this.routeName === "admin-create-examination-room")
            mMessage.success(this.$i18n.t("m.Create_Successfully"));
          else {
            mMessage.success(this.$i18n.t("m.Update_Successfully"));
          }
        })
        .catch(() => {});
    },
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.updateCardSize); // 移除窗口大小变化监听器
  },
};
</script>

<style scoepd>
#main {
  width: 100%;
}
.seatClass {
  position: absolute;
}
.box {
  position: relative;
  /* margin-top: -40px; */
  width: 100%;
  justify-content: center;
  align-items: center;
}
.seatImgClass {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
}
.titleInfo {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #ccc;
  margin-top: 10px;
  padding: 0 10px 0 10px;
}
.centerInfo {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin-top: 10px;
}
.centerInfo2 {
  display: flex;
  align-items: center;
}
.screen {
  width: 250px;
  border: 30px solid #ccc;
  border-color: #ccc transparent transparent transparent;
  height: 20px;
  margin: auto;
  margin-top: 10px;
}
.screen-text {
  text-align: center;
  white-space: nowrap;
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-top: -30px;
}
</style>
