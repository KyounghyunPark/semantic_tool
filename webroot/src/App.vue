<script>
import ConfirmDialog from "@/components/dialog/ConfirmDialog.vue";
import ErrorDialog from "@/components/dialog/ErrorDialog.vue";

export default {
  name: "App",
  components: {
    ConfirmDialog,
    ErrorDialog,
  },
  data() {
    return {
      isBackgroundBlur: false,
      isConfirmDialog: false,
      isConfirm: false,
      confirmDialogMessage: "",
      confirmDialogCallback: null,
      isErrorDialog: false,
      errorDialogMessage: "",
      webSocket: null,
    };
  },
  mounted() {
    this.emitter.on("showConfirmDialog", this.showConfirmDialog);
    this.emitter.on("showErrorDialog", this.showErrorDialog);
    this.emitter.on("sendWebSocketMessage", this.sendWebSocketMessage);

    this.connectWebSocket();
  },
  beforeUnmount() {
    this.emitter.off("showConfirmDialog");
    this.emitter.off("showErrorDialog");
    this.emitter.off("sendWebSocketMessage");

    this.isConfirmDialog = false;
    this.confirmDialogMessage = "";
    this.confirmDialogCallback = null;
    this.isErrorDialog = false;
    this.errorDialogMessage = "";
    this.webSocket = null;
  },
  methods: {
    showConfirmDialog(data) {
      this.isConfirmDialog = true;
      this.isConfirm = data.isConfirm;
      this.confirmDialogMessage = data.message;
      this.confirmDialogCallback = data.callback;
    },
    closeConfirmDialog(state) {
      if (this.confirmDialogCallback) {
        this.confirmDialogCallback(state);
      }
      this.isConfirmDialog = false;
      this.confirmDialogCallback = null;
    },
    showErrorDialog(msg) {
      this.isErrorDialog = true;
      this.errorDialogMessage = msg;
    },
    closeErrorDialog() {
      this.isErrorDialog = false;
    },
    connectWebSocket() {
      let checkDevPort = window.location.port;
      if (checkDevPort == "8052") {
        checkDevPort = 8050;
      }

      if (!this.webSocket) {
        let webSocketUrl =
          "ws://" +
          window.location.hostname +
          ":" +
          checkDevPort +
          "/websocket/message";
        this.webSocket = new WebSocket(webSocketUrl);

        this.webSocket.onmessage = this.onWebSocketMessage;
        this.webSocket.onopen = this.onWebSocketOpen;
        this.webSocket.onclose = this.onWebSocketClose;
        this.webSocket.onerror = this.onWebSocketError;
      }
    },
    onWebSocketMessage(message) {
      if (message) {
        let websocketDataMap = JSON.parse(message.data);
        if (websocketDataMap["command"] == "twinsChanged" && websocketDataMap["uuid"] != this.uuid) {
          let message = "다른사용자에 의해 TWIN 정보가 변경되었습니다.\n새로운 데이터로 변경하겠습니다.";
          this.emitter.emit("showConfirmDialog", {
            message: message,
            isConfirm: false,
            callback: () => this.emitter.emit("twinsChanged")
          });

          ;
        } else {
          this.emitter.emit("onWebSocketMessage", message.data);
        }
      }
    },
    onWebSocketOpen(event) {
      console.log("Successful websocket connection", event);
    },
    onWebSocketClose(event) {
      console.log("Websocket closed", event);
    },
    onWebSocketError(event) {
      console.error("Websocket Error", event);
    },
    sendWebSocketMessage(message) {
      this.webSocket.send(message);
    },
  },
};
</script>

<template>
  <div id="app" :class="isBackgroundBlur ? 'backgroundBlur' : ''">
    <transition name="fade" mode="out-in">
      <router-view></router-view>
    </transition>

    <ConfirmDialog
      v-if="isConfirmDialog"
      :propsIsDialog="isConfirmDialog"
      :propsIsConfirm = "isConfirm"
      :propsMessage="confirmDialogMessage"
      @close="closeConfirmDialog"
    />

    <ErrorDialog
      v-if="isErrorDialog"
      :propsIsDialog="isErrorDialog"
      :propsMsg="errorDialogMessage"
      @close="closeErrorDialog"
    />
  </div>
</template>

<style lang="scss">
#app {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background: #0F0F12;

  .backgroundBlur {
    filter: blur(2px);
  }
}
@import 'devextreme/dist/css/dx.dark.css';
@import "style";
@import "/assets/font.css";
</style>
