<script>
export default {
  name: "ExapndSparqlDialog",
  props: {
    propsIsDialog: {
      type: Boolean,
      required: true,
    },
    propsSparql: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      isDialog: false,
      sparql: "",
      options: {
        fontSize : 14, 
        automaticLayout: true, 
        minimap: {
          enabled: false
        } 
      },
      monaco: undefined
    };
  },
  created() {
    this.isDialog = this.propsIsDialog;
    this.sparql = this.propsSparql;
  },
  methods: {
    apply() {
        this.returnData = this.sparql;
        this.close();
    },
    close() {
        this.isDialog = false;
    },
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
    onHidden(e) {
      this.$emit("close", this.returnData);
    },
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
      :width="800"
      height="80%"
      title="SPARQL Editor"
      container="body"
      @hidden="onHidden($event)"
    >
      <div class="dialogPanel">
        <div class="editPanel">
          <MonacoEditor
            :options="options"
            language="sparql"
            v-model:value="sparql"
            @editorWillMount="editorWillMount"
            @editorDidMount="editorDidMount"
          ></MonacoEditor>
        </div>

        <div class="footerPanel">
          <DxButton
            class="purple"
            type="normal"
            text="Apply"
            width="80"
            @click="apply()"
          />

          <DxButton
            class="gray"
            type="normal"
            text="Close"
            width="80"
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

.editPanel {
  display: flex;
  width: 100%;
  height: calc(100% - 55px);
  border: 1px solid #343434;
  border-radius: 10px;
  padding: 5px;
}

.footerPanel {
  display: flex;
  flex-direction: row;
  gap: 10px;
  justify-content: flex-end;
}
</style>
