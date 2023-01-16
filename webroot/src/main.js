import { createApp } from "vue";
import App from "./App.vue";
import LoadingPage from "@/components/loading/LoadingPage.vue";

const app = createApp(App);
const loadingPage = createApp(LoadingPage);

/**======================== [mitt] ========================*/
import mitt from "mitt";
const emitter = mitt();
app.config.globalProperties.emitter = emitter;
app.config.globalProperties.uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
  var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
  return v.toString(16);
});
loadingPage.config.globalProperties.emitter = emitter;
/**======================== [mitt] ========================*/

/**======================== [primeng] ========================*/
import PrimeVue from "primevue/config";
import "primevue/resources/themes/lara-light-teal/theme.css";
import "primevue/resources/primevue.min.css";
import "primeicons/primeicons.css";
// import "primevue/core/core.min.js";
app.use(PrimeVue);

import Button from "primevue/button";
app.component("P-Button", Button);
import InputText from "primevue/inputtext";
app.component("P-InputText", InputText);
import InputNumber from "primevue/inputnumber";
app.component("P-InputNumber", InputNumber);
import Password from "primevue/password";
app.component("P-Password", Password);
import Textarea from "primevue/textarea";
app.component("P-Textarea", Textarea);
import DataTable from "primevue/datatable";
app.component("P-DataTable", DataTable);
import Column from "primevue/column";
app.component("P-Column", Column);
import Paginator from "primevue/paginator";
app.component("P-Paginator", Paginator);
import Dialog from "primevue/dialog";
app.component("P-Dialog", Dialog);
import Tree from "primevue/tree";
app.component("P-Tree", Tree);
import TabView from "primevue/tabview";
app.component("P-TabView", TabView);
import TabPanel from "primevue/tabpanel";
app.component("P-TabPanel", TabPanel);
import Calendar from "primevue/calendar";
app.component("P-Calendar", Calendar);
import Dropdown from "primevue/dropdown";
app.component("P-Dropdown", Dropdown);
import Image from "primevue/image";
app.component("P-Image", Image);
import Checkbox from "primevue/checkbox";
app.component("P-Checkbox", Checkbox);
/**======================== [primeng] ========================*/

/**======================== [devextreme] ========================*/
import DxButton from 'devextreme-vue/button';
app.component("DxButton", DxButton);
import DxTextBox from 'devextreme-vue/text-box';
app.component("DxTextBox", DxTextBox);
import DxTextArea from 'devextreme-vue/text-area';
app.component("DxTextArea", DxTextArea);
import DxDrawer from 'devextreme-vue/drawer';
app.component("DxDrawer", DxDrawer);
import { DxList } from 'devextreme-vue/list';
app.component("DxList", DxList);
import DxTagBox from 'devextreme-vue/tag-box';
app.component("DxTagBox", DxTagBox);
import DxSelectBox from 'devextreme-vue/select-box';
app.component("DxSelectBox", DxSelectBox);
import { DxPopup, DxPosition, DxToolbarItem } from 'devextreme-vue/popup';
app.component("DxPopup", DxPopup);
app.component("DxPosition", DxPosition);
import { DxFileUploader } from 'devextreme-vue/file-uploader';
app.component("DxFileUploader", DxFileUploader);
import {
  DxDataGrid, DxScrolling, DxSorting, DxLoadPanel, DxColumn, DxSelection
} from 'devextreme-vue/data-grid';
app.component("DxDataGrid", DxDataGrid);
app.component("DxScrolling", DxScrolling);
app.component("DxSorting", DxSorting);
app.component("DxLoadPanel", DxLoadPanel);
app.component("DxColumn", DxColumn);
app.component("DxSelection", DxSelection);
import { DxScrollView } from 'devextreme-vue/scroll-view';
app.component("DxScrollView", DxScrollView);
import {
  DxValidator,DxRequiredRule,
} from 'devextreme-vue/validator';
app.component("DxValidator", DxValidator);
app.component("DxRequiredRule", DxRequiredRule);
import {
  DxChart, DxSeries, DxArgumentAxis, DxCommonSeriesSettings, DxExport, DxLegend, DxMargin
} from 'devextreme-vue/chart';
app.component("DxChart", DxChart);
app.component("DxSeries", DxSeries);
app.component("DxArgumentAxis", DxArgumentAxis);
app.component("DxCommonSeriesSettings", DxCommonSeriesSettings);
app.component("DxExport", DxExport);
app.component("DxLegend", DxLegend);
app.component("DxMargin", DxMargin);
import DxToolbar, { DxItem } from 'devextreme-vue/toolbar';
app.component("DxToolbar", DxToolbar);
app.component("DxItem", DxItem);
import { DxCheckBox } from 'devextreme-vue/check-box';
app.component("DxCheckBox", DxCheckBox);
import DxContextMenu from 'devextreme-vue/context-menu';
app.component("DxContextMenu", DxContextMenu);
import DxTabs from 'devextreme-vue/tabs';
app.component("DxTabs", DxTabs);
import DxTabPanel from 'devextreme-vue/tab-panel';
app.component("DxTabPanel", DxTabPanel);
/**======================== [devextreme] ========================*/

/**======================== [monaco-editor] ========================*/
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker';
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker';
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';

self.MonacoEnvironment = {
  getWorker(_, label) {
    if (label === 'json') {
      return new jsonWorker();
    }
    if (label === 'css' || label === 'scss' || label === 'less') {
      return new cssWorker();
    }
    if (label === 'html' || label === 'handlebars' || label === 'razor') {
      return new htmlWorker();
    }
    if (label === 'typescript' || label === 'javascript') {
      return new tsWorker();
    }
    return new editorWorker();
  },
};

import MonacoEditor from 'monaco-editor-vue3';
app.component("MonacoEditor", MonacoEditor);
/**======================== [monaco-editor] ========================*/

/**======================== [font awesome icon] ========================*/
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
app.component("font-awesome-icon", FontAwesomeIcon);

import { library } from "@fortawesome/fontawesome-svg-core";
import {
  faBars,
  faArrowsToEye,
  faBezierCurve,
  faArrowRightFromBracket,
  faUserLarge,
  faHome,
  faCaretLeft,
  faCaretRight,
  faList,
  faProjectDiagram,
  faPlusCircle,
  faTabletAlt,
  faCog,
  faShareSquare,
  faFileUpload,
  faArrowAltCircleDown,
  faEye,
  faTrash,
  faNetworkWired,
  faTimes,
  faDownload,
  faCheckCircle,
  faExclamationCircle,
  faSearch,
  faSearchPlus,
  faSearchMinus
} from "@fortawesome/free-solid-svg-icons";
library.add(faBars);
library.add(faArrowsToEye);
library.add(faBezierCurve);
library.add(faArrowRightFromBracket);
library.add(faUserLarge);
library.add(faHome);
library.add(faCaretLeft);
library.add(faCaretRight);
library.add(faList);
library.add(faProjectDiagram);
library.add(faPlusCircle);
library.add(faTabletAlt);
library.add(faCog);
library.add(faShareSquare);
library.add(faFileUpload);
library.add(faArrowAltCircleDown);
library.add(faEye);
library.add(faTrash);
library.add(faNetworkWired);
library.add(faTimes);
library.add(faDownload);
library.add(faCheckCircle);
library.add(faExclamationCircle);
library.add(faSearch);
library.add(faSearchPlus);
library.add(faSearchMinus);

import {
  faCopyright,
  faCaretSquareLeft,
  faCaretSquareRight
} from "@fortawesome/free-regular-svg-icons";
library.add(faCopyright);
library.add(faCaretSquareLeft);
library.add(faCaretSquareRight);

/**======================== [font awesome icon] ========================*/

/**======================== [EChart] ========================*/
import * as echarts from "echarts";
app.config.globalProperties.echarts = echarts;

import ECharts from "vue-echarts";
app.component("v-echart", ECharts);
/**======================== [EChart] ========================*/

/**======================== [moment] ========================*/
// import VueMoment from "vue-moment";
// app.use(require('vue-moment'))
// app.use(VueMoment);
/**======================== [moment] ========================*/

import VNetworkGraph from "v-network-graph";
import "v-network-graph/lib/style.css";
app.use(VNetworkGraph);



/**======================== [component] ========================*/
import router from "./router";
app.use(router);

import store from "./store";
app.use(store);
/**======================== [component] ========================*/

app.mount("#app");
loadingPage.mount("#loadingPage");
