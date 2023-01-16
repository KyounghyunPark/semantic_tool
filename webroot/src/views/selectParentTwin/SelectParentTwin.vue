<script>
import JsonViewDialog from "@/views/jsonView/JsonViewDialog.vue";
const dataGridRefKey = 'select-parent-data-grid';

export default {
    name: "SelectParentTwinDialog",
    props: {
        propsIsDialog: {
            type: Boolean,
            required: true,
        },
        propsSelectedTwinList: {
            type: Array,
            required: true,
        }
    },
    components: {
        JsonViewDialog
    },
    data() {
        return {
            isDialog: false,
            twinList: [],
            isJsonViewDialog: false,
            jsonViewData: "",
            dataGridRefKey,
            returnData: undefined
        };
    },
    computed: {
        dataGrid: function () {
            return this.$refs[dataGridRefKey].instance;
        }
    },
    created() {
        this.isDialog = this.propsIsDialog;
        this.twinList = [];
        this.propsSelectedTwinList.forEach(d => {
            let newTwin = Object.assign({}, d);
            newTwin.checkBoxValue = false;

            this.twinList.push(newTwin);
        });
    },
    methods: {
        create() {
            let selectedData = this.twinList.find(d => d.checkBoxValue);
            if (!selectedData) {
                this.emitter.emit("showConfirmDialog", {
                    message: "Parent Twin을 선택하세요.",
                    isConfirm: false
                });
                
                return;
            }

            this.returnData = selectedData;
            this.close();
        },
        close() {
            this.isDialog = false;
        },
        onHidden(e) {
            this.$emit("close", this.returnData);
        },
        onJSONViewClick(data) {
            this.jsonViewData = JSON.stringify(data.data, null, 2);
            this.isJsonViewDialog = true;
        },
        closeJsonViewDialog() {
            this.jsonViewData = "";
            this.isJsonViewDialog = false;
        },
        onRowClick(e) {
            this.twinList.forEach(d =>  d.checkBoxValue = false);
            e.data.checkBoxValue = true;
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
            height="auto" title="Select Parent Twin"
            container="body" @hidden="onHidden($event)">
            <div class="dialogPanel">
                <div class="contentPanel">
                    <DxDataGrid 
                        id="gridContainer" 
                        :data-source="twinList" 
                        :show-borders="false"
                        key-expr="dt-id" 
                        :ref="dataGridRefKey"
                        @row-click="onRowClick">

                        <DxScrolling mode="virtual" />
                        <DxSorting mode="none" />
                        <DxSelection 
                            :show-check-boxes-mode="'onClick'"
                            mode="single" 
                        />

                        <DxColumn css-class="buttonTDPanel centerTitle" :width="60" caption="parent"
                            cell-template="cellTemplate1" />

                        <template #cellTemplate1="{ data }">
                            <div class="dataGridButtonPanel">
                                <DxCheckBox v-model:value="data.data.checkBoxValue"/>
                            </div>
                           
                        </template>

                        <DxColumn :width="230" caption="name" data-field="name" />
                        <DxColumn caption="description" data-field="description" />
                        <DxColumn caption="dt-id" data-field="dt-id" />

                        <DxColumn css-class="buttonTDPanel centerTitle" :width="60" caption=""
                            cell-template="cellTemplate" />

                        <template #cellTemplate="{ data }">
                            <div class="dataGridButtonPanel">
                                <div class="buttonPanel jsonView" @click="onJSONViewClick(data)">
                                    <font-awesome-icon icon="fa-solid fa-eye" />
                                </div>
                            </div>
                        </template>
                    </DxDataGrid>
                </div>

                <div class="footerPanel">
                    <DxButton class="purple" type="normal" text="Apply" @click="create()" />
                    <DxButton class="gray" type="normal" text="Close" @click="close()" />
                </div>
            </div>
        </DxPopup>

        <JsonViewDialog 
            v-if="isJsonViewDialog" 
            :propsIsDialog="isJsonViewDialog" 
            :propsJSON="jsonViewData"
            @close="closeJsonViewDialog" />
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
    height: calc(100% - 55px);
    gap: 20px;

    #gridContainer {
        height: 100%;
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
</style>
