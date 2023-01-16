<script>
import { saveAs } from 'file-saver';

export default {
  name: "JsonViewDialog",
  props: {
    propsIsDialog: {
      type: Boolean,
      required: true,
    },
    propsJSON: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      isDialog: false,
      jsonData: "",
      dtId: "",
      options: {
        fontSize : 14, 
        automaticLayout: true, 
        minimap: {
          enabled: false
        },
        readOnly: false
      },
      monaco: undefined,
      editorLangage: 'json',
      formatList: [
        {value: 'JSON', display: 'JSON'},
        {value: 'TURTLE', display: 'TURTLE'},
        {value: 'RDFXML', display: 'RDF/XML'},
        {value: 'RDFJSON', display: 'RDF/JSON'},
        {value: 'JSONLD', display: 'JSON-LD'}
      ],
      formatSelectedItem: "JSON"
    };
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.jsonData = this.propsJSON;
    this.dtId = JSON.parse(this.jsonData)["dt-id"];
  },
  methods: {
    apply() {
      try {
        let jsonObject = JSON.parse(this.jsonData);

        let param = {
          "dt-id": this.dtId,
          "json": this.jsonData,
          "uuid": this.uuid
        }

        fetch("/api/twin/edit", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(param),
        })
          .then((resp) => resp.json())
          .then((result) => {
            this.emitter.emit("setLoading", false);

            if (result.succeeded) {
              this.emitter.emit("twinsChanged");

              this.returnData = jsonObject;
              this.close();
            } else {
              this.emitter.emit("showErrorDialog", result.data);
            }
          })
          .catch((err) => {
            this.emitter.emit("setLoading", false);

            console.error(err);
            this.emitter.emit("showErrorDialog", err);
          });
      } catch(e) {
        this.emitter.emit("showConfirmDialog", {
            message: "JSON 형태가 올바르지 않습니다.",
            isConfirm: false
        });
      }
    },
    close() {
      this.isDialog = false;
    },
    editorWillMount(monaco) {
      this.monaco = monaco;
    },
    editorDidMount(editor) {
      this.monaco.editor.defineTheme('myTheme', {
        base: 'vs-dark',
        inherit: true,
        rules: [{ background: '#1F2128' }],
        colors: {
          'editor.background': '#1F2128',
        }
      });
      this.monaco.editor.setTheme('myTheme');
    },
    onFormatChanged() {
      if (this.formatSelectedItem == 'JSON' || this.formatSelectedItem == 'RDFJSON' || this.formatSelectedItem == 'JSONLD') {
        this.editorLangage = "json";
      } else if ( this.formatSelectedItem == "RDFXML") {
        this.editorLangage = "xml";
      } else {
        this.editorLangage = "plaintext";
      }

      if (this.formatSelectedItem == 'JSON') {
        this.options.readOnly = false;
        this.jsonData = this.propsJSON;
      } else {
        this.options.readOnly = false;
        this.emitter.emit("setLoading", true);
        
        let param = {
          "dt-id": this.dtId,
          "format": this.formatSelectedItem
        }

        fetch("/api/twin/getOtherFormat", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(param),
        })
          .then((resp) => resp.json())
          .then((result) => {
            this.emitter.emit("setLoading", false);

            if (result.succeeded) {
              this.jsonData = result.data;
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
    download() {
      this.emitter.emit("setLoading", true);

      let param = {
       "type": "single",
       "dt-id": this.dtId,
       "format": this.formatSelectedItem
      }

      let fileName = "twin.json";
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
    },
    onHidden(e) {
      this.$emit("close", this.returnData);
    },
  },
};
</script>

<template>
  <DxPopup
      v-model:visible="isDialog"
      :drag-enabled="true"
      :hide-on-outside-click="false"
      :show-close-button="true"
      :show-title="true"
      :width="800"
      max-width="80%"
      height="80%"
      title="Information"
      container="body"
      @hidden="onHidden($event)"
    >
      <div class="dialogPanel">
        <div class="editPanel">
          <MonacoEditor
            :options="options"
            :language="editorLangage"
            v-model:value="jsonData"
            @editorWillMount="editorWillMount"
            @editorDidMount="editorDidMount"
          ></MonacoEditor>
        </div>

        <div class="footerPanel">
          <div class="leftPanel">
            <DxSelectBox
                :items="formatList"
                value-expr="value"
                display-expr="display"
                v-model:value="formatSelectedItem"
                @value-changed="onFormatChanged"
                width="200"
            />
          </div>

          <div class="rightPanel">
            <DxButton
              class="red"
              type="normal"
              text="Download"
              @click="download()"
            />

            <transition name="fade">
              <div v-if="formatSelectedItem == 'JSON'">
                <DxButton
                class="purple"
                type="normal"
                text="Apply"
                @click="apply()"
              />
              </div>
            </transition>

            <DxButton
              class="gray"
              type="normal"
              text="Close"
              @click="close()"
            />
          </div>
        </div>
      </div>
    </DxPopup>
</template>

<style scoped lang="scss">
.dialogPanel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  height: 100%;
}

.editPanel {
  display: flex;
  width: 100%;
  height: calc(100% - 55px);
  border: 1px solid #343434;
  border-radius: 10px;
  padding: 5px;
}

.footerPanel {
  display: flex;
  flex-direction: row;
  gap: 10px;

  .leftPanel {
    flex: 1 1 0;
  }

  .rightPanel {
    display: flex;
    flex-direction: row;
    gap: 10px;
    justify-content: flex-end;
  }
}
</style>
