<script>
import JsonViewDialog from "@/views/jsonView/JsonViewDialog.vue";
const dataGridRefKey = 'my-data-grid';

export default {
  name: "AddTwinDialog",
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
      allTwinList: [
      {
            "dt-id": "https://dtid.org/19096ff3-8c0e-4d16-82d4-d0220f33709b",
            "hosting-iri": "https://dtw.twinbase.org/19096ff3-8c0e-4d16-82d4-d0220f33709b",
            "name": "Drill",
            "description": "Some random drill located in the AIIC lab at K3",
            "baseurl": "https://dtw.twinbase.org",
            "edit": "https://github.com/juusoautiosalo/twinbase-demo/edit/main/docs/19096ff3-8c0e-4d16-82d4-d0220f33709b/index.yaml",
            "relations": [
                {
                    "dt-id": "http://dtid.org/3091b292-6104-4ef3-a30f-8275b46b5ab4",
                    "relationType": "parent"
                },
                {
                    "dt-id": "https://dtid.org/81ca4ef8-0125-4b27-8f44-3a1587e8bd57",
                    "relationType": "parent"
                }
            ]
        },
        {
            "version": "1.0",
            "privacy": "public",
            "dt-id": "http://dtid.org/3091b292-6104-4ef3-a30f-8275b46b5ab4",
            "hosting-iri": "https://dtw.twinbase.org/lab-room-K3-143",
            "name": "AIIC mechatronics hall",
            "description": "A laboratory room located at Aalto University.",
            "owner": {
                "name": "Aalto-yliopistos\u00e4\u00e4ti\u00f6",
                "website": "https://www.aalto.fi"
            },
            "contact": {
                "name": "Juuso Autiosalo",
                "website": "https://people.aalto.fi/juuso.autiosalo"
            },
            "location": {
                "streetAddress": "Otaniemi",
                "gpsCoordinates": "60.1841\u00b0 N, 24.8301\u00b0 E"
            },
            "relations": [
                {
                    "dt-id": "https://dtid.org/81ca4ef8-0125-4b27-8f44-3a1587e8bd57",
                    "relationType": "parent"
                },
                {
                    "dt-id": "http://dtid.org/6a303315-d9e5-48ef-a1da-d37bcaa10c57",
                    "relationType": "child"
                },
                {
                    "dt-id": "https://dtid.org/19096ff3-8c0e-4d16-82d4-d0220f33709b",
                    "relationType": "child"
                }
            ],
            "edit": "https://github.com/juusoautiosalo/twinbase-demo/edit/main/docs/lab-room-K3-143/index.yaml",
            "baseurl": "https://dtw.twinbase.org"
        },
        {
            "dt-id": "https://dtid.org/81ca4ef8-0125-4b27-8f44-3a1587e8bd57",
            "hosting-iri": "https://dtw.twinbase.org/81ca4ef8-0125-4b27-8f44-3a1587e8bd57",
            "name": "K3 building",
            "description": "The Aalto University building \"K3\" used by Mechanical Engineering.",
            "baseurl": "https://dtw.twinbase.org",
            "edit": "https://github.com/juusoautiosalo/twinbase-demo/edit/main/docs/81ca4ef8-0125-4b27-8f44-3a1587e8bd57/index.yaml",
            "relations": [
                {
                    "dt-id": "https://dtid.org/497ff305-3444-487a-a8a9-5b8ff21071b4",
                    "relationType": "parent"
                },
                {
                    "dt-id": "http://dtid.org/3091b292-6104-4ef3-a30f-8275b46b5ab4",
                    "relationType": "child"
                },
                {
                    "dt-id": "https://dtid.org/19096ff3-8c0e-4d16-82d4-d0220f33709b",
                    "relationType": "child"
                }
            ]
        },
        {
            "dt-id": "https://dtid.org/497ff305-3444-487a-a8a9-5b8ff21071b4",
            "hosting-iri": "https://dtw.twinbase.org/497ff305-3444-487a-a8a9-5b8ff21071b4",
            "name": "Puumiehenkuja",
            "description": "A street located in Otaniemi, Espoo, Finland.",
            "baseurl": "https://dtw.twinbase.org",
            "edit": "https://github.com/juusoautiosalo/twinbase-demo/edit/main/docs/497ff305-3444-487a-a8a9-5b8ff21071b4/index.yaml",
            "relations": [
                {
                    "dt-id": "https://dtid.org/81ca4ef8-0125-4b27-8f44-3a1587e8bd57",
                    "relationType": "child"
                }
            ]
        },
        {
            "version": "1.0",
            "privacy": "public",
            "dt-id": "http://dtid.org/488cccff-6902-41a9-9130-24ba2e62fbd8",
            "hosting-iri": "https://dtw.twinbase.org/juuso",
            "name": "Juuso Autiosalo",
            "description": "Juuso is working as a doctoral candidate at Aalto University.",
            "contact": {
                "website": "https://people.aalto.fi/juuso.autiosalo"
            },
            "location": {
                "streetAddress": "S\u00f6rn\u00e4inen, Helsinki, Finland"
            },
            "features": [
                {
                    "name": "Personal website",
                    "address": "https://me.juu.so"
                },
                {
                    "name": "LinkedIn profile",
                    "description": "Juuso occasionally uses LinkedIn.",
                    "address": "https://www.linkedin.com/in/juuso-autiosalo"
                },
                {
                    "name": "Another website",
                    "description": "Some old website made by Juuso, hosted by Aalto.",
                    "address": "https://users.aalto.fi/~autiosj1/"
                },
                {
                    "name": "GiHub profile",
                    "description": "Juuso's GitHub profile",
                    "address": "https://github.com/juusoautiosalo"
                }
            ],
            "edit": "https://github.com/juusoautiosalo/twinbase-demo/edit/main/docs/juuso/index.yaml",
            "baseurl": "https://dtw.twinbase.org"
        },
        {
            "dt-id": "https://dtid.org/1bbfab79-2890-4bcf-97fa-9dffb99c1759",
            "hosting-iri": "https://dtw.twinbase.org/1bbfab79-2890-4bcf-97fa-9dffb99c1759",
            "name": "Twin for research seminar",
            "description": "This twin can be modified on the research seminar.",
            "baseurl": "https://dtw.twinbase.org",
            "edit": "https://github.com/juusoautiosalo/twinbase-demo/edit/main/docs/1bbfab79-2890-4bcf-97fa-9dffb99c1759/index.yaml",
            "relations": {
                "parent": "https://d-t.fi/juuso"
            }
        },
        {
            "version": "1.0",
            "privacy": "public",
            "dt-id": "http://dtid.org/6a303315-d9e5-48ef-a1da-d37bcaa10c57",
            "hosting-iri": "https://dtw.twinbase.org/crane-ilmatar",
            "name": "Ilmatar crane",
            "description": "Ilmatar is an overhead crane located at Aalto University.",
            "owner": {
                "name": "Aalto-yliopistos\u00e4\u00e4ti\u00f6",
                "website": "https://www.aalto.fi"
            },
            "contact": {
                "name": "Juuso Autiosalo",
                "website": "https://people.aalto.fi/juuso.autiosalo"
            },
            "location": {
                "streetAddress": "Otaniemi",
                "gpsCoordinates": "60.1841\u00b0 N, 24.8301\u00b0 E"
            },
            "manufacturer": {
                "name": "Konecranes",
                "website": "https://www.konecranes.com"
            },
            "relations": [
                {
                    "dt-id": "http://dtid.org/3091b292-6104-4ef3-a30f-8275b46b5ab4",
                    "relationType": "parent"
                },
                {
                    "dt-id": "https://dtid.org/329cd047-09b1-419a-b389-b10615bb7211",
                    "relationType": "child"
                },
                {
                    "dt-id": "https://dtid.org/2358055c-4712-4b53-a0c6-fa8f2b90719f",
                    "relationType": "child"
                },
                {
                    "dt-id": "https://dtid.org/2bf2ee33-1297-4c30-afac-d80411f3f617",
                    "relationType": "child"
                }
            ],
            "features": [
                {
                    "name": "OSEMA",
                    "description": "OSEMA allows managing retrofitted sensors attached to the crane.",
                    "address": "https://example.sensor.fi/sensors/browse",
                    "apiAddress": "https://digi.kaksonen.fi/api/v1.0/",
                    "requirement": "User account is needed.",
                    "documentation": "https://github.com/AaltoIIC/OSEMA/blob/master/Documentation.md"
                },
                {
                    "name": "MindSphere",
                    "description": "MindSphere stores historical data of the crane",
                    "address": "https://crane-fleetmanager.eu1.mindsphere.io/",
                    "requirement": "User account for the aiicd1 tenant is required.",
                    "documentation": "http://mindsphere.io"
                },
                {
                    "name": "OPC UA",
                    "description": "OPC UA server allows real-time monitoring and control of the crane",
                    "requirement": "Access to Ilmatar local network, details available on request."
                },
                {
                    "name": "GraphQL Interface",
                    "description": "This interface allows accessing OPC UA using GraphQL",
                    "requirement": "Access to Ilmatar local network, details available on request.",
                    "address": "192.168.0.77",
                    "documentation": "https://github.com/AaltoIIC/OPC-UA-GraphQL-Wrapper"
                },
                {
                    "name": "Teamcenter",
                    "description": "Teamcenter stores PLM data of the crane. Includes 3D model of the crane without business criticial parts",
                    "requirement": "Access to Aalto Teamcenter.",
                    "documentation": "teamcenter.com"
                },
                {
                    "name": "Ilmatar OIE",
                    "description": "The website of Ilmatar OIE provides human readable information about the crane.",
                    "address": "https://aalto.fi/ilmatar"
                }
            ],
            "edit": "https://github.com/juusoautiosalo/twinbase-demo/edit/main/docs/crane-ilmatar/index.yaml",
            "baseurl": "https://dtw.twinbase.org"
        }
      ],
      twinList: [],
      dataGridRefKey,
      isJsonViewDialog: false,
      jsonViewData: "",
      returnData: {}
    };
  },
  computed: {
    dataGrid: function() {
        return this.$refs[dataGridRefKey].instance;
    }
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.twinList = Object.assign([], this.allTwinList);
  },
  methods: {
    create() {
        let selectedTwinList = this.dataGrid.getSelectedRowsData();
        if (selectedTwinList.length == 0) {
            this.emitter.emit("showConfirmDialog", {
              message: "추가할 Twin을 선택하세요.",
              isConfirm: false
            });

            return;
        }

        this.returnData = selectedTwinList[0];
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
    onJSONViewClick(data) {
        this.jsonViewData = JSON.stringify(data.data, null, 2);
        this.isJsonViewDialog = true;
    },
    closeJsonViewDialog() {
        this.jsonViewData = "";
        this.isJsonViewDialog = false;
    }
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
      :width="1000"
      height="80%"
      title="Add Twin"
      container="body"
      @hidden="onHidden($event)"
    >
      <div class="dialogPanel">
        <div class="contentPanel">
            <DxScrollView
                id="scrollview"
                ref="scrollViewWidget"
                :scroll-by-content="false"
                :scroll-by-thumb="true"
                :show-scrollbar="always"
                :bounce-enabled="false"
                reach-bottom-text="Updating..."
            >
                <div class="scrollContentPanel">
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
                                class="purple"
                                type="normal"
                                text="Search Twins"
                                @click="onSearchClick($event)"
                            />
                        </div>
                    </div>

                    <div class="totalDataGridPanel">
                        <DxDataGrid
                            id="gridContainer"
                            :data-source="twinList"
                            :show-borders="false"
                            key-expr="dt-id"
                            :ref="dataGridRefKey"
                        >
                            <DxScrolling mode="virtual"/>
                            <DxSorting mode="none"/>
                            <DxSelection
                                select-all-mode="allPages"
                                :show-check-boxes-mode="onClick"
                                mode="single"
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
                                :width="230"
                                caption="dt-id"
                                data-field="dt-id"
                            />
                            <DxColumn
                                :width="90"
                                caption="version"
                                data-field="version"
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
                </div>
            </DxScrollView>
            
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
    </DxPopup>

    <JsonViewDialog
      v-if="isJsonViewDialog"
      :propsIsDialog="isJsonViewDialog"
      :propsJSON="jsonViewData"
      @close="closeJsonViewDialog"
    />
   
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
    height: calc(100% - 55px);

    .scrollContentPanel {
        display: flex;
        flex-direction: column;
        gap: 20px;

        .filterPanel {
            display: flex;
            flex-direction: row;
            width: 100%;
            height: 35px;
            background-color: #1F2128;
            border-radius: 10px;

            .textBoxPanel {
                width: 100%;
                height: 100%;
            }

            .tagBoxPanel {
                width: 180px;
                margin-left: 20px;
            }

            .buttonPanel {
                margin-left: 20px;
            }
        }

        .totalDataGridPanel {
            display: flex;
            width: 100%;
            flex: 1 1 0%;
        }

        :deep(.buttonTDPanel) {
            padding: 0px;
            vertical-align: middle;
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
    
}

.footerPanel {
  display: flex;
  flex-direction: row;
  gap: 10px;
  justify-content: flex-end;
}
</style>
