<script>
export default {
  name: "ErrorDialog",
  props: {
    propsIsDialog: {
      type: Boolean,
      required: true,
    },
    propsMsg: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      isDialog: false,
      msg: "",
    };
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.msg = this.propsMsg;

    if (!this.msg) {
      this.msg = "An error occurred on the server.";
    }
  },
  methods: {
    close() {
      this.$emit("close");
    },
  },
};
</script>

<template>
  <P-Dialog
    id="errorDialog"
    v-model:visible="isDialog"
    :closable="false"
    :modal="true"
  >
    <template #header>
      <label class="title">Result</label>
    </template>

    <P-Textarea v-model="msg" :autoResize="false" rows="100" />

    <template #footer>
      <P-Button @click="close">Close</P-Button>
    </template>
  </P-Dialog>
</template>

<style scoped lang="scss">
#errorDialog {
  .title {
    color: #ff4c4c;
  }

  .p-inputtext {
    width: 100%;
    height: 100%;
  }
}
</style>
