<template>
  <div class="log-view">
    <div class="tool-bar">
      <el-tooltip
        v-if="printPause"
        content="继续"
        :hide-after="2000"
        :open-delay="500"
      >
        <i
          class="el-icon-video-play tool-btn"
          style="color: #42b983"
          @click="handlePlay"
        />
      </el-tooltip>
      <el-tooltip v-else content="暂停" :hide-after="2000" :open-delay="500">
        <i
          class="el-icon-video-pause tool-btn"
          style="color: red"
          @click="handlePlay"
        />
      </el-tooltip>
      <el-tooltip content="下载" :hide-after="2000" :open-delay="500">
        <i
          class="el-icon-download tool-btn tool-btn-hover-blue"
          @click="handleDownload"
        />
      </el-tooltip>
      <el-tooltip content="设置" :hide-after="2000" :open-delay="500">
        <i
          class="el-icon-setting tool-btn tool-btn-hover-blue"
          @click="handleSetting"
        />
      </el-tooltip>
      <el-tooltip content="清空" :hide-after="2000" :open-delay="500">
        <i
          class="el-icon-delete tool-btn tool-btn-hover-red"
          @click="handleClearLog"
        />
      </el-tooltip>
      <el-divider class="tool-divider" direction="vertical" />
      <el-tooltip content="关闭" :hide-after="2000" :open-delay="500">
        <i
          class="el-icon-circle-close tool-btn tool-btn-hover-red"
          @click="handleCloseWin"
        />
      </el-tooltip>
    </div>
    <el-dialog
      title="设置主题"
      :visible.sync="themeSettingDialog"
      width="300px"
    >
      <el-radio v-model="theme" label="light">白色</el-radio>
      <el-radio v-model="theme" label="dark">黑色</el-radio>
    </el-dialog>
    <log-message
      ref="logMessage"
      :url="websocketUrl"
      :pause="printPause"
      :theme="theme"
      @connected="handleConnected"
    />
  </div>
</template>

<script>
import LogMessage from '@/components/LogMessage';
import api from '@/api';

export default {
  name: 'LogView',
  components: {
    LogMessage,
  },
  data() {
    return {
      printPause: false,
      themeSettingDialog: false,
      theme: 'dark',
      logType: '',
      command: '',
      websocketUrl: process.env.VUE_APP_WEBSOCKET_HOST + api.websocket,
    };
  },
  created() {
    this.logType = this.$route.query.type;
    this.command = this.$route.query.command;
    var queryStr = '?type=' + this.logType + '&command=' + this.command;
    this.websocketUrl = this.websocketUrl + queryStr;
  },
  mounted() {
    var that = this;
    // 禁止刷新页面
    document.onkeydown = function(e) {
      if (e.ctrlKey == true && e.keyCode == 82) {
        return false;
      } else if (e.keyCode == 116) {
        return false;
      }
    };
    // 禁用右键菜单
    document.oncontextmenu = function() {
      return false;
    };
    // 关闭页面提醒
    window.onbeforeunload = function(e) {
      e.preventDefault();
      return '退出页面将无法恢复日志监听，是否退出？';
    };
    window.onunload = function() {
      // 关闭页面时中断日志浏览进程
      if (that.logType == 'log') {
        that.axios.post(api.interruptCommand, {
          type: that.logType,
          commandStr: that.command,
        });
      }
      window.localStorage.removeItem(window.name);
    };
  },
  methods: {
    handleConnected() {},
    handleClearLog() {
      this.$refs.logMessage.clearLogMessage();
    },
    handlePlay() {
      this.printPause = !this.printPause;
    },
    handleSetting() {
      this.themeSettingDialog = true;
    },
    handleCloseWin() {
      window.close();
    },
    handleDownload() {
      var data = this.$refs.logMessage.logMessage;
      data = data.replaceAll('<br/>', '\r\n'); // 替换换行符
      var name = '日志消息.txt';
      this.saveFile(data, name);
    },
    saveFile(data, name) {
      var urlObject = window.URL || window.webkitURL || window;
      var exportBlob = new Blob([data]);
      var saveLink = document.createElement('a');
      saveLink.href = urlObject.createObjectURL(exportBlob);
      saveLink.download = name;
      saveLink.click();
    },
  },
};
</script>

<style scoped>
.tool-bar {
  position: absolute;
  width: 290px;
  top: 15px;
  height: 50px;
  background: #ffffff;
  border-radius: 7px;
  box-shadow: 0 0 6px 3px #afafaf;
  opacity: 0.3;
  left: calc(50% - 145px);
  z-index: 100;
}
.tool-bar:hover {
  opacity: 1;
  box-shadow: 0 0 6px 3px #afafaf83;
}
.tool-btn {
  font-size: 26px;
  line-height: 50px;
  margin: 0 12px 0 12px;
  cursor: pointer;
}
.tool-btn-hover-red:hover {
  color: red;
}
.tool-btn-hover-blue:hover {
  color: #1890ff;
}
.tool-divider {
  position: relative;
  top: -6px;
  height: 50px;
  width: 2px;
  font-size: 10px;
  background: #b7b7b7;
}
.log-view {
  position: relative;
  height: 100%;
}
</style>
