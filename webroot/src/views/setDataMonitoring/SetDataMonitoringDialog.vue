<script>
import { setTransitionHooks } from 'vue';


export default {
  name: "SetDataMonitoringDialog",
  props: {
    propsIsDialog: {
      type: Boolean,
      required: true,
    },
    propsTwin: {
      type: Object,
      required: true,
    }
  },
  components: {
    
  },
  data() {
    return {
      isDialog: false,
      twin: undefined,
      selectedIndex: 0,
      tabs: [
        {
          id: 0,
          text: 'Ditto'
        },
        {
          id: 1,
          text: 'Mindsphere'
        }
      ],
      monaco: undefined,
      editor: undefined,
      options: {
          fontSize: 14,
          automaticLayout: true,
          minimap: {
              enabled: false
          },
          lineNumbers: "off"
      },
      dittoJsonData: "",
      isDropZoneActive: false,
      fileName: "",
      fileSize: "",
      isFileUploadSuccess: false,
      mindshpereId: "",
      mindshpereSecret: "",
      mindshpereAssetId: "",
      returnData: undefined
    };
  },
  computed: {
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.twin = this.propsTwin;
    this.getOldValue();
  },
  mounted() {
   
  },
  beforeUnmount() {
   
  },
  methods: {
    isMobile() {
      if(/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
        return true
      } else {
        return false
      }
    },
    editorWillMount(monaco) {
        this.monaco = monaco;
    },
    editorDidMount(editor) {
        this.editor = editor;
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
    getOldValue() {
      let features = this.twin["features"] || [];
      if (features.length > 0) {
        let feature = features.find(d => d.name == 'mindsphere' || d.name == 'ditto');
          if (feature.name == 'mindsphere') {
            if (feature["authorization"]) {
              this.mindshpereId = feature["authorization"]["id"] || "";
              this.mindshpereSecret = feature["authorization"]["secret"] || "";
            }

            this.mindshpereAssetId = feature["assets-id"] || "";
            this.selectedIndex = 1;
          } else {
              if (feature["thingId"]) {
                this.getThingInfo(feature["thingId"]);
              }
          }
      }
    },
    getThingInfo(thingId) {
      this.emitter.emit("setLoading", true);
      fetch("/api/twin/get-thing", {
          method: "POST",
          headers: {
          "Content-Type": "application/json",
          },
          body: JSON.stringify({
            thingId: thingId
          }),
      })
      .then((resp) => resp.json())
      .then((result) => {
        this.emitter.emit("setLoading", false);

        if (result.succeeded && result.data) {
          let jsonData = JSON.parse(result.data);
          this.dittoJsonData = JSON.stringify(jsonData, null, 2);
        } 
      })
      .catch((err) => {
          this.emitter.emit("setLoading", false);
      });
    },
    onTabValueChanged() {
      setTimeout(()=>{
        this.$refs["popup"].instance.option('position', {at: 'center'});
      }, 100)
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
          this.dittoJsonData = reader.result.toString();
          this.isFileUploadSuccess = true;
        }.bind(this);

        reader.readAsText(file, 'UTF-8');
        e.value = "";
      }

      if (this.dittoJsonData) {
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
    addThing() {
      this.emitter.emit("setLoading", true);
      let param = {
          "thingJson": this.dittoJsonData,
          "twinJson": JSON.stringify(this.twin),
          "uuid": this.uuid
      }

      fetch("/api/twin/add-thing", {
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
            this.returnData = this.twin;
            this.close();
          } else {
            this.emitter.emit("showConfirmDialog", { message: result.data, isConfirm: false});
          }
      })
      .catch((err) => {
          this.emitter.emit("setLoading", false);

          console.error(err);
          this.emitter.emit("showConfirmDialog", { message: err, isConfirm: false});
      });
    },
    editTwin() {
      this.emitter.emit("setLoading", true);

      let param = {
          "dt-id": this.twin["dt-id"],
          "json": JSON.stringify(this.twin),
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
            this.returnData = this.twin;
            this.close();
          } else {
            this.emitter.emit("showConfirmDialog", { message: result.data, isConfirm: false});
          }
      })
      .catch((err) => {
          this.emitter.emit("setLoading", false);

          console.error(err);
          this.emitter.emit("showConfirmDialog", { message: err, isConfirm: false});
      });
    },
    create() {
      if (this.selectedIndex == 0) {
        this.addThing();
      } else {
        if (!this.mindshpereId) {
          this.emitter.emit("showConfirmDialog", {
              message: "CredentialID를 입력하세요.",
              isConfirm: false,
              callback: () => this.$refs["textbox-id"].instance.focus()
          });
          return;
        }

        if (!this.mindshpereSecret) {
          this.emitter.emit("showConfirmDialog", {
              message: "CredentialSecret 입력하세요.",
              isConfirm: false,
              callback: () => this.$refs["textbox-secret"].instance.focus()
          });
          return;
        }

        if (!this.mindshpereAssetId) {
          this.emitter.emit("showConfirmDialog", {
              message: "AssetId 입력하세요.",
              isConfirm: false,
              callback: () => this.$refs["textbox-assetid"].instance.focus()
          });
          return;
        }

        let data = {
          "name": "mindsphere",
          "authorization": {
            "id": this.mindshpereId,
            "secret": this.mindshpereSecret
          },
          "assets-id": this.mindshpereAssetId
        }

        let features = this.twin["features"] || [];
        let feature = features.find(d => d.name == data.name);
        if (feature) {
            features.splice(features.indexOf(feature), 1, data);
        } else {
            features.push(data);
        }

        this.twin["features"] = features;

        this.editTwin();
      }
    },
    close() {
      this.isDialog = false;
    },
    onHidden(e) {
      this.$emit("close", this.returnData);
    },
  },
};
</script>

<template>
    <div>
        <DxPopup
            v-model:visible="isDialog"
            ref="popup"
            :drag-enabled="true"
            :hide-on-outside-click="false"
            :show-close-button="true"
            :show-title="true"
            :width="700"
            max-width="80%"
            :height="selectedIndex == 0 ? '80%' : 'auto'"
            title="Set Data Monitoring"
            container="body"
            @hidden="onHidden($event)"
            >
            <div class="dialogPanel" v-if="!isMobile()">
                <div class="contentPanel">
                  <DxTabs
                    :data-source="tabs"
                    v-model:selected-index="selectedIndex"
                    @selection-changed="onTabValueChanged()"
                  />

                  <div class="ditto" :class="selectedIndex == 0 ? 'visible' : 'none-visible'">
                    <div class="editPanel">
                      <MonacoEditor
                        :options="options"
                        language="json"
                        v-model:value="dittoJsonData"
                        @editorWillMount="editorWillMount"
                        @editorDidMount="editorDidMount"
                      ></MonacoEditor>
                    </div>

                    <div class="dropPanel">
                      <div
                        id="dropzone-external"
                        class="flex-box"
                        :class="[isDropZoneActive
                          ? 'dx-theme-accent-as-border-color dropzone-active'
                          : 'dx-theme-border-color']"
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
                  </div>

                  <div class="mindshpere" :class="selectedIndex == 1 ? 'visible' : 'none-visible'">
                    <div class="field-set" >
                      <div class="field-label">
                        CredentialID : 
                      </div>

                      <div class="field-value">
                        <DxTextBox :show-clear-button="true" v-model:value="mindshpereId" ref="textbox-id"/>
                      </div>
                    </div>          
                    
                    <div class="field-set" >
                      <div class="field-label">
                        CredentialSecret  : 
                      </div>

                      <div class="field-value">
                        <DxTextBox :show-clear-button="true" v-model:value="mindshpereSecret" ref="textbox-secret"/>
                      </div>
                    </div>  

                    <div class="field-set" >
                      <div class="field-label">
                          AssetId : 
                      </div>

                      <div class="field-value">
                        <DxTextBox :show-clear-button="true" v-model:value="mindshpereAssetId" ref="textbox-assetid"/>
                      </div>
                    </div>  
                  </div>
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
              <div class="contentPanel">
                  <DxTabs
                    :data-source="tabs"
                    v-model:selected-index="selectedIndex"
                    @selection-changed="onTabValueChanged()"
                  />

                  <div class="ditto" :class="selectedIndex == 0 ? 'visible' : 'none-visible'">
                    <div class="editPanel">
                      <MonacoEditor
                        :options="options"
                        language="json"
                        v-model:value="dittoJsonData"
                        @editorWillMount="editorWillMount"
                        @editorDidMount="editorDidMount"
                      ></MonacoEditor>
                    </div>
                  </div>

                  <div class="mindshpere" :class="selectedIndex == 1 ? 'visible' : 'none-visible'">
                    <div class="field-set" >
                      <div class="field-label">
                        CredentialID : 
                      </div>

                      <div class="field-value">
                        <DxTextBox :show-clear-button="true" v-model:value="mindshpereId" ref="textbox-id"/>
                      </div>
                    </div>          
                    
                    <div class="field-set" >
                      <div class="field-label">
                        CredentialSecret  : 
                      </div>

                      <div class="field-value">
                        <DxTextBox :show-clear-button="true" v-model:value="mindshpereSecret" ref="textbox-secret"/>
                      </div>
                    </div>  

                    <div class="field-set" >
                      <div class="field-label">
                          AssetId : 
                      </div>

                      <div class="field-value">
                        <DxTextBox :show-clear-button="true" v-model:value="mindshpereAssetId" ref="textbox-assetid"/>
                      </div>
                    </div>  
                  </div>
                </div>

              <div class="footerPanel">
                <DxButton
                  id="file-upload-button"
                  class="purple"
                  type="normal"
                  text="Select a File"
                  v-if="(selectedIndex == 0)"
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
    </div>
</template>

<style scoped lang="scss">
.dialogPanel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  height: 100%;

  .contentPanel {
    display: flex;
    flex-direction: column;
    height: calc(100% - 55px);
    gap: 20px;
    
    .ditto {
      width: 100%;
      height: calc(100% - 64px);
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .mindshpere {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .visible {
      display: flex;
    }

    .none-visible {
      display: none;
    }

    .field-set {
      display: flex;
      flex-direction: row;
      gap: 20px;
      align-items: center;
      padding: 0px 30px;

      .field-label {
        width: 130px;
        text-align: right;
      }

      .field-value {
        width: calc(100% - 150px);
      }
    }

    .editPanel {
      display: flex;
      width: 100%;
      height: calc(100% - 190px);
      border: 1px solid #343434;
      border-radius: 10px;
      padding: 5px;
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

  .contentPanel {
    display: flex;
    flex-direction: column;
    height: calc(100% - 55px);
    gap: 20px;
    
    .ditto {
      width: 100%;
      height: calc(100% - 64px);
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .mindshpere {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .visible {
      display: flex;
    }

    .none-visible {
      display: none;
    }

    .field-set {
      display: flex;
      flex-direction: row;
      gap: 20px;
      align-items: center;
      padding: 0px 30px;

      .field-label {
        width: 130px;
        text-align: right;
      }

      .field-value {
        width: calc(100% - 150px);
      }
    }

    .editPanel {
      display: flex;
      width: 100%;
      height: 100%;
      border: 1px solid #343434;
      border-radius: 10px;
      padding: 5px;
    }
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
