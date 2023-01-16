<script>
import moment from "moment";

export default {
  name: "DataMonitoringDialog",
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
      mindsphere: undefined,
      isConnect: false,
      schedularJobId: "",
      positionChartOption: {
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
          right: 180,
          bottom: 20,
          containLabel: true,
        },
        legend: {
          type: "scroll",
          orient: "vertical",
          top: 10,
          right: 0,
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
      }
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
      this.mindsphere = this.twin.features.find(d => d.name == 'mindsphere');
    }
  },
  methods: {
    unLoadEvent() {
      this.disconnect();
    },
    connect() {
      if (!this.mindsphere) {
        this.emitter.emit("showConfirmDialog", { message: "TWIN에 Features 정보가 없습니다.", isConfirm: false });
        return;
      }

      this.emitter.emit("setLoading", true);

      fetch("/api/twin/connect", {
            method: "POST",
            headers: {
            "Content-Type": "application/json",
            },
            body: JSON.stringify(this.mindsphere),
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
    disconnect() {
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
    onWebSocketMessage(websocketData) {
      if (this.isConnect && this.schedularJobId) {
        let websocketDataMap = JSON.parse(websocketData);
        if (websocketDataMap["schedularJobId"] && websocketDataMap["data"]) {
          let jobId = websocketDataMap["schedularJobId"];
          if (this.schedularJobId == jobId) {
            let datas = JSON.parse(websocketDataMap["data"]);
            datas.forEach(data => {
              // this.positionChartOption.xAxis.data.push(moment(new Date(data["_time"])).format("HH:mm:ss"));

              let keys = Object.keys(data).filter(d => d != "_time");
              keys.forEach(key => {
                let seriesItem = this.positionChartOption.series.find(d => d.name == key);
                if (!seriesItem) {
                  seriesItem = {
                    name: key,
                    type: "line",
                    data: [],
                    showSymbol: false
                  }

                  this.positionChartOption.series.push(seriesItem);
                }

                seriesItem.data.push(data[key]);

                if (seriesItem.data.length > 100) {
                  seriesItem.data.shift();
                }
              });
            })
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
      :title="'Realtime Data Monitoring ( ' + twin['name'] + ' )'"
      container="body"
      @hiding="onHiding($event)"
      @hidden="onHidden($event)"
    >
      <div class="dialogPanel">
        <div class="contentPanel">
          <v-echart
            class="chart"
            ref="positionChart"
            :option="positionChartOption"
            autoresize
          />
        </div>

        <div class="footerPanel">
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

    #chart {
        height: 100%;
    }
}

.footerPanel {
  display: flex;
  flex-direction: row;
  gap: 10px;
  justify-content: flex-end;

  .connectButtonPanel {
    width: 200px;
    text-align: right;
  }
}
</style>
