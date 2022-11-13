<template>
  <div class="home-page">
    <img class="logo" alt="logo" src="../assets/gzrc_logo.png" />
    <div class="main-content">
      <el-input
        :placeholder="commandTypeOpt.placeholder"
        v-model="command"
        class="input-with-select"
        @click.native="handleSelectFile"
      >
        <el-select
          v-model="commandType"
          slot="prepend"
          @change="handleCommandChange"
        >
          <el-option
            class="el-option"
            v-for="opt of commandTypeOpts"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          >
          </el-option>
        </el-select>
        <el-button
          class="input-append-btn"
          slot="append"
          type="primary"
          @click="handleExec"
          >{{ commandTypeOpt.execBtnText }}</el-button
        >
      </el-input>

      <input
        ref="shadowFileInput"
        v-if="commandType == '3'"
        type="file"
        style="display: none"
        @change="handleAddFile"
      />

      <div class="main-table">
        <el-table :data="execTableData" v-show="commandType != '3'">
          <el-table-column
            prop="command"
            label="命令"
            width="350"
          ></el-table-column>
          <el-table-column prop="type" label="类型">
            <template slot-scope="scope">
              <el-tag
                v-if="scope.row.type == 'shell'"
                style="border-color: #d3adf7; color: #915de7;"
                color="#f9f0ff"
                type="success"
                size="small"
                >脚本</el-tag
              >
              <el-tag
                v-if="scope.row.type == 'log'"
                style="border-color: #87e8de; color: #08979c;"
                color="#e6fffb"
                type="success"
                size="small"
                >日志</el-tag
              >
            </template>
          </el-table-column>
          <el-table-column
            prop="startTime"
            label="开始时间"
            width="200"
          ></el-table-column>
          <el-table-column prop="option" label="操作">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="danger"
                @click="handleInterruptCommand(scope, scope.row)"
                >中断</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <el-table v-show="commandType == '3'" :data="updateTableData">
          <el-table-column
            prop="fileName"
            label="文件名"
            width="200"
          ></el-table-column>
          <el-table-column prop="path" label="上传路径" width="280">
            <template slot-scope="scope">
              <el-input
                v-if="!scope.row.isConfirmPath"
                v-model="scope.row.path"
                :class="{
                  'input-upload-path-err': errFlash,
                }"
                size="mini"
                placeholder="请输入上传路径"
              >
                <el-button
                  class="input-mini-btn"
                  slot="append"
                  icon="el-icon-check"
                  @click="handleConfirmUploadPath(scope, scope.row)"
                ></el-button>
              </el-input>
              <template v-else>
                {{ scope.row.path }}
                <el-tooltip
                  v-if="!scope.row.isUploading"
                  content="修改"
                  :hide-after="2000"
                  :open-delay="500"
                  placement="top"
                >
                  <i
                    class="el-icon-edit edit-upload-path-icon"
                    @click="handleEditUploadPath(scope, scope.row)"
                  ></i>
                </el-tooltip>
              </template>
            </template>
          </el-table-column>
          <el-table-column prop="progressBar" label="上传进度">
            <template slot-scope="scope">
              <el-progress
                style="width: 160px; display: inline-block"
                :percentage="scope.row.progress"
                :show-text="false"
                status="success"
              ></el-progress>
              <template v-if="scope.row.progress < 100">
                <el-tooltip
                  v-if="!scope.row.pause"
                  content="暂停"
                  :hide-after="2000"
                  :open-delay="500"
                  placement="top"
                >
                  <i
                    class="el-icon-video-pause upload-option-btn"
                    style="color: red"
                    @click="handlePauseUpload(scope, scope.row)"
                  ></i>
                </el-tooltip>
                <el-tooltip
                  v-else
                  content="继续"
                  :hide-after="2000"
                  :open-delay="500"
                  placement="top"
                >
                  <i
                    class="el-icon-video-play upload-option-btn"
                    style="color: #42b983"
                    @click="handlePauseUpload(scope, scope.row)"
                  ></i>
                </el-tooltip>
                <el-tooltip
                  content="取消"
                  :hide-after="2000"
                  :open-delay="500"
                  placement="top"
                >
                  <i
                    class="el-icon-error upload-option-btn"
                    style="color: red"
                    @click="handleInterruptUpload(scope, scope.row)"
                  ></i>
                </el-tooltip>
              </template>
              <span v-else>
                <i class="el-icon-success upload-ok" />上传完成
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog
      title="确认信息"
      :visible.sync="confirmInfoDialog"
      width="500px"
      center
      @closed="isPrintLog = '1'"
    >
      <span class="dialog-label">操作类型：</span
      ><span class="dialog-content">{{ commandTypeOpt.label }}</span
      ><br />
      <span class="dialog-label">文件路径：</span
      ><span class="dialog-content">{{ command }}</span
      ><br />
      <span class="dialog-label">是否打印日志：</span>
      <el-radio class="dialog-content" v-model="isPrintLog" label="1"
        >是</el-radio
      >
      <el-radio class="dialog-content" v-model="isPrintLog" label="0"
        >否</el-radio
      >
      <span slot="footer">
        <el-button size="small" @click="handleCloseDialog">取消</el-button>
        <el-button size="small" type="primary" @click="handleSubmit"
          >确认</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import api from '@/api';

export default {
  data() {
    return {
      command: 'ping 10.128.103.45',
      // command: '',
      commandType: '1',
      commandTypeOpts: [
        {
          label: '执行脚本',
          value: '1',
          type: 'shell',
          placeholder: '请输入文件路径或命令...',
          execBtnText: '执行',
        },
        {
          label: '浏览日志',
          value: '2',
          type: 'log',
          placeholder: '请输入日志文件路径...',
          execBtnText: '浏览',
        },
        {
          label: '上传文件',
          value: '3',
          type: 'file',
          placeholder: '请添加准备上传的文件...',
          execBtnText: '上传',
        },
      ],
      execTableData: [],
      updateTableData: [
        {
          fileName: 'xxxppp.zip',
          path: '',
          isConfirmPath: false,
          progress: 0,
          pause: false,
          isUploading: false,
        },
        {
          fileName: '1232323232323.zip',
          path: '',
          isConfirmPath: false,
          progress: 50,
          pause: false,
          isUploading: false,
        },
      ],
      confirmInfoDialog: false,
      isPrintLog: '1',
      pollingTimer: null,
      errFlash: false,
      isErrFlashing: false,
    };
  },
  mounted() {
    this.getExecList();
    var that = this;
    // 三秒轮询一次
    this.pollingTimer = setInterval(function() {
      that.getExecList();
      // that.updateTableData[0].progress += 20;
    }, 3000);
  },
  computed: {
    commandTypeOpt() {
      return this.commandTypeOpts[this.commandType - 1];
    },
  },
  methods: {
    execCommand() {
      return this.axios
        .post(api.execCommand, {
          userId: 'admin',
          command: this.command,
          type: this.commandTypeOpt.type,
          charser: 'gb2312',
        })
        .then((res) => {
          if (res.data.success) {
            return res.data;
          } else {
            this.$message.error(res.data.message);
            return res.data;
          }
        });
    },
    handleExec() {
      if (this.commandType != '3' && this.command) {
        this.confirmInfoDialog = true;
      } else if (this.commandType == '3') {
        // 检查上传地址是否为空
        for (var item of this.updateTableData) {
          if (!item.path || !item.isConfirmPath) {
            this.errorFlash();
            return;
          }
        }
        console.log('上传成功');
      } else {
        this.$message.error('文件路径或命令不能为空！');
      }
    },
    handleInterruptCommand(scope, row) {
      var that = this;
      this.axios
        .post(api.interruptCommand, {
          command: row.command,
          type: row.type,
        })
        .then((res) => {
          if (res.data.success) {
            that.$message({
              message: res.data.message,
              type: 'success',
            });
            this.getExecList();
          } else {
            this.$message.error(res.data.message);
          }
        });
    },
    handleCloseDialog() {
      this.confirmInfoDialog = false;
    },
    handleSubmit() {
      var that = this;
      this.execCommand().then((res) => {
        that.handleCloseDialog();
        if (res.success) {
          that.getExecList();
          if (that.isPrintLog == '1') {
            var url = that.$router.resolve({
              path: '/LogView',
            });
            var windowName = '_' + that.command;
            if (window.localStorage.getItem(windowName) == url.href) {
              this.$message.error('该日志消息页面已存在，请勿重复打开！');
            } else {
              this.$message({
                message: res.message,
                type: 'success',
              });
              // 延迟0.5秒打开新页面，等待原页面执行完毕
              setTimeout(function() {
                var newPage = window.open(url.href, windowName);
                newPage.onload = function() {
                  newPage.document.title = that.command + ' 日志消息';
                  window.localStorage.setItem(windowName, url.href);
                  return false;
                };
              }, 500);
            }
          } else {
            this.$message({
              message: res.message,
              type: 'success',
            });
          }
        }
      });
    },
    getExecList() {
      var that = this;
      return this.axios.get(api.getExecLiat).then((res) => {
        if (res.data.success) {
          var data = res.data.data;
          var records = [];
          for (var record of data)
            records.push({
              command: record.command,
              startTime: record.startTime,
              type: record.type,
              status: '运行中',
            });
          that.execTableData = records;
        }
      });
    },
    handleSelectFile(e) {
      if (this.commandType == '3' && e.target.tagName == 'INPUT') {
        this.$refs.shadowFileInput.click();
      }
    },
    handleCommandChange() {
      this.command = null;
    },
    handlePauseUpload(scope, row) {
      row.pause = !row.pause;
    },
    handleInterruptUpload() {},
    handleConfirmUploadPath(scope, row) {
      if (!row.path) return;
      row.isConfirmPath = true;
    },
    handleEditUploadPath(scope, row) {
      row.isConfirmPath = false;
    },
    errorFlash() {
      if (this.isErrFlashing) return;
      this.isErrFlashing = true;
      var that = this;
      var times = 6;
      var timer = setInterval(function() {
        that.errFlash = !that.errFlash;
        times--;
        if (times == 0) {
          clearInterval(timer);
          that.errFlash = false;
          that.isErrFlashing = false;
          timer = null;
        }
      }, 500);
    },
    handleAddFile(e) {
      e.preventDefault();
      var file = this.$refs.shadowFileInput.files[0];
      if (!file) return;
      console.log(file);
      this.updateTableData.push({
        id: Date.now(),
        fileName: file.name,
        path: '',
        isConfirmPath: false,
        progress: 0,
        pause: false,
        isUploading: false,
        file: file
      });
    },
  },
  destroyed() {
    clearInterval(this.pollingTimer);
    this.pollingTimer = null;
  },
};
</script>

<style scoped>
.home-page {
  position: relative;
  height: 100%;
}
.main-content {
  position: relative;
  margin: 40px auto 0 auto;
  width: 40%;
}
.logo {
  position: relative;
  margin-top: 50px;
  width: 100px;
}
.el-select {
  width: 120px;
  /* background: #02ac49 !important;
  color: white !important;
  border-radius: 4px 0 0 4px; */
}
.el-option {
  color: #606266 !important;
}
.input-append-btn {
  background: #02ac49 !important;
  color: white !important;
  border-radius: 0 4px 4px 0;
  width: 100px;
}
.input-append-btn:hover {
  background: #259555 !important;
}
.main-table {
  position: relative;
  margin-top: 70px;
  width: 100%;
  /* height: 500px;
  border: 1px solid green; */
}
.dialog-label {
  display: inline-block;
  width: 120px;
  font-size: 16px;
  line-height: 2.5;
  text-align: right;
  margin-right: 10px;
}
.dialog-content {
  font-size: 16px;
  line-height: 2;
  font-weight: bold;
}
::v-deep .el-dialog__body {
  padding: 15px 30px 15px 30px;
}
::v-deep .el-button--primary {
  background: #02ac49;
  border-color: #02ac49;
}
::v-deep .el-button--primary:hover {
  background: #259555;
  border-color: #259555;
}
::v-deep .el-button--default:hover {
  background: #2595551a !important;
  border-color: #2595551a !important;
  color: #02ac49 !important;
}
::v-deep .el-radio__input.is-checked .el-radio__inner {
  background: #02ac49;
  border-color: #02ac49;
}
::v-deep .el-radio__inner:hover {
  border-color: #02ac49;
}
::v-deep .el-radio__input.is-checked + .el-radio__label {
  color: #02ac49;
}
::v-deep .el-table thead th .cell {
  padding-left: 15px;
}
::v-deep .el-table thead th .cell::before {
  content: '*';
  color: #02ac49;
  position: absolute;
  width: 6px;
  height: 16px;
  background: #02ac49;
  left: 4px;
  top: 3px;
  border-radius: 2px;
}
/* ::v-deep .el-input--mini .el-input__inner {
  border-color: red;
} */
.input-mini-btn {
  padding: 8px 13px 8px 13px;
}
.upload-option-btn {
  font-size: 20px;
  cursor: pointer;
  margin-left: 20px;
  vertical-align: middle;
}
.upload-ok {
  font-size: 20px;
  color: #67c23a;
  margin: 0 5px 0 20px;
  vertical-align: middle;
}
.edit-upload-path-icon {
  margin-left: 5px;
  cursor: pointer;
  font-size: 18px;
  color: #40a9ff;
}
.input-upload-path-err {
  box-shadow: 0 0 2px 1px #ff4d4f;
  border-radius: 3px;
}
::v-deep .el-table .cell {
  overflow: unset;
}
</style>
