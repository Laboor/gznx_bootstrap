<template>
  <div>
    <el-popover placement="right" width="400" trigger="click" title="历史记录">
      <ol class="history-list">
        <li v-for="item of historyList" :key="item" class="history-item">
          {{ item }}
        </li>
      </ol>
      <img
        class="history-icon"
        alt="历史记录"
        src="../../assets/history.svg"
        slot="reference"
      />
    </el-popover>
  </div>
</template>

<script>
export default {
  name: 'HistoryList',
  data() {
    return {
      historyList: [],
    };
  },
  mounted() {
    for (var i = 0; i < 30; i++) {
      this.historyList.push('C:/Users/Administrator/Desktop/upload_test');
    }
  },
  methods: {},
};
</script>

<style scoped>
.history-icon {
  height: 20px;
  width: 20px;
  padding: 5px;
  border-radius: 5px;
  box-shadow: 0 0 2px 2px #e7e7e7;
  opacity: 0.4;
}
.history-icon:hover {
  opacity: 0.8;
}
.history-list {
  list-style-type: none;
  height: 200px;
  overflow: auto;
  padding: 5px;
}
.history-item {
  display: inline-block;
  cursor: pointer;
  line-height: 23px;
  background: #ffffff;
  padding: 5px;
  width: 95%;
  color: #999999;
}
.history-item:hover {
  color: #02ac49;
  background: #c5efd675;
  border-radius: 3px;
}
</style>
