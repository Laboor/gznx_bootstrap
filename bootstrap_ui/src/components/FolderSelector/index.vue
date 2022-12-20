<template>
  <div class="folder-selector">
    <el-cascader
      :props="props"
      :style="{ width: width + 'px' }"
      v-model="pathValue"
      size="mini"
      placeholder="请选择上传路径"
      filterable
    ></el-cascader>
    <el-button
      class="input-mini-btn"
      size="mini"
      icon="el-icon-check"
      @click="handleConfirmUploadPath()"
    ></el-button>
  </div>
</template>

<script>
import api from "@/api";

export default {
  name: "FolderSelector",
  props: {
    value: String,
    width: Number,
  },
  data() {
    return {
      pathValue: [1, 2],
      props: {
        checkStrictly: true,
        lazy: true,
        lazyLoad: this.lazyLoad,
      },
    };
  },
  methods: {
    lazyLoad(node, resolve) {
      // const { level } = node;
      console.log(node);
      this.axios
        .get(api.dirList, {
          params: {
            dirPath: node.value,
          },
        })
        .then((res) => {
          var data = res.data.data;
          if (!data.length) return;
          var nodes = [];
          for (var i = 0; i < data.length; i++) {
            nodes.push({
              value: data[i],
              label: data[i],
              leaf: false,
            });
          }
          resolve(nodes);
        });
    },
    handleConfirmUploadPath() {
      if (this.pathValue) {
        this.$emit("confirm", this.pathValue);
      }
    },
  },
};
</script>

<style scoped>
.folder-selector {
  display: inline-block;
}

::v-deep .el-input__inner {
  border-top-right-radius: 0px;
  border-bottom-right-radius: 0px;
}

.input-mini-btn {
  border-left: none;
  border-top-left-radius: 0px;
  border-bottom-left-radius: 0px;
  background: #f1f1f1;
}
</style>
