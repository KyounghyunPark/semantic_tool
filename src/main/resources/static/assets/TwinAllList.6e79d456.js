import{J as b}from"./JsonViewDialog.d6519c3d.js";import{_ as A,p as S,b as k,c as t,r as h,o as r,d as c,F as m,t as p,e as d,f as x,i as C,k as D,h as g,g as T}from"./index.5ded1198.js";const I={name:"TwinAllList",components:{JsonViewDialog:b},data(){return{topPanelHeight:0,filterPanelHeight:0,filterString:"",filterSelectItems:["All Fields","dt-id","name","description","nationality"],filterSelected:"All Fields",allTwinList:[],twinList:[],isJsonViewDialog:!1,jsonViewData:""}},mounted(){window.addEventListener("resize",this.onWindowsResize),this.emitter.on("twinsChanged",this.getTwinList),this.onWindowsResize(),this.getTwinList()},beforeUnmount(){window.removeEventListener("resize",this.onWindowsResize),this.emitter.off("twinsChanged")},methods:{onWindowsResize(e){setTimeout(()=>{this.$refs.twinTopPanel?this.topPanelHeight=this.$refs.twinTopPanel.clientHeight>0?this.$refs.twinTopPanel.clientHeight+30:0:this.topPanelHeight=0,this.$refs.twinFilterPanel?this.filterPanelHeight=this.$refs.twinFilterPanel.clientHeight+30:this.filterPanelHeight=0},100)},getTwinList(){fetch("/api/twin/getList",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({})}).then(e=>e.json()).then(e=>{this.emitter.emit("setLoading",!1),e.succeeded?(this.allTwinList=e.data,this.onSearchClick()):this.emitter.emit("showErrorDialog",e.data)}).catch(e=>{this.emitter.emit("setLoading",!1),console.error(e),this.emitter.emit("showErrorDialog",e)})},isRelationExist(e,i){let a=e.relations;if(a&&Array.isArray(a))return a.filter(n=>n.relationType==i).length>0},onSearchClick(){if(this.filterSelected=="All Fields"){let e=[];this.allTwinList.forEach(i=>{Object.keys(i).forEach(n=>{typeof i[n]=="string"&&i[n].toLowerCase().includes(this.filterString.toLowerCase())&&e.indexOf(i)==-1&&e.push(i)})}),this.twinList=e}else this.twinList=this.allTwinList.filter(e=>e[this.filterSelected]&&e[this.filterSelected].toLowerCase().includes(this.filterString.toLowerCase()))},onJSONViewClick(e){this.jsonViewData=JSON.stringify(e,null,2),this.isJsonViewDialog=!0},onGraphClick(e){if(e&&e["dt-id"]){let i=e["dt-id"].split("/"),a=i[i.length-1];this.emitter.emit("changeMenu","detailInformation/"+a)}},closeJsonViewDialog(){this.jsonViewData="",this.isJsonViewDialog=!1},onDeleteTwin(e){this.emitter.emit("showConfirmDialog",{message:"\uC120\uD0DD\uD55C TWIN\uC744 \uC0AD\uC81C\uD558\uC2DC\uACA0\uC2B5\uB2C8\uAE4C?",isConfirm:!0,callback:i=>{if(i){this.emitter.emit("setLoading",!0);let a={"dt-id":e["dt-id"],uuid:this.uuid};fetch("/api/twin/delete",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(a)}).then(n=>n.json()).then(n=>{this.emitter.emit("setLoading",!1),n.succeeded?this.getTwinList():this.emitter.emit("showErrorDialog",n.data)}).catch(n=>{this.emitter.emit("setLoading",!1),console.error(n),this.emitter.emit("showErrorDialog",n)})}}})}}},P=e=>(S("data-v-2e9dd7e0"),e=e(),k(),e),V={id:"twinAllList"},J={class:"topPanel",ref:"twinTopPanel"},B={class:"totalCount"},E=P(()=>t("div",{class:"subTitle"}," ( Enter Filter values ) ",-1)),z={class:"filterPanel",ref:"twinFilterPanel"},O={class:"textBoxPanel"},j={class:"tagBoxPanel"},H={class:"buttonPanel"},N={class:"dx-widget dx-button dx-button-mode-contained dx-button-normal dx-button-has-text dx-button-template-wrapper purple"},F=P(()=>t("div",{class:"title"}," Twin List ",-1)),R={class:"contentPanel"},W={style:{display:"flex",width:"100%",height:"100%"}},U={class:"gridPanel"},G={class:"itemPanel"},K={class:"nameInfoPanel"},M=["src"],q=["src"],Q={class:"namePanel"},X={class:"subInfoPanel"},Y={class:"rowPanel"},Z={class:"halfPanel"},$={class:"halfPanel right"},ee={class:"rowPanel"},te=["title"],ie={class:"relationPanel"},le={key:0,class:"parentPanel"},ne={key:1,class:"childPanel"},se={class:"buttonPanel"};function ae(e,i,a,n,s,o){const u=h("DxTextBox"),v=h("DxSelectBox"),f=h("DxButton"),_=h("font-awesome-icon"),L=h("DxScrollView"),y=h("JsonViewDialog");return r(),c(m,null,[t("div",V,[t("div",J,[t("div",B," There are "+p(s.allTwinList.length)+" Digital Twins ",1),E],512),t("div",z,[t("div",O,[d(u,{"show-clear-button":!0,mode:"search",value:s.filterString,"onUpdate:value":i[0]||(i[0]=l=>s.filterString=l),onEnterKey:o.onSearchClick},null,8,["value","onEnterKey"])]),t("div",j,[d(v,{items:s.filterSelectItems,value:s.filterSelected,"onUpdate:value":i[1]||(i[1]=l=>s.filterSelected=l)},null,8,["items","value"])]),t("div",H,[d(f,{class:"purple text-button",type:"normal",text:"Search Twins",height:"100%",onClick:i[2]||(i[2]=l=>o.onSearchClick())}),d(f,{class:"purple icon-button",template:"icon-button-template",width:"40",height:"100%",onClick:i[3]||(i[3]=l=>o.onSearchClick())},{"icon-button-template":x(()=>[t("div",N,[d(_,{icon:"fa-solid fa-search"})])]),_:1})])],512),t("div",{class:"listPanel",style:C({height:`calc(100% - ${s.topPanelHeight}px - ${s.filterPanelHeight}px)`})},[F,t("div",R,[d(L,{id:"scrollview",ref:"scrollViewWidget","scroll-by-content":!1,"scroll-by-thumb":!0,"show-scrollbar":"always","bounce-enabled":!1,"reach-bottom-text":"Updating..."},{default:x(()=>[t("div",W,[t("div",U,[(r(!0),c(m,null,D(s.twinList,l=>(r(),c("div",G,[t("div",K,[l.nationality?(r(),c("img",{key:0,src:`/assets/images/country/48/${l.nationality}.png`},null,8,M)):g("",!0),l.nationality?g("",!0):(r(),c("img",{key:1,src:"assets/images/country/48/WORLD.png"},null,8,q)),t("div",Q,p(l.name),1)]),t("div",X,[t("div",Y,[t("div",Z," version: "+p(l.version),1),t("div",$," privacy: "+p(l.privacy),1)]),t("div",ee,p(l["dt-id"]),1),t("div",{class:"description",title:l.description},p(l.description),9,te)]),t("div",ie,[o.isRelationExist(l,"parent")?(r(),c("div",le,"P")):g("",!0),o.isRelationExist(l,"child")?(r(),c("div",ne,"C")):g("",!0)]),t("div",se,[d(f,{class:"blue",type:"normal",text:"Information",width:"100",height:"30",onClick:w=>o.onJSONViewClick(l)},null,8,["onClick"]),d(f,{class:"orange",type:"normal",text:"Graph",width:"100",height:"30",onClick:w=>o.onGraphClick(l)},null,8,["onClick"]),d(f,{class:"red",type:"normal",text:"Delete",width:"100",height:"30",onClick:w=>o.onDeleteTwin(l)},null,8,["onClick"])])]))),256))])])]),_:1},512)])],4)]),s.isJsonViewDialog?(r(),T(y,{key:0,propsIsDialog:s.isJsonViewDialog,propsJSON:s.jsonViewData,onClose:o.closeJsonViewDialog},null,8,["propsIsDialog","propsJSON","onClose"])):g("",!0)],64)}var re=A(I,[["render",ae],["__scopeId","data-v-2e9dd7e0"]]);export{re as default};
