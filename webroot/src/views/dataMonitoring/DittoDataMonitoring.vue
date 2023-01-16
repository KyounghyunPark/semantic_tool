<script>
import moment from "moment";

export default {
  name: "DittoDataMonitoringDialog",
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
      ditto: undefined,
      topics: undefined,
      selectedTopic: [],
      properteis1: [],
      selectedProperties1: undefined,
      properteis2: [],
      selectedProperties2: undefined,
      properteis3: [],
      selectedProperties3: undefined,
      properteis4: [],
      selectedProperties4: undefined,
      isConnect: false,
      schedularJobId: "",
      isNumberChart: false,
      isBooleanChart: false,
      isStringTable: false,
      numberChartSeries: [],
      numberChartOption: {
        tooltip: {
          trigger: "axis",
          order: 'valueDesc',
          axisPointer: {
            type: "shadow",
          },
        },
        grid: {
          show: false,
          left: 20,
          top: 20,
          right: 20,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          type: "scroll",
          orient: "horizontal",
          bottom: 0,
          textStyle: {
            color: "#ffffff"
          }
        },
        xAxis: {
          type: "category",
          show: false,
          // data: []
        },
        yAxis: {
          type: "value",
          axisLabel: {
            color: "#ffffff"
          }
        },
        // toolbox: {
        //   show: true,
        //   feature: {
        //     dataZoom: {
        //       yAxisIndex: 'none'
        //     },
        //     dataView: { readOnly: false },
        //     magicType: { type: ['line', 'bar'] },
        //     restore: {},
        //     saveAsImage: {}
        //   }
        // },
        series: []
      },
      booleanChartOption: {
        tooltip: {
          trigger: "axis",
          order: 'valueDesc',
          axisPointer: {
            type: "shadow",
          },
        },
        grid: {
          show: false,
          left: 20,
          top: 20,
          right: 20,
          containLabel: true,
        },
        legend: {
          orient: "horizontal",
          bottom: 0,
          textStyle: {
            color: "#ffffff"
          }
        },
        xAxis: {
          type: "category",
          show: false,
          // data: []
        },
        yAxis: {
          type: 'value',
          data: [0, 1],
          min: 0,
          max: 1,
          splitNumber: 1,
          axisLabel: {
            color: "#ffffff",
            formatter: (function(value){
              let label;
              if (value == 1){ 
                label = 'True';
              }
              else {
                label = 'False';
              }
              return label;
            })
          }
        },
        // toolbox: {
        //   show: true,
        //   feature: {
        //     dataZoom: {
        //       yAxisIndex: 'none'
        //     },
        //     dataView: { readOnly: false },
        //     magicType: { type: ['line', 'bar'] },
        //     restore: {},
        //     saveAsImage: {}
        //   }
        // },
        series: []
      },
      stringTableList: [],
      intervals: [
        { key: 1, name: '1 second'},
        { key: 2, name: '2 second'},
        { key: 3, name: '3 second'},
        { key: 4, name: '4 second'},
        { key: 5, name: '5 second'},
        { key: 6, name: '6 second'},
        { key: 7, name: '7 second'},
        { key: 8, name: '8 second'},
        { key: 9, name: '9 second'},
        { key: 10, name: '10 second'}
      ],
      selectedInterval: 2,
      x: 0,
      y: 0,
      z: 0,
      numberCategory: 0,
      booleanCategory: 0
      
    };
  },
  computed: {
  },
  mounted() {
    this.emitter.on("onWebSocketMessage", this.onWebSocketMessage);
    window.addEventListener('beforeunload', this.unLoadEvent);
  },
  beforeUnmount() {
    this.emitter.off("onWebSocketMessage");
    window.removeEventListener('beforeunload', this.unLoadEvent);
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.twin = this.propsTwin;

    if (this.twin.features) {
      this.ditto = this.twin.features.find(d => d.name == 'ditto');
      this.getThingInfo();
    }
  },
  methods: {
    unLoadEvent() {
      this.disconnect();
    },
    getTopic(obj, properties) {
        let isData = false;
        let isObject = false;

        Object.entries(obj).forEach((entry) => {
          if (!(entry[1] instanceof Array) && entry[1] instanceof Object) {
            isObject = true;
          } else {
            if (entry[0] != 'last_update') {
              isData = true;
            }
          }
        });

        if (isData && isObject) {
          let topic = {
              name: "",
              properties: []
          };
          properties.push(topic);
        }

        Object.keys(obj).forEach(key => {
          if (!(obj[key] instanceof Array) && obj[key] instanceof Object) {
                let topic = {
                    name: key,
                    properties: []
                };
                properties.push(topic);

                this.getTopic(obj[key], topic.properties);
            }
        });
    },
    getThingInfo() {
        this.topics = [];
        this.selectedTopic = undefined;
        
        fetch("/api/twin/get-thing", {
            method: "POST",
            headers: {
            "Content-Type": "application/json",
            },
            body: JSON.stringify(this.ditto),
        })
        .then((resp) => resp.json())
        .then((result) => {
        this.emitter.emit("setLoading", false);

        if (result.succeeded && result.data) {
            let thingInfo = JSON.parse(result.data);
            if (thingInfo.features) {
                let features = thingInfo.features;
                
                Object.keys(features).forEach(key => {
                    let topic = {
                        name: key,
                        properties: []
                    }

                    this.getTopic(features[key]["properties"], topic.properties, 0);
                    this.topics.push(topic);
                });

                if (this.topics.length > 0) {
                    this.selectedTopic = this.topics[0];
                }
            }
        } else {
            this.emitter.emit("showConfirmDialog", { message: result.data, isConfirm: false });
        }
        })
        .catch((err) => {
            this.emitter.emit("setLoading", false);

            console.error(err);
            this.emitter.emit("showConfirmDialog", { message: err, isConfirm: false });
        });
    },
    connect() {
      // let getRandomInt = (max) => {
      //   return Math.floor((Math.random() * max) - 2);
      // }

      // setInterval(() => {
      //   this.x += getRandomInt(5);
      //   this.y += getRandomInt(5);
      //   this.z += getRandomInt(5);

      //   let obj = {
      //     last_update: new Date().getTime(),
      //     pose: {
      //       position: {
      //         x: this.x,
      //         y: this.y,
      //         z: this.z
      //       }
      //     }
      //   }

      //   this.setChartData(obj);

      // }, 1000);
      
      // return;

      if (!this.ditto) {
        this.emitter.emit("showConfirmDialog", { message: "TWIN에 Features 정보가 없습니다.", isConfirm: false });
        return;
      }

      if (!this.selectedTopic) {
        this.emitter.emit("showConfirmDialog", { message: "Topic 정보가 없습니다.", isConfirm: false });
        return;
      }

      this.emitter.emit("setLoading", true);

      this.initChartAndTable();

      let param = Object.assign({ "topic" : this.selectedTopic.name, "interval": this.selectedInterval }, this.ditto);

      fetch("/api/twin/connect", {
            method: "POST",
            headers: {
            "Content-Type": "application/json",
            },
            body: JSON.stringify(param),
        })
        .then((resp) => resp.json())
        .then((result) => {
        this.emitter.emit("setLoading", false);

        if (result.succeeded && result.data) {
            this.isConnect = true;
            this.schedularJobId = result.data;
        } else {
            this.emitter.emit("showConfirmDialog", { message: result.data, isConfirm: false });
        }
        })
        .catch((err) => {
            this.emitter.emit("setLoading", false);

            console.error(err);
            this.emitter.emit("showConfirmDialog", { message: err, isConfirm: false });
        });
    },
    disconnect(callback = null) {
      this.emitter.emit("setLoading", true);

      let param = {
        shcedularJobId: this.schedularJobId
      }

      fetch("/api/twin/disconnect", {
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
            this.isConnect = false;
            this.schedularJobId = "";

            if (callback != null) {
                callback(true);
            }
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
    initChartAndTable() {
      if (this.isNumberChart) {
        this.$refs["numberChart"].setOption({}, true);
        this.numberChartOption.series = [];  
      }

      if (this.isBooleanChart) {
        this.$refs["booleanChart"].setOption({}, true);
        this.booleanChartOption.series = [];
      }

      if (this.isStringTable) {
        this.stringTableList = [];
      }

      this.numberCategory = 0;
      this.booleanCategory = 0;

      this.isNumberChart = false;
      this.isBooleanChart = false;
      this.isStringTable = false;
    },

    onTopicChanged() {
      let callback = (isConnect) => {
        this.initChartAndTable();
      
        this.properteis2 = [];
        this.properteis3 = [];
        this.properteis4 = [];

        this.selectedProperties2 = undefined;
        this.selectedProperties3 = undefined;
        this.selectedProperties4 = undefined;

        if (this.selectedTopic) {
          this.properteis1 = this.selectedTopic.properties;
          // setTimeout(() => { this.selectedProperties1 = this.selectedTopic.properties[0]; }, 100);
          this.selectedProperties1 = this.selectedTopic.properties[0];
        } else {
          this.properteis1 = [];
          this.selectedProperties1 = undefined;
        }

        if (isConnect) {
          this.connect();
        }
      }

      if (this.isConnect) {
        this.disconnect(callback);
      } else {
        callback(false);
      }
      
    },
    onProperties1Changed() {
      this.initChartAndTable();

      this.properteis3 = [];
      this.properteis4 = [];

      this.selectedProperties3 = undefined;
      this.selectedProperties4 = undefined;

      if (this.selectedProperties1) {
        this.properteis2 = this.selectedProperties1.properties;
        // setTimeout(() => { this.selectedProperties2 = this.properteis2[0]; }, 100);
        this.selectedProperties2 = this.properteis2[0];
      } else {
        this.properteis2 = [];
        this.selectedProperties2 = undefined;
      }
    },
    onProperties2Changed() {
      this.initChartAndTable();

      this.properteis4 = [];
      this.selectedProperties4 = undefined;

      if (this.selectedProperties2) {
        this.properteis3 = this.selectedProperties2.properties;
        // setTimeout(() => { this.selectedProperties3 = this.properteis3[0]; }, 100);
        this.selectedProperties3 = this.properteis3[0];
      } else {
        this.properteis3 = [];
        this.selectedProperties3 = undefined;
      }
    },
    onProperties3Changed() {
      this.initChartAndTable();

      if (this.selectedProperties2) {
        this.properteis4 = this.selectedProperties3.properties;
        this.selectedProperties4 = this.properteis4[0];
      } else {
        this.properteis4 = [];
        this.selectedProperties4 = undefined;
      }
    },
    onIntervalChanged() {
      let callback = (isConnect) => {
        if (isConnect) {
          this.connect();
        }
      }

      if (this.isConnect) {
        this.disconnect(callback);
      } else {
        callback(false);
      }
    },
    setChartData(obj) {
      let setStringTable = (stringKey, stringValue, isTimestamp = false) => {
        stringValue = isTimestamp ? moment(stringValue).format("YYYY-MM-DD HH:mm:ss") : stringValue;
        let stringTableItem = this.stringTableList.find(d => d.key == stringKey);
        if (!stringTableItem) {
          stringTableItem = {
            key: stringKey,
            value: stringValue
          };

          this.stringTableList.push(stringTableItem);
        } else {
          stringTableItem.value = stringValue;
        }

        this.isStringTable = true;
      }

      if ('last_update' in obj) {
        let key = "last_update";
        let value = obj[key];

        if (typeof(value) == 'string') {
          setStringTable(key, value);
        } else if (typeof(value) == 'number') {
          if (value > new Date('1971-01-01').getTime()) {
            setStringTable(key, value, true);
          }
        }
      }

      let data = obj;
      if (this.selectedProperties1) {
        if (this.selectedProperties1.name != "") {
          data = obj[this.selectedProperties1.name];
        }
        
        if (this.selectedProperties2) {
          if (this.selectedProperties2.name != "") {
            data = data[this.selectedProperties2.name];
          }
          
          if (this.selectedProperties3) {
            if (this.selectedProperties3.name != "") {
              data = data[this.selectedProperties3.name];
            }
            
            if (this.selectedProperties4) {
              if (this.selectedProperties4.name != "") {
                data = data[this.selectedProperties4.name];
              }
            }
          }
        }
      } 

      this.numberCategory++;
      this.booleanCategory++;

      Object.entries(data).forEach(entry => {
          let key = entry[0];
          let value = entry[1];

          if (typeof(value) == 'string') {
            setStringTable(key, value);
          } else if (typeof(value) == 'number') {
            if (value > new Date('1971-01-01').getTime()) {
              setStringTable(key, value, true);
            } else {
              let seriesItem = this.numberChartOption.series.find(d => d.name == key);
              if (!seriesItem) {
                  seriesItem = {
                      name: key,
                      type: "line",
                      data: [],
                      showSymbol: false
                  }

                  this.numberChartOption.series.push(seriesItem);
              }

              if (seriesItem.data.length > 20) {
                  seriesItem.data.shift();
              }

              seriesItem.data.push([this.numberCategory, value]);
              
              this.isNumberChart = true;
            }
          } else if (typeof(value) == 'boolean') {
              let seriesItem = this.booleanChartOption.series.find(d => d.name == key);
              if (!seriesItem) {
                  seriesItem = {
                      name: key,
                      type: "line",
                      data: [],
                      showSymbol: false
                  }

                  this.booleanChartOption.series.push(seriesItem);
              }

              if (seriesItem.data.length > 20) {
                  seriesItem.data.shift();
              }

              seriesItem.data.push([this.booleanCategory, value]);

              this.isBooleanChart = true;
          } else if (value instanceof Array) {
            setStringTable(key, "Array(" + value.length + ")");
          }
        });
    },
    onWebSocketMessage(websocketData) {
      if (this.isConnect && this.schedularJobId) {
        let websocketDataMap = JSON.parse(websocketData);
        if (websocketDataMap["schedularJobId"] && websocketDataMap["data"]) {
          let jobId = websocketDataMap["schedularJobId"];
          if (this.schedularJobId == jobId) {
            let data = JSON.parse(websocketDataMap["data"]);
            let propertiesObj = data["properties"];
            this.setChartData(propertiesObj);
          }
        }
      }
    },
    close() {
      this.isDialog = false;
    },
    onHiding(e) {
      if (this.isConnect) {
        this.disconnect();
      }
      
    },
    onHidden(e) {
      this.$emit("close");
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
      max-width="80%"
      height="80%"
      titleTemplate="titleTemplate"
      container="body"
      @hiding="onHiding($event)"
      @hidden="onHidden($event)"
    >
      <template #titleTemplate>
          <div class="dx-toolbar dx-widget dx-visibility-change-handler dx-collection dx-popup-title dx-has-close-button">
            <div class="dx-toolbar-items-container">
              <div class="dx-toolbar-before">
                <div class="dx-item dx-toolbar-item dx-toolbar-label" style="max-width: 909.65px;">
                  <div class="dx-item-content dx-toolbar-item-content" style="display:flex; flex-direction: row; gap: 30px">
                    <div>Realtime Data Monitoring</div>
                    <div style="font-size: 16px; align-self: end;">
                      [ {{twin['name']}} : {{ditto.thingId}} ]
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="dx-toolbar-after">
                <div class="dx-item dx-toolbar-item dx-toolbar-button">
                  <div class="dx-item-content dx-toolbar-item-content">
                    <div class="dx-widget dx-button dx-button-mode-text dx-button-normal dx-button-has-icon dx-closebutton" @click="close()">
                      <div class="dx-button-content"  >
                        <i class="dx-icon dx-icon-close"></i>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
       </template>
      <div class="dialogPanel">
        <div class="contentPanel">
            <div class="dittoInfoPanel">
                <label >
                    fetures
                </label>

                <DxSelectBox
                    :width="'auto'"
                    :items="topics"
                    v-model:value="selectedTopic"
                    display-expr="name"
                    @value-changed="onTopicChanged"
                />

                <label style="margin-left: 20px;" :class="properteis1.length > 0 ? 'visible' : 'hidden'">
                    properties
                </label>

                <div :class="properteis1.length > 0 ? 'visible' : 'hidden'">
                  <DxSelectBox
                    :width="'auto'"
                    :items="properteis1"
                    v-model:value="selectedProperties1"
                    display-expr="name"
                    @value-changed="onProperties1Changed"
                  />
                </div>
            
                <div :class="properteis2.length > 0 ? 'visible' : 'hidden'">
                  <DxSelectBox
                    :width="'auto'"
                    :items="properteis2"
                    v-model:value="selectedProperties2"
                    display-expr="name"
                    @value-changed="onProperties2Changed"
                  />
                </div>
                
                <div :class="properteis3.length > 0 ? 'visible' : 'hidden'">
                  <DxSelectBox
                    :width="'auto'"
                    :items="properteis3"
                    v-model:value="selectedProperties3"
                    display-expr="name"
                    @value-changed="onProperties3Changed"
                  />
                </div>
            </div>

            <div class="chartPanel">
              <div class="numberChart" v-if="isNumberChart">
                <v-echart
                  class="chart"
                  ref="numberChart"
                  :option="numberChartOption"
                  autoresize
                />
              </div>

              <div class="booleanChart" v-if="isBooleanChart" :class="(isBooleanChart && isNumberChart && isStringTable) ? 'boolean-number-string' : ''">
                <v-echart
                  class="chart"
                  ref="booleanChart"
                  :option="booleanChartOption"
                  autoresize
                />
              </div>

              <div class="stringTable" v-if="isStringTable" :class="(!isBooleanChart && !isNumberChart && isStringTable) ? 'string-only' : ''">
                <DxScrollView
                    id="scrollview"
                    ref="scrollViewWidget"
                    :scroll-by-content="false"
                    :scroll-by-thumb="true"
                    :show-scrollbar="'always'"
                    :bounce-enabled="false"
                    reach-bottom-text="Updating..."
                >
                  <div class="stringTableContent">
                    <div class="tableRow" v-for="(entry, index) in stringTableList">
                      <div class="tableColumn">
                        {{entry.key}}
                      </div>

                      <div class="tableColumn">
                        {{entry.value}}
                      </div>
                    </div>
                  </div>
                </DxScrollView>
               
                  
              </div>
            </div>
        </div>

        <div class="footerPanel">
          <label >
              Monitoring Interval
          </label>

          <DxSelectBox
            :width="120"
            :items="intervals"
            value-expr="key"
            display-expr="name"
            v-model:value="selectedInterval"
            @value-changed="onIntervalChanged"
          />
          <div class="connectButtonPanel">
            <transition name="fade">
              <div v-if="!isConnect">
                <DxButton
                  class="green"
                  type="normal"
                  text="Connect"
                  @click="connect()"
                />
              </div>
            </transition>

            <transition name="fade">
              <div v-if="isConnect">
                <DxButton
                  class="red"
                  type="normal"
                  text="Disconnect"
                  @click="disconnect()"
                />
              </div>
            </transition>
          </div>

          <DxButton
            class="gray"
            type="normal"
            text="Close"
            @click="close()"
          />
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

.contentPanel {
    display: flex;
    flex-direction: column;
    height: calc(100% - 55px);    
    gap: 20px;

    .dittoInfoPanel {
        height: 38px;
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 10px;
    }

    .chartPanel {
        display: flex;
        flex-direction: column;
        height: calc(100% - 58px);
        gap: 20px;

        .numberChart {
          flex: 1 1 auto; 
          border: 1px solid #4d4d4d;
          padding: 10px;
        }

        .booleanChart {
          flex: 1 1 auto; 
          border: 1px solid #4d4d4d;
          padding: 10px;

          &.boolean-number-string {
            height: 10%;
          }
        }

        .stringTable {
          flex: 0 1 auto; 
          // border: 1px solid blue;
          max-height: 20%;

          &.string-only {
            flex: 1 1 auto; 
            max-height: none;
          }

          .stringTableContent {
            display: flex;
            flex-direction: column;
            overflow-y: auto;
          }

          .stringTableContent:first-child {
            border-top: 1px solid #4d4d4d;
          }

          .tableRow {
            display: flex;
            flex-direction: row;
            border-left: 1px solid #4d4d4d;
            border-right: 1px solid #4d4d4d;
            border-bottom: 1px solid #4d4d4d;
          }

          .tableColumn:first-child {
            border-right: 1px solid #4d4d4d;
          }

          .tableColumn {
            width: 50%;
            text-align: center;
            padding: 10px;
          }
        }

        #chart {
          height: 100%;
        }

        
    }

    .visible {
      visibility: visible;
    }

    .hidden {
      visibility: hidden;
    }
}

.footerPanel {
  display: flex;
  flex-direction: row;
  gap: 10px;
  justify-content: flex-end;
  align-items: center;

  .connectButtonPanel {
    margin-left: 20px;
    text-align: right;
  }
}
</style>
