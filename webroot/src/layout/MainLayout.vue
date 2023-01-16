<script>
import { mapGetters } from "vuex";
import HeaderLayout from "@/layout/HeaderLayout.vue";
import LeftLayout from "@/layout/LeftLayout.vue";

export default {
  name: "MainLayout",
  components: {
    HeaderLayout,
    LeftLayout,
  },
  data() {
    return {
      isMenuFolded: false,
      openState: false,
      isSmallScreen: true
    };
  },
  computed: {
    ...mapGetters(["getUserInfo"]),
  },
  created() {
    this.onWindowsResize();
  },
  mounted() {
    this.emitter.on("changeLeftLayout", this.changeLayout);
    window.addEventListener("resize", this.onWindowsResize);
    
  },
  beforeUnmount() {
    this.emitter.off("changeLeftLayout");
    window.removeEventListener("resize", this.onWindowsResize);
  },
  methods: {
    onWindowsResize(event) {
      if (document.body.clientWidth < 1000) {
        this.isSmallScreen = true;
      } else {
        this.isSmallScreen = false;
      }
    },
    changeLayout(isMenuFolded) {
      this.isMenuFolded = isMenuFolded;
    },
  },
};
</script>

<template>
  <div id="mainLayout">
    <div class="small-screen" v-if="isSmallScreen">
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
              <LeftLayout />
            </div>
          </template>

          <div class="centerPanel">
            <router-view></router-view>
          </div>
        </DxDrawer>
      </div>
    </div>

    <div class="large-screen" v-if="!isSmallScreen">
      <div :class="isMenuFolded ? 'leftPanel foldedLeftPanel' : 'leftPanel'">
        <LeftLayout />
      </div>

      <div :class="isMenuFolded ? 'centerPanel foldedCenterPanel' : 'centerPanel'">
        <div class="headerPanel">
          <HeaderLayout />
        </div>

        <div class="contentPanel">
          <router-view></router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
#mainLayout {
  width: 100%;
  height: 100%;

  .small-screen {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;

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
        padding: 20px;
      }
    }
  }

  .large-screen {
    display: flex;
    flex-direction: row;
    width: 100%;
    height: 100%;

    .leftPanel {
      display: flex;
      flex-direction: column;
      width: 270px;
      height: 100%;

      transition-property: all;
      transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
      transition-duration: 1s;
    }

    .foldedLeftPanel {
      width: 84px !important;

      transition-property: all;
      transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
      transition-duration: 1s;
    }


    .centerPanel {
      display: flex;
      flex-direction: column;
      width: calc(100% - 270px);
      height: 100%;

      transition-property: all;
      transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
      transition-duration: 1s;

      .headerPanel {
        display: flex;
        width: 100%;
        height: 70px;
      }
      

      .contentPanel {
        display: flex;
        flex-direction: column;
        width: 100%;
        height: calc(100% - 70px);
        padding: 30px;
      }
    }

    .foldedCenterPanel {
        width: calc(100% - 84px) !important;

        transition-property: all;
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
        transition-duration: 1s;
    }
  }
}
</style>
