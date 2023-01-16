<script>
import AddRelationsDialog from "@/views/addRelations/AddRelationsDialog.vue";
import { parse } from "@babel/parser";

export default {
  name: "CreateTwinDialog",
  props: {
    propsIsDialog: {
      type: Boolean,
      required: true,
    }
  },
  components: {
    AddRelationsDialog
  },
  data() {
    return {
      isDialog: false,
      isDropZoneActive: false,
      fileName: "",
      fileSize: "",
      isFileUploadSuccess: false,
      jsonSample: {
        "dt-id": "",
        "hosting-iri": "",
        "name": "",
        "description": "",
        "baseurl": "",
        "edit": "",
        "version": "",
        "privacy": ""

      },
      jsonData: "",
      options: {
        fontSize : 14, 
        automaticLayout: true, 
        minimap: {
          enabled: false
        } 
      },
      monaco: undefined,
      isAddRelationsDialog: false
    };
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.jsonData = JSON.stringify(this.jsonSample, null, 2);
  },
  methods: {
    isMobile() {
      if(/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
        return true
      } else {
        return false
      }
    },
    create() {
      try {
        JSON.parse(this.jsonData);

        this.emitter.emit("setLoading", true);

        let param = {
          json: this.jsonData,
          uuid: this.uuid
        }

        fetch("/api/twin/add", {
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
        this.emitter.emit("setLoading", false);
        
        let message = "JSON 형태가 올바르지 않습니다.";
        this.emitter.emit("showConfirmDialog", {
          message: message,
          isConfirm: false
        });

        return;
      }
    },
    close() {
      this.isDialog = false;
    },
    clickSelectDtID() {

    },
    clickAddRelations() {
      this.showAddRelationsDialog();
    },
    clickInitializeJSON() {
      let message = "데이터를 초기화하시겠습니까?";
      this.emitter.emit("showConfirmDialog", {
        message: message,
        isConfirm: true,
        callback: (state) => {
          if (state) {
            this.jsonData = JSON.stringify(this.jsonSample, null, 2);
          }
        },
      });
    },
    onDropZoneEnter(e) {
      if (e.dropZoneElement.id === 'dropzone-external') {
        this.isDropZoneActive = true;
      }
    },
    onDropZoneLeave(e) {
      if (e.dropZoneElement.id === 'dropzone-external') {
        this.isDropZoneActive = false;
      }
    },
    onDropZoneValueChanged(e) {
      let readCallback = () => {
        var file = e.value[0];

        this.fileName= file.name;
        this.fileSize = this.getByteSize(file.size);

        let extension= this.fileName.slice(this.fileName.indexOf(".") + 1).toLowerCase();
        if(extension != "json" && extension != "text" && extension != "txt") {
          this.isFileUploadSuccess = false;
          return; 
        }

        const reader = new FileReader();
        reader.onload = function () {
          this.jsonData = reader.result.toString();
          this.isFileUploadSuccess = true;
        }.bind(this);

        reader.readAsText(file, 'UTF-8');
        e.value = "";
      }

      if (this.jsonData) {
        let message = "입력하신 파일로 데이터를 변경하시겠습니까?";
        this.emitter.emit("showConfirmDialog", {
          message: message,
          isConfirm: true,
          callback: (state) => {
            if (state) {
              readCallback();
            } else {
              e.value = "";
            }
          },
        });
      } else {
        readCallback();
      }

     
    },
    getByteSize(bytes, decimals = 2) {
      if (bytes === 0) return '0 Bytes';

      const k = 1024;
      const dm = decimals < 0 ? 0 : decimals;
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

      const i = Math.floor(Math.log(bytes) / Math.log(k));

      return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
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

      editor.addAction({
        id: "Add Relations",
        label: "Add Relations",
        contextMenuOrder: 1,
        contextMenuGroupId: "10_customMenu",
        run: () => {
          this.clickAddRelations();
        }
      });

      editor.addAction({
        id: "Initialize JSON",
        label: "Initialize JSON",
        contextMenuOrder: 2,
        contextMenuGroupId: "10_customMenu",
        run: () => {
          this.clickInitializeJSON();
        }
      });
    },
    onHidden(e) {
      this.$emit("close");
    },
    showAddRelationsDialog() {
      try {
        JSON.parse(this.jsonData);
      } catch(e) {
        let message = "JSON 형태가 올바르지 않습니다.";
        this.emitter.emit("showConfirmDialog", {
          message: message,
          isConfirm: false
        });

        return;
      }

      this.isAddRelationsDialog = true;
    },
    closeAddRelationsDialog(data) {
      this.isAddRelationsDialog = false;

      if (data) {
        let json = JSON.parse(this.jsonData);
        let relations = json["relations"] || [];
        relations = relations.concat(data);
        json["relations"] = relations;

        this.jsonData = JSON.stringify(json, null, 2);
      }
    }
  },
};
</script>

<template>
  <div>
    <DxPopup
        v-model:visible="isDialog"
        :drag-enabled="true"
        :hide-on-outside-click="false"
        :show-close-button="true"
        :show-title="true"
        :width="800"
        max-width="80%"
        height="80%"
        title="Create Twin"
        container="body"
        @hidden="onHidden($event)"
      >
        <div class="dialogPanel" v-if="!isMobile()">
          <div class="buttonPanel">
            <DxButton
              class="blue"
              type="normal"
              text="Add Relations"
              @click="clickAddRelations($event)"
            />

            <DxButton
              class="blue"
              type="normal"
              text="Initialize JSON"
              @click="clickInitializeJSON($event)"
            />
          </div>

          <div class="editPanel">
            <MonacoEditor
              :options="options"
              language="json"
              v-model:value="jsonData"
              @editorWillMount="editorWillMount"
              @editorDidMount="editorDidMount"
            ></MonacoEditor>
          </div>

          <div class="dropPanel">
            <div
              id="dropzone-external"
              class="flex-box"
              :class="[isDropZoneActive ? 'dx-theme-accent-as-border-color dropzone-active': 'dx-theme-border-color']"
            >
                <div class="fileExistPanel" v-if="fileName && fileSize">
                  <div class="infoPanel">
                    <div class="fileInfoPanel">
                      <div class="file-name">
                        {{fileName}}
                      </div>

                      <div class="file-size">
                        {{fileSize}}
                      </div>
                    </div>

                    <div class="fileStatusPanel">
                      <div v-if="isFileUploadSuccess">
                        File read success
                      </div>

                      <div v-if="!isFileUploadSuccess">
                        File read fail. (Allowed file extensions: .json, .txt, .text)
                      </div>
                    </div>
                  </div>
                </div>  
                
                <div class="file-icon" v-if="!fileName && !fileSize">
                  <font-awesome-icon icon="fa-solid fa-file-upload" />
                </div>

                <div id="dropzone-text" class="flex-box">
                  <span>Drag & Drop the desired file</span>
                  <span>or</span>
                  <div class="selectedButton">
                    Select a File
                  </div>
                </div>
              </div>
              
            <DxFileUploader
              id="file-uploader"
              dialog-trigger="#dropzone-external"
              drop-zone="#dropzone-external"
              :multiple="false"
              :visible="false"
              upload-mode="useButtons"
              accept="application/json,text/plain"
              @drop-zone-enter="onDropZoneEnter"
              @drop-zone-leave="onDropZoneLeave"
              @value-changed="onDropZoneValueChanged"
            />
          </div>

          <div class="footerPanel">
            <DxButton
              class="purple"
              type="normal"
              text="Apply"
              @click="create()"
            />

            <DxButton
              class="gray"
              type="normal"
              text="Close"
              @click="close()"
            />
          </div>
        </div>

        <div class="mobileDialogPanel" v-if="isMobile()">
          <div class="buttonPanel">
            <DxButton
              class="blue"
              type="normal"
              text="Add Relations"
              @click="clickAddRelations($event)"
            />

            <DxButton
              class="blue"
              type="normal"
              text="Initialize JSON"
              @click="clickInitializeJSON($event)"
            />
          </div>

          <div class="editPanel">
            <MonacoEditor
              :options="options"
              language="json"
              v-model:value="jsonData"
              @editorWillMount="editorWillMount"
              @editorDidMount="editorDidMount"
            ></MonacoEditor>
          </div>

          <div class="footerPanel">
            <DxButton
              id="file-upload-button"
              class="purple"
              type="normal"
              text="Select a File"
            />

            <DxFileUploader
              id="file-uploader"
              dialog-trigger="#file-upload-button"
              :multiple="false"
              :visible="false"
              upload-mode="useButtons"
              accept="application/json,text/plain"
              @value-changed="onDropZoneValueChanged"
            />

            <DxButton
              class="purple"
              type="normal"
              text="Apply"
              @click="create()"
            />

            <DxButton
              class="gray"
              type="normal"
              text="Close"
              @click="close()"
            />
          </div>
        </div>
      </DxPopup>

      <AddRelationsDialog
        v-if="isAddRelationsDialog"
        :propsIsDialog="isAddRelationsDialog"
        @close="closeAddRelationsDialog"
      />
  </div>
</template>

<style scoped lang="scss">
.dialogPanel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  height: 100%;

  .buttonPanel {
    display: flex;
    flex-direction: row;
    justify-content: flex-end;
    gap: 10px;
  }

  #dropzone-external {
    width: 100%;
    height: 170px;
    border-width: 2px;
    border-style: dashed;
    border-radius: 5px;
    padding: 10px;
    cursor: pointer;
  }

  #dropzone-external > * {
    pointer-events: none;
  }

  #dropzone-external.dropzone-active {
    border-style: solid;
  }

  .widget-container > span {
    font-size: 22px;
    font-weight: bold;
    margin-bottom: 16px;
  }

  .file-icon {
    svg {
      font-size: 28px;
    }
  }

  #dropzone-text {
    margin-top: 20px;

    span {
      font-weight: 100;
      opacity: 0.5;
      margin-bottom: 10px;
    }

    .selectedButton {
      width: 100px;
      height: 30px;
      background-color: #7364DB;
      border-radius: 5px;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }

  #upload-progress {
    display: flex;
    margin-top: 10px;
  }

  .flex-box {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
  }

  .fileExistPanel {
    display: flex;
    flex-direction: row;
    gap: 5px;
    
    .infoPanel {
      display: flex;
      flex-direction: column;

      .fileInfoPanel {
        display: flex;
        flex-direction: row;

        .file-name {
          float: left;
          max-width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          padding-bottom: 3.5px;
          color: #dedede;
        }

        .file-size {
          margin-left: 4px;
          float: left;
          vertical-align: super;
          font-size: 10px;
          padding-bottom: 3.5px;
        }
      }
    }
    .fileStatusPanel {
      color: #787878;
      float: left;
      font-size: 12px;
      height: 16px;
      margin-top: -3px;
    }
  }

  .editPanel {
    display: flex;
    width: 100%;
    height: calc(100% - 300px);
    border: 1px solid #343434;
    border-radius: 10px;
    padding: 5px;
  }

  .footerPanel {
    display: flex;
    flex-direction: row;
    gap: 10px;
    justify-content: flex-end;
  }
}

.mobileDialogPanel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  height: 100%;

  .buttonPanel {
    display: flex;
    flex-direction: row;
    justify-content: center;
    gap: 10px;

    .dx-button-mode-contained {
        flex: 1 1 0;
    }
  }

  .editPanel {
    display: flex;
    width: 100%;
    height: calc(100% - 100px);
    border: 1px solid #343434;
    border-radius: 10px;
    padding: 5px;
  }

  .footerPanel {
    display: flex;
    flex-direction: row;
    gap: 10px;
    justify-content: center;

    .dx-button-mode-contained {
        flex: 1 1 0;
    }
  }
}

</style>
