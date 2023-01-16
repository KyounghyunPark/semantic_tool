<script>
import { mapGetters } from "vuex";
import HeaderLayout from "@/layout/mobile/HeaderLayout.vue";
import LeftLayout from "@/layout/LeftLayout.vue";
import { path } from "d3-path";

export default {
  name: "MainLayout",
  components: {
    HeaderLayout,
    LeftLayout,
  },
  data() {
    return {
      openState: false
    };
  },
  computed: {
    ...mapGetters(["getUserInfo"]),
  },
  created() {
  },
  mounted() {
    this.setActivePath(this.$router.currentRoute.value.path);
    this.setActivePage();

    this.emitter.on("changeMenu", this.changeMenu);
    window.addEventListener("popstate", this.onBackButton);
  },
  beforeUnmount() {
    this.emitter.off("changeMenu");
    window.removeEventListener("popstate", this.onBackButton);
  },
  methods: {
    setActivePath(path) {
      let paths = path.split('/');
      this.activePageId = paths[paths.length - 1];
    },
    showCreateTwinDialog() {
      this.isCreateTwinDialog = true;
    },
    closeCreateTwinDialog() {
      this.isCreateTwinDialog = false;
    },
    onBackButton() {
      let path = this.$router.currentRoute.value.path.substr(1);
      this.changeMenu(path);
    },
    changeMenu(path) {
      this.openState = false;
      this.setDisablePage();

      this.setActivePath(path);
      this.setActivePage();

      this.$router.push("/" + path).catch(() => {});
    },
    setActivePage() {
      if (this.activePageId) {
        document.getElementById(this.activePageId).classList.add("activeButton");
      }
    },
    setDisablePage() {
      if (this.activePageId) {
        document.getElementById(this.activePageId).classList.remove("activeButton");
      }
    },
    onCreateTwinClick() {
      this.showCreateTwinDialog();
    },
    onCollectTwinsClick() {
      let result = confirm("Twin Base에서 데이터를 수집하시겠습니까?\n기존 데이터에 영향을 줄 수 있습니다.");
      if (result) {
        this.emitter.emit("setLoading", true);
        fetch("/api/twin/collect", {
              method: "POST",
              headers: {
              "Content-Type": "application/json",
              },
              body: JSON.stringify({}),
          })
          .then((resp) => resp.json())
          .then((result) => {
          this.emitter.emit("setLoading", false);

          if (result.succeeded) {
            this.emitter.emit("showConfirmDialog", {
              message: "Collect Twins Complete",
              isConfirm: false
            });
            
            this.emitter.emit("twinsChanged");
          } else {
              this.emitter.emit("showErrorDialog", result.data);
          }
          })
          .catch((err) => {
              this.emitter.emit("setLoading", false);

              console.error(err);
              this.emitter.emit("showErrorDialog", err);
          });
      }
    },
    onDownloadAllTwinsClick() {
      this.emitter.emit("setLoading", true);

      let param = {
       type: "all"
      }

      let fileName = "all_twin_json.zip";
      fetch("/api/twin/download", {
            method: "POST",
            headers: {
            "Content-Type": "application/json",
            },
            body: JSON.stringify(param),
        })
        .then((resp) => {
          if (resp.status === 200) {
            fileName = resp.headers.get("content-disposition").match(/(?<=")(?:\\.|[^"\\])*(?=")/)[0];
            return resp.blob();
          } else if (resp.status === 204) {
            return "등록된 TWIN이 없습니다.";
          }
          else if (resp.status === 400) {
            return Promise.reject("파리미터가 올바르지 않습니다.");
          } else if (resp.status == 500) {
            return Promise.reject("서버에서 오류가 발생하였습니다.");
          }
        })
        .then((result) => {
          this.emitter.emit("setLoading", false);

          if (typeof(result) == 'string') {
            this.emitter.emit("showConfirmDialog", {
              message: result,
              isConfirm: false
            });
          } else {  
            saveAs(result, fileName);
          }
        })
        .catch((err) => {
            this.emitter.emit("setLoading", false);
            this.emitter.emit("showErrorDialog", err);
        });
    }
  },
};
</script>

<template>
  <div id="mainLayout">
    <div class="headerPanel">
      <HeaderLayout />
      <button id="twinAllList" @click="(openState = !openState)">
        <font-awesome-icon icon="fa-solid fa-bars" />
      </button>
    </div>

    <div class="drawPanel">
      <DxDrawer
        :opened-state-mode="'overlap'"
        :position="'left'"
        :reveal-mode="'slide'"
        v-model:opened="openState"
        :height="'100%'"
        :close-on-outside-click="true"
        template="listMenu"
      >
        <template #listMenu>
          <div class="leftPanel">
            <div id="leftLayout">
              <div class="menuPanel">
                <div class="menuItemPanel">
                  <button id="twinAllList" @click="changeMenu('mobile/twinAllList')">
                    <font-awesome-icon icon="fa-solid fa-list" />
                    <label>Twins All List</label>
                  </button>
                </div>

                <div class="menuItemPanel">
                  <button id="sparqlList" @click="changeMenu('sparqlList')">
                    <font-awesome-icon icon="fa-solid fa-project-diagram" />
                    <label>Select SPARQL</label>
                  </button>
                </div>

                <div class="menuItemPanel">
                  <button id="createTwin" @click="onCreateTwinClick()">
                    <font-awesome-icon icon="fa-solid fa-plus-circle" />
                    <label>Create Twin</label>
                  </button>
                </div>

                <div class="menuItemPanel">
                  <button id="detailInformation" @click="changeMenu('detailInformation/all')">
                    <font-awesome-icon icon="fa-solid fa-network-wired" />
                    <label>Graph Information</label>
                  </button>
                </div>

                <div class="menuItemPanel">
                  <button id="collectTwins" @click="onCollectTwinsClick()">
                    <font-awesome-icon icon="fa-solid fa-share-square" />
                    <label>Collect Twins</label>
                  </button>
                </div>

                <div class="menuItemPanel">
                  <button id="collectTwins" @click="onDownloadAllTwinsClick()">
                    <font-awesome-icon icon="fa-solid fa-download" />
                    <label>Download All Twins</label>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>

        <div class="centerPanel">
          <router-view></router-view>
        </div>
      </DxDrawer>
    </div>    
  </div>
</template>

<style scoped lang="scss">
#mainLayout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .headerPanel {
    display: flex;
    width: 100%;
    height: 70px;
    position: relative;

    button {
      position: absolute;
      left: 20px;
      top: 15px;
      width: 48px;
      height: 48px;
      border-radius: 10px;
      background-color: transparent;
      color: #A6A6A6;
      text-align: center;
      font-size: 20px;
      border: 1px solid gray;
    }
  }

  .drawPanel {
    width: 100%;
    height: calc(100% - 70px);

    .leftPanel {
      width: 270px;
      height: 100%;
    }

    .centerPanel {
      display: flex;
      flex-direction: column;
      width: 100%;
      height: 100%;
      padding: 30px;
    }
  }
}

#leftLayout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #1F2128;

  .menuPanel {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: calc(100% - 70px);
    padding-top: 10px;
    background-color: #1F2128;

    .menuItemPanel {
      display: flex;
      flex-direction: row;
      margin: 0px 20px;
    }

    button {
      width: 100%;
      height: 48px;
      border-radius: 10px;
      background-color: transparent;
      border: none;
      color: #A6A6A6;
      text-align: left;
      padding: 10px 15px;
      cursor: pointer;

      label {
        cursor: pointer;
        margin-left: 15px;
        color: #A6A6A6;
        width: 100%;
        text-align: start;
      }

      svg {
        font-size: 16px;
      }
    }

    button:hover:not(.activeButton) {
      color: #95c1ff;

      label {
        color: #95c1ff;
      }
    }

    .activeButton {
      background-color: #7364DB;
      color: white;

      label {
        color: white;
      }
    }
  }
}
</style>
