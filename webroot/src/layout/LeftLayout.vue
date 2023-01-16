<script>
import CreateTwinDialog from "@/views/createTwin/CreateTwinDialog.vue";
import { saveAs } from 'file-saver';

export default {
  name: "LeftLayout",
  components: {
    CreateTwinDialog,
  },
  data() {
    return {
      isCreateTwinDialog: false,
      isMenuFolded: false,
      isLayoutUnfoldedToText: true,
      activePageId: "twinAllList",
    };
  },
  mounted() {
    this.activePageId = this.$router.currentRoute.value.path.substr(1).split('/')[0];
    this.setActivePage();
    this.emitter.on("changeMenu", this.changeMenu);
    window.addEventListener("popstate", this.onBackButton);
  },
  beforeUnmount() {
    this.emitter.off("changeMenu");
    window.removeEventListener("popstate", this.onBackButton);
  },
  methods: {
    showCreateTwinDialog() {
      this.isCreateTwinDialog = true;
    },
    closeCreateTwinDialog() {
      this.isCreateTwinDialog = false;
    },
    onBackButton(event) {
      let path = this.$router.currentRoute.value.path.substr(1);
      this.changeMenu(path);
    },
    changeLayout() {
      setTimeout(() => {
        this.setActivePage();
        this.isMenuFolded = !this.isMenuFolded;
        this.isLayoutUnfoldedToText = !this.isLayoutUnfoldedToText
        this.emitter.emit("changeLeftLayout", this.isMenuFolded);
      }, 10);
    },
    changeMenu(path) {
      this.setDisablePage();

      this.activePageId = path.split('/')[0];
      this.$router.push("/" + path).catch(() => {});

      this.setActivePage();
    },
    setActivePage() {
      if (this.activePageId) {
        document
          .getElementById(this.activePageId)
          .classList.add("activeButton");
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
      // window.open("http://www.naver.com", "_blank");
      // return;
      this.emitter.emit("showConfirmDialog", {
          message: "Twin Base에서 데이터를 수집하시겠습니까?\n기존 데이터는 초기화 됩니다.",
          isConfirm: true,
          callback: (state) => {
              if (state) {
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
      });
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
  <div id="leftLayout">
    <div class="topPanel">
      <div class="logoPanel">
        <img src="/assets/images/machinaide_logo_white.png" :class="isMenuFolded ? 'foldedimage' : ''" />
      </div>
      
      <div class="expand" @click="changeLayout">
        <font-awesome-icon v-if="!isMenuFolded" icon="fa-solid fa-caret-left" />
        <font-awesome-icon v-if="isMenuFolded" icon="fa-solid fa-caret-right" />
      </div>
    </div>

    <div class="menuPanel">
      <div class="menuItemPanel">
        <button id="twinAllList" @click="changeMenu('twinAllList')">
          <font-awesome-icon icon="fa-solid fa-list" />
          <label v-if="isLayoutUnfoldedToText">Twins All List</label>
        </button>
      </div>

      <div class="menuItemPanel">
        <button id="sparqlList" @click="changeMenu('sparqlList')">
          <font-awesome-icon icon="fa-solid fa-project-diagram" />
          <label v-if="isLayoutUnfoldedToText">Select SPARQL</label>
        </button>
      </div>

      <div class="menuItemPanel">
        <button id="createTwin" @click="onCreateTwinClick()">
          <font-awesome-icon icon="fa-solid fa-plus-circle" />
          <label v-if="isLayoutUnfoldedToText">Create Twin</label>
        </button>
      </div>

      <div class="menuItemPanel">
        <button id="detailInformation" @click="changeMenu('detailInformation/all')">
          <font-awesome-icon icon="fa-solid fa-network-wired" />
          <label v-if="isLayoutUnfoldedToText">Graph Information</label>
        </button>
      </div>

      <!-- <div class="menuItemPanel">
        <button id="settingConfiguration" @click="changeMenu('settingConfiguration')">
          <font-awesome-icon icon="fa-solid fa-cog" />
          <label v-if="isLayoutUnfoldedToText">Setting Configuration</label>
        </button>
      </div> -->

      <div class="menuItemPanel">
        <button id="collectTwins" @click="onCollectTwinsClick()">
          <font-awesome-icon icon="fa-solid fa-share-square" />
          <label v-if="isLayoutUnfoldedToText">Collect Twins</label>
        </button>
      </div>

      <div class="menuItemPanel">
        <button id="collectTwins" @click="onDownloadAllTwinsClick()">
          <font-awesome-icon icon="fa-solid fa-download" />
          <label v-if="isLayoutUnfoldedToText">Download All Twins</label>
        </button>
      </div>
    </div>
  </div>

  <CreateTwinDialog
    v-if="isCreateTwinDialog"
    :propsIsDialog="isCreateTwinDialog"
    @close="closeCreateTwinDialog"
  />
</template>

<style scoped lang="scss">
#leftLayout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #1F2128;

  .topPanel {
    display: flex;
    flex-direction: row;
    border-right: 1px solid #343434;
    position: relative;

    .logoPanel {
      display: flex;
      flex-direction: row;
      height: 70px;
      align-items: center;
      margin-left: 20px;

    img {
      height: 46px;
      align-items: center;
      // background: white;
      align-self: center;
      border-radius: 5px;
    }

    .foldedimage {
      width: 40px;
      height: 46px;
      object-fit: cover;
      object-position: 4% 0;
    }


    .title {
      display: flex;
      width: 146px;
      height: 100%;
      font-weight: bold;
      align-items: center;
      font-size: 23px;
      margin-left: 20px;
    }
  }

  .expand {
      position: absolute;
      top: calc(50% - 14px);
      right: -14px;
      display: flex;
      border: 2px solid #464646;
      width: 28px;
      height: 28px;
      align-items: center;
      display: flex;
      justify-content: center;
      border-radius: 14px;
      background: #1F2128;
      cursor: pointer;

      svg {
        font-size: 20px;
        color: #A6A6A6;
      }
    }
  }

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

  @media screen and (max-width: 1000px) {
    .topPanel {
      display: none;
    }
  }
}
</style>
