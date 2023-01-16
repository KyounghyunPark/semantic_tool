<script>
import JsonViewDialog from "@/views/jsonView/JsonViewDialog.vue";

export default {
  name: "AddRelationsDialog",
  props: {
    propsIsDialog: {
      type: Boolean,
      required: true,
    }
  },
  components: {
    JsonViewDialog
  },
  data() {
    return {
      isDialog: false,
      filterString: "",
      filterSelectItems: [
        'All Fields',
        'dt-id',
        'name',
        'description'
      ],
      filterSelected: "All Fields",
      allTwinList: [],
      twinList: [],
      selectedTwinList: [],
      isJsonViewDialog: false,
      jsonViewData: "",
      relationTypes: [
        "parent",
        "child"
      ],
      returnData: undefined,
      totalGridHeight: 0,
      selectGridHeight: 0,
      selectedIndex: 0,
      stepTabpanels: [
        { index: 1, name: "Add Relations"},
        { index: 2, name: "Select Relation Type"}
      ],
      totalDataGrid: undefined
    };
  },
  computed: {
  },
  created() {
    this.isDialog = this.propsIsDialog;
  },
  mounted() {
    this.setDatatableResize();
    this.getTwinList();
    window.addEventListener("resize", this.onWindowsResize);

    setTimeout(() => {
        this.totalDataGrid = this.$refs["totalGrid"].instance;
    }, 100);
  },
  beforeUnmount() {
    window.removeEventListener("resize", this.onWindowsResize);
  },
  methods: {
    onWindowsResize(event) {
      this.setDatatableResize();
    },
    setDatatableResize() {
        setTimeout(() => {
            this.totalGridHeight = document.querySelector(".dx-tabpanel-container").offsetHeight - 35 - 20;
            this.selectGridHeight = document.querySelector(".dx-tabpanel-container").offsetHeight - 35 - 20;
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
            this.twinList = Object.assign([], this.allTwinList);
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
    create() {
        if (this.selectedTwinList.length == 0) {
            this.emitter.emit("showConfirmDialog", {
                message: "Relaition에 추가할 Twin을 선택하세요.",
                isConfirm: false
            });
            return;
        }

        this.returnData = [];
        this.selectedTwinList.forEach(d => {
            let obj = {
                "dt-id": d["dt-id"],
                "relationType": d["relationType"]
            }    

            this.returnData.push(obj);
        });
        
        this.close();
    },
    close() {
      this.isDialog = false;
    },
    onHidden(e) {
      this.$emit("close", this.returnData);
    },
    onSearchClick() {
        if (this.filterSelected == "All Fields") {
            let tempTwinList = [];
            this.allTwinList.forEach(d => {
                let keys = Object.keys(d);
                keys.forEach(key => {
                    if (typeof(d[key]) == 'string') {
                        if (d[key].toLowerCase().includes(this.filterString)) {
                            if (tempTwinList.indexOf(d) == -1) {
                                tempTwinList.push(d);
                            }
                        }
                    }
                });
            });

            this.twinList = tempTwinList;
        } else { 
            this.twinList = this.allTwinList.filter(d => d[this.filterSelected].toLowerCase().includes(this.filterString));
        } 
    },
    back() {
        this.selectedIndex = 0;
    },
    next() {
        let tmpSelectedTwinList = this.totalDataGrid.getSelectedRowsData();
        if (tmpSelectedTwinList.length == 0) {
            this.emitter.emit("showConfirmDialog", {
                message: "Relation에 추가할 TWIN을 선택하세요.",
                isConfirm: false
            });
            return;
        }

        tmpSelectedTwinList.forEach(d => {
            let count = this.selectedTwinList.filter(f => f["dt-id"] == d["dt-id"]).length;
            if (count == 0) {
                let obj = {
                    "name": d["name"],
                    "dt-id" : d["dt-id"],
                    "relationType": "parent"
                }

                this.selectedTwinList.push(obj);
            }
        });

        this.selectedTwinList = [...this.selectedTwinList];
        this.selectedIndex = 1;
    },
    onDeleteTwinList(data) {
        let index = this.selectedTwinList.indexOf(data.data);
        if (index !== -1) this.selectedTwinList.splice(index, 1);
        this.selectedTwinList = [...this.selectedTwinList];
    },
    onJSONViewClick(data) {
        let dtId = data.data["dt-id"];
        let findTwin = this.allTwinList.find(d => d["dt-id"] == dtId);
        if (findTwin) {
            this.jsonViewData = JSON.stringify(findTwin, null, 2);
            this.isJsonViewDialog = true;
        }
    },
    closeJsonViewDialog() {
        this.jsonViewData = "";
        this.isJsonViewDialog = false;
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
            :width="1000"
            max-width="80%"
            height="80%"
            title="Add Relations"
            container="body"
            @hidden="onHidden($event)"
            >
            <div class="dialogPanel">
                <div class="contentPanel">
                    <div class="filterPanel">
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
                        </div>
                    </div>

                    <div class="dataGridPanel" >
                        <DxTabPanel
                            :data-source="stepTabpanels"
                            v-model:selected-index="selectedIndex"
                            :loop="false"
                            :animation-enabled="true"
                            :swipe-enabled="false"
                            :activeStateEnabled="false"
                            :hoverStateEnabled="false"
                            :focusStateEnabled="false"
                            >
                            <template #title="{ data: data }">
                                <div class="titleWrapper">
                                    <div class="titleIndex">
                                        {{data.index}}
                                    </div>
                                    <div class="titleName">
                                        {{data.name}}
                                    </div>
                                </div>
                            </template>
                            <template #item="{ data: data }">
                               <div class="totalDataGridPanel" :style="{'display': selectedIndex == 0 ? 'block' : 'none'}">
                                    <DxDataGrid
                                        id="gridContainer"
                                        :height="totalGridHeight"
                                        :data-source="twinList"
                                        :show-borders="false"
                                        key-expr="dt-id"
                                        ref="totalGrid"
                                    >
                                        <DxScrolling mode="virtual"/>
                                        <DxSorting mode="none"/>
                                        <DxSelection
                                            :select-all-mode="'allPages'"
                                            :show-check-boxes-mode="'onClick'"
                                            mode="multiple"
                                        />

                                        <DxColumn
                                            :width="230"
                                            caption="name"
                                            data-field="name"
                                        />
                                        <DxColumn
                                            caption="description"
                                            data-field="description"
                                        />
                                        <DxColumn
                                            caption="dt-id"
                                            data-field="dt-id"
                                        />

                                        <DxColumn
                                            css-class="buttonTDPanel centerTitle"
                                            :width="60"
                                            caption=""
                                            cell-template="cellTemplate"
                                        />
                                        
                                        <template #cellTemplate="{ data }">
                                            <div class="dataGridButtonPanel">
                                                <div class="buttonPanel jsonView" @click="onJSONViewClick(data)">
                                                    <font-awesome-icon icon="fa-solid fa-eye" />
                                                </div>
                                            </div>
                                        </template>
                                    </DxDataGrid>

                                    <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
                                        <DxButton
                                            class="gray"
                                            type="normal"
                                            text="Next"
                                            @click="next()"
                                        />
                                    </div>
                               </div>

                               <div class="selectDataGridPanel" :style="{'display': selectedIndex == 1 ? 'block' : 'none'}">
                                    <DxDataGrid
                                        id="selectGridContainer"
                                        :height="selectGridHeight"
                                        :data-source="selectedTwinList"
                                        :show-borders="false"
                                    >
                                        <DxScrolling mode="virtual"/>
                                        <DxSorting mode="none"/>

                                        <DxColumn
                                            :width="230"
                                            caption="name"
                                            data-field="name"
                                        />

                                        <DxColumn
                                            caption="dt-id"
                                            data-field="dt-id"
                                        />

                                        <DxColumn
                                            css-class="buttonTDPanel centerTitle"
                                            :width="120"
                                            caption="relationType"
                                            cell-template="cellTemplate0"
                                        />
                                        <template #cellTemplate0="{ data }">
                                            <div class="dataGridButtonPanel">
                                                <DxSelectBox
                                                    :width="100"
                                                    :height="34"
                                                    :items="relationTypes"
                                                    v-model:value="data.data.relationType"
                                                />
                                            </div>
                                        </template>
                                        
                                        <DxColumn
                                            css-class="buttonTDPanel centerTitle"
                                            :width="60"
                                            caption=""
                                            cell-template="cellTemplate1"
                                        />
                                        <template #cellTemplate1="{ data }">
                                            <div class="dataGridButtonPanel">
                                                <div class="buttonPanel jsonView" @click="onJSONViewClick(data)">
                                                    <font-awesome-icon icon="fa-solid fa-eye" />
                                                </div>
                                            </div>
                                        </template>

                                        <DxColumn
                                            css-class="buttonTDPanel centerTitle"
                                            :width="60"
                                            caption=""
                                            cell-template="cellTemplate2"
                                        />
                                        <template #cellTemplate2="{ data }">
                                            <div class="dataGridButtonPanel">
                                                <div class="buttonPanel delete" @click="onDeleteTwinList(data)">
                                                    <font-awesome-icon icon="fa-solid fa-trash" />
                                                </div>
                                            </div>
                                        </template>
                                    </DxDataGrid>

                                    <div style="margin-top: 20px; display: flex; justify-content: space-between;">
                                        <DxButton
                                            class="gray"
                                            type="normal"
                                            text="Back"
                                            @click="back()"
                                        />

                                        <DxButton
                                            class="green"
                                            type="normal"
                                            text="Apply"
                                            @click="create()"
                                        />
                                    </div>
                               </div>
                            </template>
                        </DxTabPanel>
                        <!-- <div class="totalDataGridPanel">
                            <DxDataGrid
                                id="gridContainer"
                                :data-source="twinList"
                                :show-borders="false"
                                :height="totalGridHeight"
                                key-expr="dt-id"
                                :ref="dataGridRefKey"
                            >
                            
                                <DxScrolling mode="virtual"/>
                                <DxSorting mode="none"/>
                                <DxSelection
                                    :select-all-mode="'allPages'"
                                    :show-check-boxes-mode="'onClick'"
                                    mode="multiple"
                                />

                                <DxColumn
                                    :width="230"
                                    caption="name"
                                    data-field="name"
                                />
                                <DxColumn
                                    caption="description"
                                    data-field="description"
                                />
                                <DxColumn
                                    caption="dt-id"
                                    data-field="dt-id"
                                />

                                <DxColumn
                                    css-class="buttonTDPanel centerTitle"
                                    :width="60"
                                    caption=""
                                    cell-template="cellTemplate"
                                />
                                
                                <template #cellTemplate="{ data }">
                                    <div class="dataGridButtonPanel">
                                        <div class="buttonPanel jsonView" @click="onJSONViewClick(data)">
                                            <font-awesome-icon icon="fa-solid fa-eye" />
                                        </div>
                                    </div>
                                </template>
                            </DxDataGrid>
                        </div>

                        <div class="addIconPanel"  >
                            <font-awesome-icon icon="fa-solid fa-arrow-alt-circle-down" @click="onAddTwinList" />
                        </div>
                        
                        <div class="selectDataGridPanel">
                            <DxDataGrid
                                id="selectGridContainer"
                                :height="selectGridHeight"
                                :data-source="selectedTwinList"
                                :show-borders="false"
                            >
                                <DxScrolling mode="virtual"/>
                                <DxSorting mode="none"/>

                                <DxColumn
                                    :width="230"
                                    caption="name"
                                    data-field="name"
                                />

                                <DxColumn
                                    caption="dt-id"
                                    data-field="dt-id"
                                />

                                <DxColumn
                                    css-class="buttonTDPanel centerTitle"
                                    :width="120"
                                    caption="relationType"
                                    cell-template="cellTemplate0"
                                />
                                <template #cellTemplate0="{ data }">
                                    <div class="dataGridButtonPanel">
                                        <DxSelectBox
                                            :width="100"
                                            :height="34"
                                            :items="relationTypes"
                                            v-model:value="data.data.relationType"
                                        />
                                    </div>
                                </template>
                                
                                <DxColumn
                                    css-class="buttonTDPanel centerTitle"
                                    :width="60"
                                    caption=""
                                    cell-template="cellTemplate1"
                                />
                                <template #cellTemplate1="{ data }">
                                    <div class="dataGridButtonPanel">
                                        <div class="buttonPanel jsonView" @click="onJSONViewClick(data)">
                                            <font-awesome-icon icon="fa-solid fa-eye" />
                                        </div>
                                    </div>
                                </template>

                                <DxColumn
                                    css-class="buttonTDPanel centerTitle"
                                    :width="60"
                                    caption=""
                                    cell-template="cellTemplate2"
                                />
                                <template #cellTemplate2="{ data }">
                                    <div class="dataGridButtonPanel">
                                        <div class="buttonPanel delete" @click="onDeleteTwinList(data)">
                                            <font-awesome-icon icon="fa-solid fa-trash" />
                                        </div>
                                    </div>
                                </template>
                            </DxDataGrid>
                        </div> -->
                    </div>
                </div>
            </div>
        </DxPopup>

        <JsonViewDialog
        v-if="isJsonViewDialog"
        :propsIsDialog="isJsonViewDialog"
        :propsJSON="jsonViewData"
        @close="closeJsonViewDialog"
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
}

.contentPanel {
    display: flex;
    flex-direction: column;
    height: 100%;
    gap: 20px;
    
    .filterPanel {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        width: 100%;
        background-color: #1F2128;
        border-radius: 10px;
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
    :deep(.dx-tabpanel) {
        height: 100%;
    }

    :deep(.dx-tabpanel > .dx-tabpanel-tabs .dx-tabs) {
        background: #313442;
        border: 1px solid #303440;
        box-shadow: none;
    }

    :deep(.dx-tabs-wrapper) {
        display: flex;
        padding: 10px 20px;
        pointer-events: none;
    }

    :deep(.dx-tabpanel-container) {
        height: calc(100% - 111px);
    }

    :deep(.dx-tabpanel > .dx-tabpanel-tabs .dx-tab) {
        width: 50%;
        background: transparent;
        box-shadow: none;
    }

    :deep(.dx-tabpanel > .dx-tabpanel-tabs .dx-tabs-expanded .dx-tab:not(.dx-tab-selected):first-of-type) {
        box-shadow: none;
    }

    :deep(.dx-tab.dx-tab-selected .titleIndex) {
        background-color: #586dcd;
        color: #dedede;
        border: none;
    }

    :deep(.dx-tab.dx-tab-selected .titleName) {
        color: #dedede;
    }

    :deep(.dx-tab.dx-tab-selected .titleWrapper:before) {
        border-top: 1px solid #dee2e6;
    }

    :deep(.dx-tabpanel > .dx-tabpanel-container > .dx-multiview-wrapper) {
        border: none;
        margin-top: 20px;
    }

    .titleWrapper {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
    }

    .titleWrapper:before {
        content: " ";
        border-top: 1px solid #5a5a5a;
        width: 100%;
        top: 50%;
        left: 0;
        display: block;
        position: absolute;
        margin-top: -1rem;
    }

    .titleIndex {
        height: 24px;
        background: #313442;
        border: 1px solid #5a5a5a;
        width: 24px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: gray;
        z-index: 1;
    }

    .titleName {
        color: #5a5a5a;
        width: 100%;
        /* text-overflow: ellipsis; */
        /* overflow: hidden; */
        white-space: pre-line;
    }


    .dataGridPanel {
        width: 100%;
        height: calc(100% - 55px);
        display: flex;
        flex-direction: column;
        gap: 20px;
    }

    .totalDataGridPanel {
        width: 100%;
        height: 100%;
     
        #gridContainer {
            height: 100%;
        }
    }

   

    .addIconPanel {
        display: flex;
        justify-content: center;

        svg {
            font-size: 24px;
            cursor: pointer;
        }
    }

    .selectDataGridPanel {
        width: 100%;
        height: calc(50% - 32px);

        #selectGridContainer {
            height: 100%;
        }
    }

    :deep(.buttonTDPanel) {
        padding: 0px;
        vertical-align: middle !important;
    }

    :deep(.dx-datagrid-headers .centerTitle) {
        text-align: center !important;
    }

    .dataGridButtonPanel {
        display: flex;
        justify-content: center;
        height: 100%;

        .buttonPanel {
            display: flex;
            justify-content: center;
            width: 23px;
            height: 23px;
            border-radius: 2px;
            align-items: center;
            cursor: pointer;
        }

        .buttonPanel.jsonView {
            background: #0070C0;
        }

        .buttonPanel.delete {
            background: #DE393B;
        }
    }
}

.footerPanel {
  display: flex;
  flex-direction: row;
  gap: 10px;
  justify-content: flex-end;
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

    .filterPanel {
        padding: 0px !important;
    }

    :deep(.dx-tabs-wrapper) {
        padding: 5px 10px !important;
    }
}

@media screen and (max-width: 420px) {
    .textBoxPanel {
      min-width: 100% !important;
    }
}
</style>
