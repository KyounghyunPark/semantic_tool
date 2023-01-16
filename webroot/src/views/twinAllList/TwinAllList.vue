<script>
import JsonViewDialog from "../jsonView/JsonViewDialog.vue"

export default {
    name: "TwinAllList",
    components: {
        JsonViewDialog
    },
    data() {
        return {
          topPanelHeight: 0,
          filterPanelHeight: 0,
          filterString: "",
          filterSelectItems: [
            'All Fields',
            'dt-id',
            'name',
            'description',
            'nationality'
          ],
          filterSelected: "All Fields",
          allTwinList: [],
          twinList: [],
          isJsonViewDialog: false,
          jsonViewData: ""
        };
    },
    mounted() {
      window.addEventListener("resize", this.onWindowsResize);
      this.emitter.on("twinsChanged", this.getTwinList);
      this.onWindowsResize();
      this.getTwinList();
    },
    beforeUnmount() {
      window.removeEventListener("resize", this.onWindowsResize);
      this.emitter.off("twinsChanged");
    },
    methods: {
      onWindowsResize(event) {
        setTimeout(() => {
          if (this.$refs["twinTopPanel"]) {
            this.topPanelHeight = this.$refs["twinTopPanel"].clientHeight > 0 ? this.$refs["twinTopPanel"].clientHeight + 30 : 0;
          } else {
            this.topPanelHeight = 0;
          }
          if (this.$refs["twinFilterPanel"]) {
            this.filterPanelHeight = this.$refs["twinFilterPanel"].clientHeight + 30;  
          } else {
            this.filterPanelHeight = 0;  
          }
        }, 100);
      },
      getTwinList() {
        fetch("/api/twin/getList", {
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
            this.allTwinList = result.data;
            this.onSearchClick();
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
      isRelationExist(twin, type) {
        let relations = twin['relations'];
        if (relations && Array.isArray(relations)) {
          return relations.filter(d => d.relationType == type).length > 0;
        }
      },
      onSearchClick() {
        if (this.filterSelected == "All Fields") {
            let tempTwinList = [];
            this.allTwinList.forEach(d => {
                let keys = Object.keys(d);
                keys.forEach(key => {
                    if (typeof(d[key]) == 'string') {
                        if (d[key].toLowerCase().includes(this.filterString.toLowerCase())) {
                            if (tempTwinList.indexOf(d) == -1) {
                                tempTwinList.push(d);
                            }
                        }
                    }
                });
            });

            this.twinList = tempTwinList;
        } else { 
            this.twinList = this.allTwinList.filter(d => d[this.filterSelected] && d[this.filterSelected].toLowerCase().includes(this.filterString.toLowerCase()));
        } 
    },
    onJSONViewClick(twin) {
        this.jsonViewData = JSON.stringify(twin, null, 2);
        this.isJsonViewDialog = true;
    },
    onGraphClick(twin) {
      if (twin && twin["dt-id"]) {
        let dtIdSplits = twin["dt-id"].split("/");
        let path = dtIdSplits[dtIdSplits.length - 1];

        this.emitter.emit("changeMenu", "detailInformation/" + path);
      }
    },
    closeJsonViewDialog() {
        this.jsonViewData = "";
        this.isJsonViewDialog = false;
    },
    onDeleteTwin(twin) {
      this.emitter.emit("showConfirmDialog", {
          message: "선택한 TWIN을 삭제하시겠습니까?",
          isConfirm: true,
          callback: (state) => {
              if (state) {
                this.emitter.emit("setLoading", true);

                let param = {
                  "dt-id": twin["dt-id"],
                  "uuid": this.uuid
                }

                fetch("/api/twin/delete", {
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
                    this.getTwinList();
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
    }
  }
}

</script>
<template>
    <div id="twinAllList">
        <div class="topPanel" ref="twinTopPanel">
          <div class="totalCount">
            There are {{allTwinList.length}} Digital Twins
          </div>

          <div class="subTitle">
            ( Enter Filter values )
          </div>
        </div>

        <div class="filterPanel" ref="twinFilterPanel">
          <div class="textBoxPanel">
            <DxTextBox
              :show-clear-button="true"
              mode="search"
              v-model:value="filterString"
              @enter-key="onSearchClick"
            />
          </div>

          <div class="tagBoxPanel">
            <DxSelectBox
              :items="filterSelectItems"
              v-model:value="filterSelected"
            />
          </div>
          
          <div class="buttonPanel">
            <DxButton
              class="purple text-button"
              type="normal"
              text="Search Twins"
              height="100%"
              @click="onSearchClick()"
            /> 

            <DxButton
              class="purple icon-button"
              template="icon-button-template"
              width="40" height="100%"
              @click="onSearchClick()"
            >
              <template #icon-button-template>
                <div class="dx-widget dx-button dx-button-mode-contained dx-button-normal dx-button-has-text dx-button-template-wrapper purple">
                  <font-awesome-icon icon="fa-solid fa-search" />
                </div>
                
              </template>
            </DxButton>
            <!-- <button class="purple icon-button" @click="onSearchClick()">
             
            </button> -->
          </div>
        </div>
       
        <div class="listPanel" :style="{'height': `calc(100% - ${topPanelHeight}px - ${filterPanelHeight}px)`}">
            <div class="title">
              Twin List
            </div>

            <div class="contentPanel">
              <DxScrollView
                id="scrollview"
                ref="scrollViewWidget"
                :scroll-by-content="false"
                :scroll-by-thumb="true"
                :show-scrollbar="'always'"
                :bounce-enabled="false"
                reach-bottom-text="Updating..."
              >
                <div style="display:flex; width: 100%; height: 100%;">
                  <div class="gridPanel">
                    <div class="itemPanel" v-for="twin of twinList">
                      <div class="nameInfoPanel">
                        <img v-if="twin.nationality" :src="`/assets/images/country/48/${twin.nationality}.png`"/>
                        <img v-if="!twin.nationality" :src="`assets/images/country/48/WORLD.png`"/>

                        <div class="namePanel">
                          {{twin.name}}
                        </div>
                      </div>

                      <div class="subInfoPanel">
                        <div class="rowPanel">
                          <div class="halfPanel">
                            version: {{twin.version}}
                          </div>

                          <div class="halfPanel right">
                            privacy: {{twin.privacy}}
                          </div>
                        </div>
                        
                        <div class="rowPanel">
                          {{twin['dt-id']}}
                        </div>


                        <div class="description" :title="twin['description']">
                          {{twin['description']}}
                        </div>
                      </div>

                      <div class="relationPanel">
                        <div v-if="isRelationExist(twin, 'parent')" class="parentPanel">P</div>
                        <div v-if="isRelationExist(twin, 'child')" class="childPanel">C</div>
                      </div>

                      <div class="buttonPanel">
                        <DxButton
                          class="blue"
                          type="normal"
                          text="Information"
                          width="100"
                          height="30"
                          @click="onJSONViewClick(twin)"
                        />

                        <DxButton
                          class="orange"
                          type="normal"
                          text="Graph"
                          width="100"
                          height="30"
                          @click="onGraphClick(twin)"
                        />

                        <DxButton
                          class="red"
                          type="normal"
                          text="Delete"
                          width="100"
                          height="30"
                          @click="onDeleteTwin(twin)"
                        />
                      </div>
                    </div>
                  </div>
                </div>
                
              </DxScrollView>
            </div>
        </div>
    </div>

    <JsonViewDialog
      v-if="isJsonViewDialog"
      :propsIsDialog="isJsonViewDialog"
      :propsJSON="jsonViewData"
      @close="closeJsonViewDialog"
    />
</template>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
#twinAllList {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .topPanel {
    align-self: center;
    text-align: center;
    height: 60px;
    margin-bottom: 30px;

    .totalCount {
      font-size: 20px;
    }

    .subTitle {
      color: #797979;
      font-size: 15px;
    }
  }

  .filterPanel {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    width: 100%;
    background-color: #1F2128;
    border-radius: 10px;
    margin-bottom: 30px;
    padding: 20px 30px;
    gap: 20px;

    .textBoxPanel {
      flex: 1 1 0;
      max-width: 100%;
      min-width: 300px;
    }

    .tagBoxPanel {
      width: 150px;
    }

    .buttonPanel {
      .text-button {
        display: block;
      }

      .icon-button {
        display: none;
      }
    }
  }
  
  .listPanel {
    display: flex;
    flex-direction: column;

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

            img {
              width: 32px;
              height: 32px;
              border-radius: 10px;
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
              background-color: #476083;
              border-radius: 12px;
              font-size: 16px;
              font-weight: 700;
              line-height: 24px;
              text-align: center;
            }

            .childPanel {
              width: 24px;
              height: 24px;
              background-color: #478380;
              border-radius: 12px;
              font-size: 16px;
              font-weight: 700;
              line-height: 24px;
              text-align: center;
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

  @media screen and (max-width: 600px) {
    .topPanel {
      display: none;
    }

    .text-button {
      display: none !important;
    }

    .icon-button {
      display: block !important;
    }
  }

  @media screen and (max-width: 420px) {
    .textBoxPanel {
      min-width: 100% !important;
    }
  }
  
}
</style>
