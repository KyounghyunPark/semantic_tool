<script>
import ExpandSparqlDialog from "@/views/expandSparql/ExpandSparqlDialog.vue";

export default {
  name: "SparqlList",
  components: {
    ExpandSparqlDialog
  },
  data() {
    return {
      isExpandSparqlDialog: false,
      options: {
        fontSize : 14, 
        automaticLayout: true, 
        minimap: {
          enabled: false
        },
        lineNumbers: "off"
      },
      monaco: undefined,
      sparqlHistorys: [],
      sparql: 'SELECT *\nWHERE {\n  ?s <https://etri.re.kr#dt-id> "http://dtid.org/3091b292-6104-4ef3-a30f-8275b46b5ab4" .\n  ?s <https://etri.re.kr#relations> ?o .\n  ?o <https://etri.re.kr#dt-id> ?id .\n  ?o <https://etri.re.kr#relationType> ?type .\n}',
      resultData: ""
    };
  },
  mounted() {
    if (localStorage.getItem("sparqlHistory")) {
      this.sparqlHistorys = JSON.parse(localStorage.getItem("sparqlHistory"));
    }
  },
  methods: {
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
    onSearchClick() {
      if (!this.sparql) {
        alert("SPARQL을 입력하세요.");
        return;
      }

      this.emitter.emit("setLoading", true);

      this.resultData = "";
      if (this.sparqlHistorys.length == 5) {
        this.sparqlHistorys.splice(0, 1);
      }
      this.sparqlHistorys.push(this.sparql);
      localStorage.setItem("sparqlHistory", JSON.stringify(this.sparqlHistorys));

      let params = {
        sparql: this.sparql,
      };

      fetch("/api/twin/getSparql", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(params),
      })
        .then((resp) => resp.json())
        .then((result) => {
          this.emitter.emit("setLoading", false);

          if (result.succeeded) {
            this.resultData = result.data;
          } else {
            this.emitter.emit("showErrorDialog", result.data);
          }
        })
        .catch((err) => {
          this.emitter.emit("setLoading", false);

          console.error(err);
          this.emitter.emit("showErrorDialog", err);
        });
    },
    onJSONViewClick(twin) {
        this.jsonViewData = JSON.stringify(twin, null, 2);
        this.isJsonViewDialog = true;
    },
    closeJsonViewDialog() {
        this.jsonViewData = "";
        this.isJsonViewDialog = false;
    },
    onExpandSparqlClick() {
      this.isExpandSparqlDialog = true;
    },
    closeExpandSparqlDialog(data) {
      this.isExpandSparqlDialog = false;
      if (data) {
        this.sparql = data;
      }
    },
    onDeleteTwin(twin) {
      let result = window.confirm("선택한 TWIN을 삭제하시겠습니까?");
      if (result) {
        let index = this.allTwinList.indexOf(twin);
        if (index !== -1) this.allTwinList.splice(index, 1);
        this.onSearchClick();
      }
    },
    onHistoryDelete(index) {
      this.sparqlHistorys.splice(index, 1);
      localStorage.setItem("sparqlHistory", JSON.stringify(this.sparqlHistorys));
    },
    onSparqlHistoryClick(sparqlHistory) {
      this.sparql = sparqlHistory;
    }
  }
};
</script>

<template>
    <div id="twinAllList">
        <div class="filterPanel">
            <div :class="sparqlHistorys.length > 0 ? 'textAreaPanel sparqlVisible' : 'textAreaPanel'" >
                <div class="editorPanel">
                    <MonacoEditor
                        :options="options"
                        language="sparql"
                        v-model:value="sparql"
                        @editorWillMount="editorWillMount"
                        @editorDidMount="editorDidMount"
                    ></MonacoEditor>
                </div>

                <div class="buttonPanel">
                    <DxButton
                        class="purple"
                        type="normal"
                        text="Expand Editor"
                        @click="onExpandSparqlClick"/>

                    <DxButton
                        class="purple"
                        type="normal"
                        text="Run Query"
                        @click="onSearchClick($event)"/>
                </div>
            </div>

            <div class="historyPanel" v-if="sparqlHistorys.length > 0">
                <div class="title">
                    SPARQL HISTORY
                </div>

                <div class="history">
                    <div class="item" v-for="(sparqlHistory, index) in sparqlHistorys" :title="sparqlHistory" @click="onSparqlHistoryClick(sparqlHistory)">
                        <div class="item-text">
                          {{sparqlHistory}}
                        </div>

                        <div class="item-icon" @click="onHistoryDelete(index)">
                          <font-awesome-icon icon="fa-solid fa-times" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
       
        <div class="listPanel">
            <div class="title">
              Result View
            </div>

            <div class="contentPanel">
              <DxTextArea
                :read-only="true"
                v-model:value="resultData"
              />
            </div>
        </div>
    </div>

    <ExpandSparqlDialog 
      v-if="isExpandSparqlDialog"
      :propsIsDialog="isExpandSparqlDialog"
      :propsSparql="sparql"
      @close="closeExpandSparqlDialog"
    />
</template>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
#twinAllList {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;


  .filterPanel {
    display: flex;
    flex-direction: row;
    height: 250px;
    gap: 20px;

    .textAreaPanel {
        display: flex;
        flex-direction: column;
        gap: 10px;
        width: 100%;
        height: 100%;
        background-color: #1F2128;
        border-radius: 10px;
        padding: 20px;

        transition-property: all;
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
        transition-duration: 1s;

        .editorPanel {
            width: 100%;
            height: calc(100% - 45px);
            border: 1px solid #343434;
            border-radius: 10px;
            padding: 5px;
        }

        .buttonPanel {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
    }

    .sparqlVisible {
      width: calc(100% - 420px);

      transition-property: all;
      transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
      transition-duration: 1s;
    }

    .historyPanel {
        width: 400px;
        height: 100%;

        transition-property: all;
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
        transition-duration: 1s;

        .title {
            font-size: 20px;
        }

        .history {
            display: flex;
            flex-direction: column;
            gap: 8px;
            margin-top: 12px;

            .item {
                display: flex;
                flex-direction: row;
                gap: 5px;
                height: 35px;
                width: 100%;
                background: #1F2128;
                border-radius: 5px;
                padding-left: 20px;
                padding-right: 10px;
                line-height: 35px;
                cursor: pointer;

                .item-text {
                  width: calc(100% - 30px);
                  height: 100%;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  font-size: 12px;
                }

                .item-icon {
                  width: 30px;
                  height: 100%;
                  text-align: center;
                  color: #A6A6A6;
                }
                
                .item-icon:hover {
                  color: white;
                }
            }
        }
    }
  }
  
  .listPanel {
    display: flex;
    flex-direction: column;
    height: calc(100% - 260px);
    margin-top: 10px;

    .title {
      height: 40px;
      font-size: 20px;
    }

    .contentPanel {
      display: flex;
      height: calc(100% - 40px);
    }

    .gridPanel {
      width: 100%;
      height: 100%;
      display: grid;
      grid-template-columns: repeat(auto-fill, 350px);
      gap: 20px;
      justify-content: center;

      .itemPanel {
          display: flex;
          flex-direction: column;
          padding: 20px;
          background-color: #1F2128;
          border-radius: 10px;

          .nameInfoPanel {
            display: flex;
            flex-direction: row;
            align-items: center;

            .imagePanel {
              width: 36px;
              height: 36px;

              img {
                width: 100%;
                height: 100%;
                align-items: center;
                background: white;
                align-self: center;
                border-radius: 5px;
              }
            }

            .namePanel {
              margin-left: 20px;
            }
          }
       
          .subInfoPanel {
            display: flex;
            flex-direction: column;
            color: #A6A6A6;
            font-size: 12px;
            margin-top: 20px;

            .rowPanel {
              display: flex;
              flex-direction: row;

              .halfPanel {
                width: 50%;
              }   
              
              .halfPanel.right {
                float: right;
              }   
            }

            .description {
                margin-top: 10px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
          }

          .relationPanel {
            display: flex;
            flex-direction: row;
            justify-content: flex-end;
            gap: 5px;
            margin-top: 10px;
            height: 24px;

            .parentPanel {
              width: 24px;
              height: 24px;
              background-color: #333F50;
              display: flex;
              justify-content: center;
              border-radius: 12px;
              font-size: 16px;
              font-weight: 700;
              font-family: sans-serif;
            }

            .childPanel {
              width: 24px;
              height: 24px;
              background-color: #333F50;
              display: flex;
              justify-content: center;
              border-radius: 12px;
              font-size: 16px;
              font-weight: 700;
              font-family: sans-serif;
            }
          }

          .buttonPanel {
            display: flex;
            flex-direction: row;
            gap: 10px;
            margin-top: 15px;

            .dx-button-mode-contained {
                font-size: 12px;
            }
          }
      }
        
    }
  }
}
</style>
