<script>
import CreateTwinDialog from "@/views/createTwin/CreateTwinDialog.vue";
import DataMonitoringDialog from "@/views/dataMonitoring/DataMonitoring.vue";
import DittoDataMonitoringDialog from "@/views/dataMonitoring/DittoDataMonitoring.vue";
import SelectParentTwinDialog from "@/views/selectParentTwin/SelectParentTwin.vue";
import SetDataMonitoringDialog from "@/views/setDataMonitoring/SetDataMonitoringDialog.vue";

import * as d3 from 'd3'
import dagreD3 from "dagre-d3";
import SvgPanZoom from 'svg-pan-zoom';
import Hammer from 'hammerjs'

export default {
    name: "DetailInformation",
    components: {
        CreateTwinDialog,
        DataMonitoringDialog,
        DittoDataMonitoringDialog,
        SelectParentTwinDialog,
        SetDataMonitoringDialog
    },
    data() {
        return {
            tmpJsonData: "",
            jsonData: "",
            options: {
                fontSize: 14,
                automaticLayout: true,
                minimap: {
                    enabled: false
                },
                lineNumbers: "off"
            },
            monaco: undefined,
            editor: undefined,
            allTwinList: [],
            twinList: [],
            nodes: [],
            edges: [],
            selectedNodeCount: 2,
            selectedNodes: [],
            isTotalGraph: false,
            directionSelectedItem: "TB",
            checkTwinList: [],
            isCreateTwinDialog: false,
            isDataMonitoringDisabled: true,
            isDataMonitoringDialog: false,
            isDittoDataMonitoringDialog: false,
            isSetDataMonitoringDialog: false,
            isAddParentRelation: false,
            isAddChildRelation: false,
            directionSelectBoxOptions: {
                width: 160,
                cssClass: 'toolbar-select',
                items:  [
                    {value: 'TB', display: 'Top to Bottom'},
                    {value: 'LR', display: 'Left to Right'}
                ],
                valueExpr: 'value',
                displayExpr: 'display',
                value: "TB",
                onValueChanged: (args) => {
                    this.directionSelectedItem = args.value;
                    this.updateLayout();
                }
            },
            addTwinButtonOptions: {
                icon: 'plus',
                text: 'Create Twin',
                onClick: this.onCreateTwinClick
            },
            addEdgeButtonOptions: {
                icon: 'arrowright',
                text: 'Add Relation',
                onClick: this.onAddRelationClick
            },
            selectionButtonOptions: {
                icon: 'fullscreen',
                text: 'Selection Twins',
                onClick: this.onSelectionClick
            },
            totalGraphButtonOptions: {
                icon: 'repeat',
                text: 'Total Graph View',
                onClick: () => this.setTwinList()
            },
            // saveButtonOptions: {
            //     icon: 'save',
            //     text: 'Save Twins',
            //     onClick: this.onSaveTwinsClick
            // },
            resetButtonOptions: {
                icon: 'refresh',
                text: 'Reset Graph',
                onClick: this.getTwinList
            },
            saveJsonButtonOptions: {
                icon: 'save',
                text: 'Save Twin',
                onClick: this.onSaveTwinClick
            },
            resetJsonButtonOptions: {
                icon: 'refresh',
                text: 'Reset Twin',
                onClick: this.onResetTwinClick
            },
            downloadJsonButtonOptions: {
                icon: 'download',
                text: 'Download Json',
                onClick: this.onDownloadJsonClick
            },
            routeParam: undefined,
            dataMonitoringTwin: undefined,
            isSelectParentTwinDialog: false,
            selectParentTwinList: [],
            virtualLine: undefined,
            svgPanZoom: undefined,
            contextItems: [],
            nodeContextItems: [
                { text: 'Child Graph View', clickEvent: () => this.onChildGraphViewClick() },
                // { text: 'Collapse Child', clickEvent: () => this.onCollapseChild() },
                { text: 'Add Parent Relation', beginGroup: true, clickEvent: () => this.addParentRelation() },
                { text: 'Add Child Relation', clickEvent: () => this.addChildRelation() },
                { text: 'Add Relation', clickEvent: () => this.onAddRelationClick() },
                { text: 'Set Data Monitoring', beginGroup: true,  clickEvent: () => this.onSetDataMonitoringClick() },
                { text: 'Data Monitoring',  clickEvent: () => this.onDataMonitoringClick() },
                { text: 'Delete Twin', beginGroup: true, clickEvent: () => this.deleteTwin() }
            ],
            edgeContextItems: [
                { text: 'Delete Relation', clickEvent: () => this.deleteEdge() },
            ],
            contextType: 'node',
            contextMenuSelectedItem: undefined,
            isContextMenuVisible: false
        };
    },
    mounted() {
        window.addEventListener('keydown', this.onWindowKeydownListener);
        this.emitter.on("twinsChanged", this.getTwinList);

        this.setSvgPanZoom();
        this.selectedNodes = [];
        this.getTwinList();
    },
    beforeUnmount() {
        this.emitter.off("twinsChanged");
        window.removeEventListener("keydown", this.onWindowKeydownListener);
    },
    methods: {
        isMobile() {
            if(/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
                return true
            } else {
                return false
            }
        },
        setTwinList(dtId) {
            this.twinList = [];
            this.selectedNodes = [];
            this.jsonData = "";

            if (dtId) {
                let twin = this.allTwinList.find(d => d["dt-id"].includes(dtId));
                this.twinList.push(twin);
                
                this.getRelationTwin(twin, this.twinList);
                this.isTotalGraph = false;
            } else {
                this.twinList = this.allTwinList;
                this.isTotalGraph = true;
            }

            if (dtId) {
                let twin = this.twinList.find(d => d["dt-id"].includes(dtId));
                if (twin) {
                    this.selectedNodes = [twin["dt-id"]];
                } else {
                    this.selectedNodes = [this.twinList[0]["dt-id"]];;
                }
            } else {
                this.selectedNodes = [this.twinList[0]["dt-id"]];;
            }

            this.setGraphData();
          
        },
        getRelationTwin(twin, twinList) {
            if (twin.relations) {
                let relations = twin.relations.filter(d => d["dt-id"] && d["relationType"] == 'child');
                relations.forEach(relation => {
                    let relationTwin = this.allTwinList.find(d => d["dt-id"] == relation["dt-id"]);
                    if (twinList.indexOf(relationTwin) == -1) {
                        twinList.push(relationTwin);
                    }

                    this.getRelationTwin(relationTwin, twinList);
                });
            }
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
                if (this.$route.params.id == 'all') {
                    this.setTwinList();
                } else {
                    this.setTwinList(this.$route.params.id);
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
        updateLayout() {
            this.layout();
        },
        setSvgPanZoom() {
            if (this.svgPanZoom) {
                this.svgPanZoom.destroy();
                this.svgPanZoom = undefined;
                d3.select(this.$refs["svg"]).selectAll("*").remove();
            }

            let eventsHandler = undefined;

            if (this.isMobile()) {
                eventsHandler = {
                    haltEventListeners: ['touchstart', 'touchend', 'touchmove', 'touchleave', 'touchcancel']
                    , init: function(options) {
                        var instance = options.instance
                        , initialScale = 1
                        , pannedX = 0
                        , pannedY = 0

                        // Init Hammer
                        // Listen only for pointer and touch events
                        this.hammer = Hammer(options.svgElement, {
                        inputClass: Hammer.SUPPORT_POINTER_EVENTS ? Hammer.PointerEventInput : Hammer.TouchInput
                        })

                        // Enable pinch
                        this.hammer.get('pinch').set({enable: true})

                        // Handle double tap
                        this.hammer.on('doubletap', function(ev){
                        instance.zoomIn()
                        })

                        // Handle pan
                        this.hammer.on('panstart panmove', function(ev){
                        // On pan start reset panned variables
                        if (ev.type === 'panstart') {
                            pannedX = 0
                            pannedY = 0
                        }

                        // Pan only the difference
                        instance.panBy({x: ev.deltaX - pannedX, y: ev.deltaY - pannedY})
                        pannedX = ev.deltaX
                        pannedY = ev.deltaY
                        })

                        // Handle pinch
                        this.hammer.on('pinchstart pinchmove', function(ev){
                        // On pinch start remember initial zoom
                        if (ev.type === 'pinchstart') {
                            initialScale = instance.getZoom()
                            instance.zoomAtPoint(initialScale * ev.scale, {x: ev.center.x, y: ev.center.y})
                        }

                        instance.zoomAtPoint(initialScale * ev.scale, {x: ev.center.x, y: ev.center.y})
                        })

                        // Prevent moving the page on some devices when panning over SVG
                        options.svgElement.addEventListener('touchmove', function(e){ e.preventDefault(); });
                    }

                    , destroy: function(){
                        this.hammer.destroy()
                    }
                    }
            }

            this.svgPanZoom = SvgPanZoom(this.$refs["svg"], {
                zoomEnabled: true,
                panEnabled: true,
                minZoom: 0.5,
                maxZoom: 2,
                zoomScaleSensitivity: 0.1,
                mouseWheelZoomEnabled: true,
                dblClickZoomEnabled: false,
                fit: false,
                customEventsHandler: eventsHandler
            });
        },
        layout() {
            let that = this;

            this.setSvgPanZoom();

            var svg = d3.select(this.$refs["svg"]);
            var inner = d3.select(".svg-pan-zoom_viewport")
            inner.attr("class", "svg-pan-zoom_viewport hide")
            inner.selectAll("*").remove();            

            var g = new dagreD3.graphlib.Graph().setGraph({
                rankdir: this.directionSelectedItem,
                nodesep: 150,
                edgesep: 50,
                ranksep: 150
            });

            this.nodes.forEach(function(node) {
                g.setNode(node.id, { label: node.label, width: 40, height: 40});
            });

            this.edges.forEach(function(edge) {
                g.setEdge(edge.from, edge.to, { label: "" });
            });

            g.nodes().forEach(function(v) {
                var node = g.node(v);
                node.rx = node.ry = 40;
            });

            g.graph().transition = function(selection) {
                return selection.transition().duration(500);
            };

            var render = new dagreD3.render();
            render(inner, g);

            inner.selectAll("g.node").attr("class", "node context-menu");
            inner.selectAll("g.edgePath").attr("class", "edgePath context-menu");

            inner.selectAll("g.node rect")
            .on("mouseover", function(e, d) { 
                if (that.selectedNodes.indexOf(d) == -1) {
                    d3.select(this).attr("class", "hover");

                    if (that.isAddParentRelation) {
                        that.drawSelectedCircle(this, d);
                        that.drawRelationType(d, "parent");
                        that.drawVirtualLine(d, "parent");
                    } else if (that.isAddChildRelation) {
                        that.drawSelectedCircle(this, d);
                        that.drawRelationType(d, "child");
                        that.drawVirtualLine(d, "child");
                    }
                }
            })
            .on("mouseout", function(e, d) { 
                d3.select(this).attr("class", null);
                if (that.selectedNodes.indexOf(d) == -1) {
                    if (that.isAddParentRelation || that.isAddChildRelation) {
                        d3.selectAll(".nodes-selections g").filter(function(datum) { return datum == d; }).remove();
                        d3.selectAll(".virtual-edge").selectAll("*").remove();
                    }
                }
            })
            .on("mousedown touchstart", function(e, d) {
                if (e.button == 2) {
                    return;
                }

                if (e instanceof TouchEvent) {
                 //   e.stopPropagation();
                    e.preventDefault();
                }
                
                d3.selectAll(".virtual-edge").selectAll("*").remove();

                if (that.contextMenuSelectedItem && (that.isAddParentRelation || that.isAddChildRelation)) {
                    if (that.isAddParentRelation) {
                        that.setRelation(d, that.contextMenuSelectedItem);
                    } else {
                        that.setRelation(that.contextMenuSelectedItem, d);
                    }

                    that.isAddParentRelation = false;
                    that.isAddChildRelation = false;
                    that.contextMenuSelectedItem = undefined;
                } else {
                    if (that.selectedNodes.indexOf(d) != -1) {
                        d3.selectAll(".nodes-selections g").filter(function(datum) { return datum == d; }).remove();
                        that.selectedNodes.splice(that.selectedNodes.indexOf(d), 1);
                        that.setJsonData();

                        d3.selectAll(".nodes-selections rect").remove();
                        d3.selectAll(".nodes-selections text").remove();
                    } else {
                        if (e.shiftKey) {
                            if (that.selectedNodes.length == that.selectedNodeCount) {
                                let firstElement = that.selectedNodes.shift();
                                d3.selectAll(".nodes-selections g").filter(function(datum) { return datum == firstElement; }).remove();
                            }

                            that.nodeClick(this, d);

                            if (that.selectedNodes.length == that.selectedNodeCount) {
                                that.selectedNodes.forEach((node, index) => {
                                    that.drawRelationType(node, index == 0 ? "parent" : "child");
                                });

                                that.drawVirtualLine(d, "parent");
                            }
                        } else {
                            clearNodeAndClick(this, d);

                            // if (that.isMobile()) {
                            //     inner.selectAll("g.node g.mobile-context").attr("visibility", "hidden");
                            //     inner.selectAll("g.node g.mobile-context").filter(function(datum) { return datum == d; }).attr("visibility", "visible");
                            // }
                        }
                    }
                }
            })
            .on("dblclick",function(e, d){ 
                if (that.isTotalGraph) {
                    that.contextMenuSelectedItem = d;
                    that.onChildGraphViewClick();
                }
            })
            .on("contextmenu", function(e, d) {
                that.contextType = "node";
                that.contextMenuSelectedItem = d;

                if (that.selectedNodes.indexOf(d) == -1) {
                    d3.selectAll(".virtual-edge").selectAll("*").remove();
                    clearNodeAndClick(this, d);
                }
            });

            let clearNodeAndClick = (element, d) => {
                if (that.selectedNodes.length > 0) {
                    that.selectedNodes.forEach(node => {
                        d3.selectAll(".nodes-selections g").filter(function(datum) { return datum == node; }).remove();
                    })
                }

                that.selectedNodes = [];

                if (element && d) that.nodeClick(element, d);
            }

            // if (this.isMobile()) {
            //     inner
            //     .selectAll("g.node")
            //     .each(function (p, j) { 
            //         let dtId = d3.select(this).select("rect").datum();

            //         let mobileContextGroup = d3.select(this)
            //         .append("g")
            //         .datum(dtId)
            //         .attr("class", "mobile-context context-menu")
            //         .attr("visibility", "hidden")
            //         .on("touchstart",function(e, d){ 
            //             setTimeout(() => {
            //                 let evt = new MouseEvent('contextmenu',{bubbles:true, clientX: e.clientX, clientY: e.clientY});
            //                 d3.select(this.parentElement).select("rect").node().dispatchEvent(evt);    
            //             }, 100);
                        
            //         });

            //         mobileContextGroup.append("circle").attr("cx", 57).attr("cy", -35).attr("r", 20).attr("fill", "red");
            //         mobileContextGroup.append("circle")
            //             .attr("cx", 45)
            //             .attr("cy", -35)
            //             .attr("r", 5)
            //             .attr("fill", "white");

            //         mobileContextGroup.append("circle")
            //             .attr("cx", 57)
            //             .attr("cy", -35)
            //             .attr("r", 5)
            //             .attr("fill", "white");

            //         mobileContextGroup.append("circle")
            //             .attr("cx", 69)
            //             .attr("cy", -35)
            //             .attr("r", 5)
            //             .attr("fill", "white");
            //     });
            // }


            inner.selectAll("g.edgePaths .edgePath")
            .on("mouseover", function(e, d) { 
                d3.select(this).attr("class", "edgePath context-menu hover");
            })
            .on("mouseout", function(e) { 
                d3.select(this).attr("class", "edgePath context-menu");
            })
            .on("contextmenu", function(e, d) {
                that.contextType = "link";
                that.contextMenuSelectedItem = d;
            });

            inner
                .selectAll("g.node")
                .attr("title", function(v) {
                    return "hello world";
                });
                // .attr("id", function(v) {
                //     return "idaafadds";
                // });

                // this.svgPanZoom.zoom(0.5);
            // this.svgPanZoom.pan({x:0, y:0});
            // setTimeout(() => {
            //     this.svgPanZoom.zoom(0.5);
            // this.svgPanZoom.fit();    
            // }, 1000);
            
            // var size = this.svgPanZoom.getSizes();
            // console.log(size);
            // var realZoom=  this.svgPanZoom.getSizes().realZoom;
            // this.svgPanZoom.pan({  
            //     x: -(inner.node().getBBox().width*realZoom)+(this.svgPanZoom.getSizes().width/2),
            //     y: -(inner.node().getBBox().height*realZoom)+(this.svgPanZoom.getSizes().height/2)
            // }); 

            inner.select(".output").insert('g','.nodes').attr("class", "virtual-edge");
            inner.select(".output").insert('g','.nodes').attr("class", "nodes-selections");


            if (this.selectedNodes.length > 0) {
                this.selectedNodes.forEach(d => {
                    let element = inner.selectAll("g.node rect").filter(function(datum) { return datum == d; });
                    if (element.size() > 0) {
                        this.nodeClick(element.node(), d);
                    }
                });
            }

            let selection = inner.append("rect")
                .attr("class", "selection")
                .attr("visibility", "hidden");
            
            var startSelection = function(start) {
                selection.attr("x", start.x).attr("y", start.y).attr("width", 0).attr("height", 0).attr("visibility", "visible");
            };

            var moveSelection = function(start, moved) {
                let x = Math.min(start.x, moved.x);
                let y = Math.min(start.y, moved.y);
                let width = Math.abs(moved.x - start.x);
                let height = Math.abs(moved.y - start.y);

                selection.attr("x", x).attr("y", y).attr("width", width).attr("height", height);
            };

            var endSelection = function(start, end) {
                selection.attr("visibility", "hidden");
            };

            var getMousePosition = (event) => {
                let point = svg.node().createSVGPoint();
                point.x = event.offsetX;
                point.y = event.offsetY;

                point = point.matrixTransform(inner.node().getCTM().inverse());
                return {
                    x: point.x,
                    y: point.y
                };
            }
            
            svg
            .on("mousedown", function(event) {
                if (event.ctrlKey) {
                    that.svgPanZoom.disablePan();

                    var subject = d3.select(window),
                    start = getMousePosition(event);
                    startSelection(start);
                    subject
                    .on("mousemove.selection", function(event2) {
                        moveSelection(start, getMousePosition(event2));
                    })
                    .on("mouseup.selection", function(event3) {
                        endSelection(start, getMousePosition(event3));
                        subject.on("mousemove.selection", null).on("mouseup.selection", null);
                        that.svgPanZoom.enablePan();
                    });
                } else {
                    if ((!event.shiftKey && that.selectedNodes.length == that.selectedNodeCount)  || that.isAddParentRelation || that.isAddChildRelation) {
                        d3.selectAll(".virtual-edge").selectAll("*").remove();
                        that.removeRelationType();
                        clearNodeAndClick();
                        that.setJsonData();
                    }

                    return;
                    if (that.selectedNodes.length == that.selectedNodeCount || that.isAddParentRelation || that.isAddChildRelation) {
                        if (that.selectedNodes.length == that.selectedNodeCount) {
                            d3.selectAll(".nodes-selections").selectAll("*").remove();
                            that.selectedNodes = [];
                            that.setJsonData();
                        }

                        d3.selectAll(".virtual-edge").selectAll("*").remove();

                        that.removeRelationType();
                    }
                }
            })
            .on("keydown", function(event) {
                console.log(event)
            });

            setTimeout(() => {
                var bb=inner.node().getBBox();
                var vbb=this.svgPanZoom.getSizes().viewBox;
                var x=vbb.width/2-bb.x-bb.width/2;
                var y=vbb.height/2-bb.y-bb.height/2;
                var rz=this.svgPanZoom.getSizes().realZoom;
                var zoom=vbb.width/bb.width;
                this.svgPanZoom.panBy({x:x*rz,y:y*rz});
                this.svgPanZoom.zoom(zoom); 
                inner.attr("class", "svg-pan-zoom_viewport show");
            }, 500);
            

            // var bb=inner.node().getBBox();
            // var vbb=this.svgPanZoom.getSizes().viewBox;
            // var x=vbb.width/2-bb.x-bb.width/2;
            // var y=vbb.height/2-bb.y-bb.height/2;
            // var rz=this.svgPanZoom.getSizes().realZoom;
            // var zoom=vbb.width/bb.width;
            // this.svgPanZoom.panBy({x:x*rz,y:y*rz});
            // this.svgPanZoom.zoom(zoom);
            
            
        },
        nodeClick(element, data) {
            if (this.selectedNodes.indexOf(data) == -1) this.selectedNodes.push(data);

            this.drawSelectedCircle(element, data);
            this.setJsonData(data);

            console.log("nodeClick");
        },
        drawSelectedCircle(element, data) {
            d3.select(element).attr("class", null);

            let parentNode = d3.select(element).node().parentNode;
            let transform = d3.select(parentNode).attr("transform");
            d3.select(".nodes-selections").append("g").datum(data).attr("transform", transform).append("circle");
        },
        drawRelationType(dtId, relationType, isDrawLine = false) {
            let group = d3.selectAll(".nodes-selections g")
                                    .filter(function(datum) { return datum == dtId; })
                                    .attr("class", relationType);
            group
                .append("rect");
            group
                .append("text")
                .attr("x", 0)
                .attr("y", -50)
                .text(relationType == "parent" ? "P" : "C");
        },
        drawVirtualLine(dtId, relationType) {
            let sourceGroup = d3.selectAll(".nodes-selections g").filter(function(datum) { return datum == dtId; });
            let sourceTransform = sourceGroup.attr("transform");
            let souceTranslateSplit = sourceTransform.substring(10, sourceTransform.length - 1).split(",");
            
            let targetGroup = d3.selectAll(".nodes-selections g").filter(function(datum) { return datum != dtId; });
            let targetTransform = targetGroup.attr("transform");
            let targetTranslateSplit = targetTransform.substring(10, targetTransform.length - 1).split(",");

            let x1 = relationType == 'parent' ? souceTranslateSplit[0] : targetTranslateSplit[0];
            let y1 = relationType == 'parent' ? souceTranslateSplit[1] : targetTranslateSplit[1];
            let x2 = relationType == 'parent' ? targetTranslateSplit[0] : souceTranslateSplit[0];
            let y2 = relationType == 'parent' ? targetTranslateSplit[1] : souceTranslateSplit[1];

            d3.selectAll(".virtual-edge").selectAll("*").remove();

            d3.select(".virtual-edge")
                .append("line")
                .attr("x1", x1)
                .attr("y1", y1)
                .attr("x2", x2)
                .attr("y2", y2)
        },
        removeRelationType() {
            d3.selectAll(".nodes-selections rect").remove();
            d3.selectAll(".nodes-selections text").remove();

            this.selectedNodes.forEach(node => {
                d3.selectAll(".nodes-selections g").filter(function(datum) { return datum != node; }).remove();
            })
            this.isAddParentRelation = false;
            this.isAddChildRelation = false;
            this.contextMenuSelectedItem = undefined;
        },
        onWindowKeydownListener(e) {
            if (e.keyCode == 27) {
                if (this.selectedNodes.length == this.selectedNodeCount || this.isAddParentRelation || this.isAddChildRelation) {
                    if (this.selectedNodes.length == this.selectedNodeCount) {
                        d3.selectAll(".nodes-selections").selectAll("*").remove();

                        this.selectedNodes = [];
                        this.setJsonData();
                    }

                    d3.selectAll(".virtual-edge").selectAll("*").remove();

                    this.removeRelationType();
                }
            }
        },
        onSvgContextMenu(event) {
            // if (this.selectedNodes.length > 0) {
            //     this.selectedNodes.forEach(node => {
            //         d3.selectAll(".nodes-selections g").filter(function(datum) { return datum == node; }).remove();
            //     })
            // }

            // this.selectedNodes = [];
            // this.setJsonData();
            event.preventDefault();
        },  
        getNodeAndEdge(relations, nodeId) {
            if (relations && Array.isArray(relations)) {
                relations.forEach(d => {
                    if (d["dt-id"] && d["relationType"]) {
                        let findItem = this.twinList.find(twin => twin["dt-id"] == d["dt-id"]);
                        if (findItem) {
                            if (this.checkTwinList.indexOf(findItem) == -1) {
                                this.nodes.push({
                                    id : findItem["dt-id"],
                                    label: findItem["name"]
                                });

                                this.checkTwinList.push(findItem);

                                if (d["relationType"] == "parent") {
                                    this.edges.push({ 
                                        from: findItem["dt-id"], to: nodeId
                                    });
                                } else {
                                    this.edges.push({ 
                                        from: nodeId, to: findItem["dt-id"]
                                    });
                                }

                                this.getNodeAndEdge(findItem["relations"], findItem["dt-id"]);
                            }
                        }
                    }
                });
            }
        },
        setGraphData() {
            this.checkTwinList = [];

            this.nodeIndex = 1;
            this.nodes = [];

            this.edgeIndex = 1;
            this.edges = [];

            this.twinList.forEach(d=> {
                if (this.checkTwinList.indexOf(d) == -1) {
                    this.nodes.push({
                        id: d["dt-id"],
                        label: d["name"]
                    });
                    
                    this.checkTwinList.push(d);

                    this.getNodeAndEdge(d["relations"], d["dt-id"]);
                }
                
            });

            this.updateLayout();
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
        setJsonData(dtId) {
            if (!dtId) {
                dtId = this.selectedNodes[this.selectedNodes.length - 1];
            }

            this.jsonData = dtId ? JSON.stringify(this.twinList.find(d => d["dt-id"] == dtId), null, 2) : "";
            this.tmpJsonData = this.jsonData;
        },
        onCreateTwinClick() {
            this.isCreateTwinDialog = true;
        },
        closeCreateTwinDialog() {
            this.isCreateTwinDialog = false;
        },
        onAddRelationClick() {
            // if (this.selectedNodes.length == 2) {
            //     let sourceNode = this.twinList.find(d => d["dt-id"] == this.selectedNodes[0]);
            //     let targetNode = this.twinList.find(d => d["dt-id"] == this.selectedNodes[1]);

            //     this.selectParentTwinList = [sourceNode, targetNode];
            //     this.isSelectParentTwinDialog = true;
            // }

            this.setRelation(this.selectedNodes[0], this.selectedNodes[1]);
        },
        closeSelectParentTwinDialog(data) {
            this.isSelectParentTwinDialog = false;
            this.selectParentTwinList = [];

            if (data) {
                let sourceIndex = 0;
                this.selectedNodes.forEach((d, index) => {
                    if (d == data["dt-id"]) {
                        sourceIndex = index;
                    }
                });

                this.setRelation(this.selectedNodes[sourceIndex], this.selectedNodes[sourceIndex == 0 ? 1 : 0]);
            }
        },
        onContextShowing(e) {
            if (this.contextType == "node") {
                if (this.selectedNodes.length == 2) {
                    this.nodeContextItems[1].visible = false;
                    this.nodeContextItems[2].visible = false;
                    this.nodeContextItems[3].visible = true;
                } else {
                    this.nodeContextItems[1].visible = true;
                    this.nodeContextItems[2].visible = true;
                    this.nodeContextItems[3].visible = false;
                }

                this.nodeContextItems[5].disabled = true;

                let twin = this.twinList.find(d => d["dt-id"] == this.contextMenuSelectedItem);
                if (twin && twin.features && twin.features.filter(d => (d.name === 'mindsphere' || d.name === 'ditto')).length > 0) {
                    this.nodeContextItems[5].disabled = false;
                }

                this.contextItems = this.nodeContextItems;
            } else {
                this.contextItems = this.edgeContextItems;
            }
        },
        onContextHidden(e) {
            this.contextType = "node";
        },
        onContextMenuClick(e) { 
            if (this.contextMenuSelectedItem) {
                e.itemData.clickEvent();
            }
	    },
        setRelation(sourceDtId, targetDtId) {
            if (sourceDtId && targetDtId) {
                let sourceNode = this.twinList.find(d => d["dt-id"] == sourceDtId);
                let targetNode = this.twinList.find(d => d["dt-id"] == targetDtId);

                let sourceRelations = sourceNode["relations"] || [];
                sourceRelations.push({
                    "dt-id" : targetNode["dt-id"],
                    "relationType": "child"
                });
                sourceNode["relations"] = sourceRelations;

                let targetRelations = targetNode["relations"] || [];
                targetRelations.push({
                    "dt-id" : sourceNode["dt-id"],
                    "relationType": "parent"
                });
                targetNode["relations"] = targetRelations;

                let callback = () => {
                    this.getTwinList();
                }

                this.editTwin(sourceNode["dt-id"], JSON.stringify(sourceNode), callback);
            }
        },
        editTwin(dtId, json, callback = null) {
            if (dtId && json) {
                this.emitter.emit("setLoading", true);

                let param = {
                    "dt-id": dtId,
                    "json": json,
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
                        if (callback != null) {
                            callback();
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
            }
        },
        deleteTwin() {
            if (this.contextMenuSelectedItem) {
                this.emitter.emit("showConfirmDialog", {
                    message: "선택한 TWIN을 삭제하시겠습니까?",
                    isConfirm: true,
                    callback: (state) => {
                        if (state) {
                            this.emitter.emit("setLoading", true);

                            let twin = this.twinList.find(d => d["dt-id"] == this.contextMenuSelectedItem);
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
                                    this.contextMenuSelectedItem = undefined;
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

            
        },
        deleteEdge() {
            if (this.contextMenuSelectedItem && this.contextMenuSelectedItem.v && this.contextMenuSelectedItem.w) {
                this.emitter.emit("showConfirmDialog", {
                    message: "선택한 Edge를 삭제하시겠습니까?",
                    isConfirm: true,
                    callback: (state) => {
                        if (state) {
                            let sourceNode = this.twinList.find(d => d["dt-id"] == this.contextMenuSelectedItem.v);
                            let targetNode = this.twinList.find(d => d["dt-id"] == this.contextMenuSelectedItem.w);

                            let sourceRelations = sourceNode["relations"];
                            if (sourceRelations) {
                                let relation = sourceRelations.find(d => d["dt-id"] == targetNode["dt-id"]);
                                if (relation) sourceRelations.splice(sourceRelations.indexOf(relation), 1);
                            }

                            let targetRelations = targetNode["relations"];
                            if (targetRelations) {
                                let relation = targetRelations.find(d => d["dt-id"] == sourceNode["dt-id"]);
                                if (relation) targetRelations.splice(targetRelations.indexOf(relation), 1);
                            }

                            let callback = () => {
                                this.contextMenuSelectedItem = undefined;
                                this.getTwinList();
                            }

                            this.editTwin(sourceNode["dt-id"], JSON.stringify(sourceNode), callback);
                        }
                    },
                });
            }
        },
        onDataMonitoringClick() {
            if (this.contextMenuSelectedItem) {
                let selectedItem = this.contextMenuSelectedItem;
                this.contextMenuSelectedItem = undefined;
                this.dataMonitoringTwin = this.twinList.find(d => d["dt-id"] == selectedItem);

                if (this.dataMonitoringTwin.features) {
                    let features = this.dataMonitoringTwin.features.find(d => d.name == 'mindsphere' || d.name == 'ditto');
                    if (features.name == 'mindsphere') {
                        this.isDataMonitoringDialog = true;
                    } else {
                        this.isDittoDataMonitoringDialog = true;
                    }
                }
            }
        },
        closeDataMonitoringDialog() {
            this.isDataMonitoringDialog = false;
            this.isDittoDataMonitoringDialog = false;
            this.dataMonitoringTwin = undefined;
        },
        onSetDataMonitoringClick() {
            if (this.contextMenuSelectedItem) {
                let selectedItem = this.contextMenuSelectedItem;
                this.contextMenuSelectedItem = undefined;
                this.dataMonitoringTwin = this.twinList.find(d => d["dt-id"] == selectedItem);
                this.isSetDataMonitoringDialog = true;
            }
        },
        closeSetDataMonitoringDialog(data) {
            if (data) {
                this.getTwinList();
            }

            this.isSetDataMonitoringDialog = false;
        }, 
        onChildGraphViewClick() {
            if (this.contextMenuSelectedItem) {
                let selectedItem = this.contextMenuSelectedItem;
                this.contextMenuSelectedItem = undefined;

                this.setTwinList(selectedItem);    
            }
        },
        onCollapseChild() {
            if (this.contextMenuSelectedItem) {
                let childTwinList = [];
                this.getRelationTwin(this.twinList.find(d => d["dt-id"] == this.contextMenuSelectedItem), childTwinList);

                let parentNode = d3.selectAll("g.node").filter(function(datum) { return datum == this.contextMenuSelectedItem; });

                childTwinList.forEach(d => {
                    d3.selectAll("g.node").filter(function(datum) { return datum == d["dt-id"]; }).attr("visibility", "hidden");
                    d3.selectAll("g.edgePath").filter(function(datum) { return datum.v == d["dt-id"] || datum.w == d["dt-id"]; }).attr("visibility", "hidden");
                });

                
                console.log(parentNode);
            }
        },
        addParentRelation() {
            if (this.contextMenuSelectedItem) {
                this.isAddParentRelation = true;
                this.isAddChildRelation = false;
                
                let selectedItem = this.contextMenuSelectedItem;
                let element = d3.selectAll("g.node rect").filter(function(datum) { return datum == selectedItem; });
                if (element.size() > 0) {
                    d3.selectAll(".nodes-selections").selectAll("*").remove();

                    this.selectedNodes = [];
                    this.nodeClick(element.node(), selectedItem);

                    this.drawRelationType(selectedItem, "child");
                    
                }
            }
        },
        addChildRelation() {
            if (this.contextMenuSelectedItem) {
                this.isAddParentRelation = false;
                this.isAddChildRelation = true;
                
                let selectedItem = this.contextMenuSelectedItem;
                let element = d3.selectAll("g.node rect").filter(function(datum) { return datum == selectedItem; });
                if (element.size() > 0) {
                    d3.selectAll(".nodes-selections").selectAll("*").remove();

                    this.selectedNodes = [];
                    this.nodeClick(element.node(), selectedItem);

                    this.drawRelationType(selectedItem, "parent");
                }
            }
        },
        onSaveTwinClick() {
            this.emitter.emit("showConfirmDialog", {
                message: "TWIN 정보를 변경하시겠습니까?\n변경 시 다른 TWIN 정보(Relation)에 영향을 줄 수 있습니다.",
                isConfirm: true,
                callback: (state) => {
                    if (state) {
                        try {
                            let dtId = JSON.parse(this.tmpJsonData)["dt-id"];
                            let jsonObject = JSON.parse(this.jsonData);
                            let callback = () => {
                                this.emitter.emit("twinsChanged");
                            }

                            this.editTwin(dtId, this.jsonData, callback);
                        } catch(e) {
                            this.emitter.emit("setLoading", false);

                            this.emitter.emit("showConfirmDialog", {
                                message: "JSON 형태가 올바르지 않습니다.",
                                isConfirm: false
                            });
                        } 
                    }
                },
            });
        },
        onResetTwinClick() {
            this.emitter.emit("showConfirmDialog", {
                message: "JSON을 초기화하시겠습니까?",
                isConfirm: true,
                callback: (state) => {
                if (state) {
                    this.jsonData = this.tmpJsonData;
                }
                },
            });
        },
        onDownloadJsonClick() {
            let dtId = JSON.parse(this.tmpJsonData)["dt-id"];

            let param = {
                "type": "single",
                "dt-id": dtId,
            }

            let fileName = "twin.json";
            fetch("/api/twin/download", {
                method: "POST",
                headers: {
                "Content-Type": "application/json",
                },
                body: JSON.stringify(param),
            })
            .then((resp) => {
                if (resp.status === 200) {
                    fileName = resp.headers.get("content-disposition").match(/(?<=")(?:\\.|[^"\\])*(?=")/)[0];
                    return resp.blob();
                } else if (resp.status === 204) {
                    return "등록된 TWIN이 없습니다.";
                }
                else if (resp.status === 400) {
                    return Promise.reject("파리미터가 올바르지 않습니다.");
                } else if (resp.status == 500) {
                    return Promise.reject("서버에서 오류가 발생하였습니다.");
                }
            })
            .then((result) => {
                this.emitter.emit("setLoading", false);

                if (typeof(result) == 'string') {
                    this.emitter.emit("showConfirmDialog", {
                        message: result,
                        isConfirm: false
                    });
                } else {  
                    saveAs(result, fileName);
                }
            })
            .catch((err) => {
                this.emitter.emit("setLoading", false);
                this.emitter.emit("showErrorDialog", err);
            });
        },
        zoomIn() {
            this.svgPanZoom.zoomIn();
        },
        zoomOut() {
            this.svgPanZoom.zoomOut();
        }
    }
};
</script>

<template>
    <div id="detailInformation">
        <div class="centerPanel">
            <div class="titlePanel">
                <div class="text">
                    Graph
                </div>

                <div class="toolbar">
                    <DxToolbar>
                        <DxItem
                            :options="directionSelectBoxOptions"
                            location="before"
                            locate-in-menu="always"
                            widget="dxSelectBox"
                        />
                        <DxItem
                            :options="addTwinButtonOptions"
                            location="after"
                            locate-in-menu="always"
                            widget="dxButton"
                        />

                        <DxItem v-if="!isMobile()"
                            :options="addEdgeButtonOptions"
                            location="after"
                            locate-in-menu="always"
                            widget="dxButton"
                            :disabled="selectedNodes.length != 2"
                        />

                        <DxItem v-if="!isMobile()"
                            :options="selectionButtonOptions"
                            location="after"
                            locate-in-menu="always"
                            widget="dxButton"
                        /> 

                        <DxItem v-if="isMobile()"
                            location="after"
                            locate-in-menu="always"
                            widget="dxButton"
                            @click="zoomIn"
                        >
                            <div class="dx-item-custom">
                                <font-awesome-icon icon="fa-solid fa-search-plus" />
                                <span class="dx-button-text">Zoom In</span>
                            </div>
                        </DxItem>

                        <DxItem v-if="isMobile()"
                            location="after"
                            locate-in-menu="always"
                            widget="dxButton"
                            @click="zoomOut"
                        >
                            <div class="dx-item-custom">
                                <font-awesome-icon icon="fa-solid fa-search-minus" />
                                <span class="dx-button-text">Zoom Out</span>
                            </div>
                        </DxItem>

                        <DxItem
                            :options="totalGraphButtonOptions"
                            locate-in-menu="always"
                            widget="dxButton"
                        />

                     
                        <!-- <DxItem
                            :options="saveButtonOptions"
                            locate-in-menu="always"
                            widget="dxButton"
                        /> -->

                        <DxItem
                            :options="resetButtonOptions"
                            locate-in-menu="always"
                            widget="dxButton"
                        />
                    </DxToolbar>
                </div>
            </div>

            <div class="content relative" >
                <div id="graph" ref="graph">
                    <svg id="svg" ref="svg" @contextmenu="onSvgContextMenu"></svg>

                    <DxContextMenu
                        :data-source="contextItems"
                        :width="130"
                        target=".context-menu"
                        @showing="onContextShowing($event)"
                        @hidden="onContextHidden($event)"
                        @item-click="onContextMenuClick($event)"
                    />
                </div>
            </div>
        </div>

        <div class="rightPanel">
            <div class="topPanel">
                <div class="titlePanel">
                    <div class="text">
                        JSON
                    </div>

                    <div class="toolbar">
                        <DxToolbar>
                            <DxItem
                                :options="saveJsonButtonOptions"
                                :disabled="selectedNodes == 0"
                                locate-in-menu="always"
                                widget="dxButton"
                            />

                            <DxItem
                                :options="resetJsonButtonOptions"
                                :disabled="selectedNodes == 0"
                                locate-in-menu="always"
                                widget="dxButton"
                            />

                            <DxItem
                                :options="downloadJsonButtonOptions"
                                :disabled="selectedNodes == 0"
                                locate-in-menu="always"
                                widget="dxButton"
                            />
                        </DxToolbar>
                    </div>
                </div>

                <div class="content">
                    <MonacoEditor :options="options" language="json" v-model:value="jsonData"
                        @editorWillMount="editorWillMount" @editorDidMount="editorDidMount"></MonacoEditor>
                </div>
            </div>
        </div>

        <CreateTwinDialog 
            v-if="isCreateTwinDialog"
            :propsIsDialog="isCreateTwinDialog"
            @close="closeCreateTwinDialog"
        />

        <DataMonitoringDialog
            v-if="isDataMonitoringDialog"
            :propsIsDialog="isDataMonitoringDialog"
            :propsTwin="dataMonitoringTwin"
            @close="closeDataMonitoringDialog"
        />

        <DittoDataMonitoringDialog
            v-if="isDittoDataMonitoringDialog"
            :propsIsDialog="isDittoDataMonitoringDialog"
            :propsTwin="dataMonitoringTwin"
            @close="closeDataMonitoringDialog"
        />
        

        <SetDataMonitoringDialog
            v-if="isSetDataMonitoringDialog"
            :propsIsDialog="isSetDataMonitoringDialog"
            :propsTwin="dataMonitoringTwin"
            @close="closeSetDataMonitoringDialog"
        />

        <SelectParentTwinDialog
            v-if="isSelectParentTwinDialog"
            :propsIsDialog="isSelectParentTwinDialog"
            :propsSelectedTwinList="selectParentTwinList"
            @close="closeSelectParentTwinDialog"
        />

        
        

       
    </div>
</template>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
#detailInformation {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: row;
    gap: 20px;

    

    .centerPanel {
        display: flex;
        flex-direction: column;
        width: 60%;
        height: 100%;
        background-color: #1F2128;
        border-radius: 10px;
        padding: 20px;
    }

    .rightPanel {
        display: flex;
        flex-direction: column;
        width: 40%;
        height: 100%;
        gap: 20px;

        .topPanel {
            display: flex;
            flex-direction: column;
            height: 100%;
            background-color: #1F2128;
            border-radius: 10px;
            padding: 20px;
        }

        .bottomPanel {
            display: flex;
            flex-direction: column;
            height: 50%;
            background-color: #1F2128;
            border-radius: 10px;
            padding: 20px;
        }
    }

    .title {
        border-bottom: 1px solid #A6A6A6;
        padding-bottom: 10px;
    }

    .titlePanel {
        display: flex;
        flex-direction: row;
        border-bottom: 1px solid #A6A6A6;
        padding-bottom: 10px;
        position: relative;

        .graphDepthPanel {
            display: flex;
            flex-direction: row;
            gap: 20px;
            margin-left: 30px;
        }

        .toolbar {
            position: absolute;
            right: 0px;
            top: 0px;

            :deep(.dx-button-mode-contained) {
                background-color: transparent;
                border: none;
            }
        }
    }

    .content {
        width: 100%;
        height: calc(100% - 35px);
    }

    .relative {
        position: relative;
    }

    

    .context-menu {
        background-color: #1F2128;
        padding: 0px 10px;
        position: fixed;
        visibility: hidden;
        font-size: 12px;
        border: 1px solid #aaa;
        border-radius: 5px;

        > div {
            width: 100%;
            padding: 10px;
            cursor: pointer;
        }

        > div:not(:first-child) {
            border-top: 1px dashed #aaa;
        }

        .disabled {
            color: gray;
            pointer-events: none;
            user-select: none;
        }
    }

    #graph {
        width: 100%;
        height: 100%;

        svg {
            width: 100%;
            height: 100%;
            text-align: center;

            :deep(text) {
                user-select: none;
            }

            :deep(.hide) {
                opacity: 0;
            }

            :deep(.show) {
                opacity: 1;
                transition: opacity .5s linear;
            }

            :deep(.selection) {
                fill: #0000FF20;
                stroke: #AAAAFF;
                stroke-width: 2;
                stroke-dasharray: 5, 5;
            }

            :deep(.node rect) {
                fill: rgb(68, 102, 204);
                cursor: pointer;
            }

            :deep(.node rect.hover) {
                fill: rgb(51, 85, 187);
                x: -35;
                y: -35;
                width: 70px;
                height: 70px;
                rx: 50;
                ry: 50;
            }

            :deep(.nodes-selections circle) {
                fill: none;
                stroke: rgb(238, 187, 0);
                stroke-width: 5;
                r: 40;
            }

            :deep(.nodes-selections rect) {
                x: -20;
                y: -70;
                width: 40px;
                height: 40px;
                fill: white;
                rx: 20;
                ry: 20;
            }

            :deep(.nodes-selections text) {
                font-size: 28px;
                font-weight: 700;
                fill: black;
                text-anchor: middle;
                alignment-baseline: central;
            }

            :deep(.virtual-edge line) {
                stroke: red;
                stroke-width: 5px;
                stroke-dasharray: 10;
                animation: offset 2s linear infinite;
            }

            @keyframes offset {
                to {
                    stroke-dashoffset: 100;
                }   
            }

            :deep(.node text) {
                fill: #fff;
                user-select: none;
                pointer-events: none;
            }

            :deep(.edgePath path) {
                stroke: rgb(170, 170, 170);
                fill: rgb(170, 170, 170);
                stroke-width: 3px;
            }

            :deep(.edgePath.hover path) {
                stroke: rgb(51, 85, 187);
                fill: rgb(51, 85, 187);
                stroke-width: 4px;
            }

            :deep(.v-layer-virtual-edge line) {
                stroke: red;
                stroke-width: 5px;
            }
        }  
    }
}

.dx-item-custom {
    display: flex !important;
    flex-direction: row;
    width: 100%;
    height: 35px;
    padding: 0px 10px !important;
    align-items: center;

    svg {
        width: 16px;
        height: 16px;
        background-position: 0 0;
        background-size: 16px 16px;
        padding: 0;
        font-size: 16px;
        text-align: center;
        line-height: 16px;
        margin-right: 9px;
        margin-left: 0;
    }

    span {
        font-size: 12px;
    }
}

@media screen and (max-width: 600px) {
    #detailInformation {
        flex-direction: column;

        .centerPanel {
            width: 100%;
            padding: 10px !important;
        }

        .rightPanel {
            width: 100%;
        }
    }
}
</style>
