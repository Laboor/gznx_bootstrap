<template>
  <div class="folder-selector">
    <el-cascader
      :props="props"
      :style="{ width: width + 'px' }"
      ref="folderSelector"
      size="mini"
      placeholder="请选择上传路径"
      separator="/"
      filterable
      @change.native="handleInput"
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
      props: {
        checkStrictly: true,
        lazy: true,
        lazyLoad: this.lazyLoad,
      },
    };
  },
  methods: {
    lazyLoad(node, resolve) {
      var { isLeaf, root, pathLabels } = node;
      if (isLeaf) {
        resolve();
        return;
      }
      this.axios
        .get(api.dirList, {
          params: {
            dirPath: root ? "" : pathLabels.join("/"),
          },
        })
        .then((res) => {
          var data = res.data.data;
          if (!data.length) {
            resolve();
            return;
          }
          var nodes = [];
          for (var i = 0; i < data.length; i++) {
            var label = data[i].path.substring(
              data[i].path.lastIndexOf("/") + 1
            );
            if (root) {
              label = data[i].path.slice(0, data[i].path.length - 1);
            }
            nodes.push({
              value: label,
              label: label,
              leaf: data[i].leaf,
            });
          }
          resolve(nodes);
        });
    },
    handleConfirmUploadPath() {
      var path = this.$refs.folderSelector.presentText;
      if (path.length) {
        this.$emit("confirm", path);
      }
    },
    handleInput(e) {
      this.$refs.folderSelector.presentText = e.target.value;
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
.el-cascader__dropdown ::v-deep .el-cascader-panel {
  font-size: 13px !important;
}
</style>

<style>
.el-cascader-node__label {
  font-size: 13px;
}
.el-cascader-node {
  height: 27px;
  line-height: 27px;
  padding: 0 30px 0 13px;
}
</style>
