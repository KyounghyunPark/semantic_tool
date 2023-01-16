<script>
export default {
  name: "ConfirmDialog",
  props: {
    propsIsDialog: {
      type: Boolean,
      required: true,
    },
    propsIsConfirm: {
      type: Boolean,
      required: true,
    },
    propsMessage: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      isDialog: false,
      isConfirm: false,
      message: "",
    };
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.isConfirm = this.propsIsConfirm;
    this.message = this.propsMessage;
  },
  methods: {
    yes() {
      this.$emit("close", true);
    },
    no() {
      this.$emit("close", false);
    },
  },
};
</script>

<template>
  <div>
    <DxPopup
        v-model:visible="isDialog"
        :drag-enabled="true"
        :hide-on-outside-click="false"
        :show-close-button="false"
        :show-title="false"
        width="auto"
        :minWidth= "250"
        :maxWidth= "'80%'"
        height="auto"
        title="Add Relations"
        container="body"
        @hidden="onHidden($event)"
        >
        <div class="dialogPanel">
            <div class="contentPanel">
              <div class="titleContainer">
                <div>
                    <font-awesome-icon v-if="isConfirm" icon="fa-solid fa-check-circle" />
                    <font-awesome-icon v-if="!isConfirm" icon="fa-solid fa-exclamation-circle" />
                </div>
                <div>
                  <label>{{isConfirm ? 'Confirm' : 'Alert'}}</label>
                </div>
             </div>

              <div>
                <pre>{{message}}</pre>
              </div>
            </div>

            <div class="footerPanel">
                <div v-if="isConfirm" class="confirmButtonPanel">
                  <DxButton
                      class="purple"
                      type="normal"
                      width="80"
                      text="Yes"
                      @click="yes()"
                  />

                    <DxButton
                      class="gray"
                      type="normal"
                      width="80"
                      text="No"
                      @click="no()"
                    />
                </div>
               
                <div v-if="!isConfirm" class="alertButtonPanel">
                  <DxButton
                    class="purple"
                    type="normal"
                    width="80"
                    text="Ok"
                    @click="yes()"
                  />
                </div>
            </div>
        </div>
      </DxPopup>
  </div>
</template>

<style scoped lang="scss">
.dialogPanel {
  display: flex;
  flex-direction: column;
  gap: 30px;
  width: 100%;
  height: 100%;
  padding: 10px;
  
  .contentPanel {
    display: flex;
    flex-direction: column;
    gap: 30px;
    width: 100%;
    height: calc(100% - 55px);

    .titleContainer {
      display: flex;
      flex-direction: row;
      gap: 10px;
      font-size: 18px;
    }

    pre {
      font-family: "Poppins Medium";
      word-break: break-word;
      white-space: break-spaces;
    }
  }

  .footerPanel {
    display: flex;
    flex-direction: row;
    height: 35px;

    .confirmButtonPanel {
      width: 100%;
      display: flex;
      flex-direction: row;
      gap: 10px;
      justify-content: center;
    }

    .alertButtonPanel {
      width: 100%;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
