<template>
	<div class="home-page">
		<img class="logo" alt="logo" src="../assets/gzrc_logo.png" />
		<div class="main-content">
			<el-input
				:placeholder="commandTypeOpts[commandType - 1].placeholder"
				v-model="command"
				class="input-with-select"
				@click.native="handleSelectFile"
			>
				<el-select v-model="commandType" slot="prepend" @change="handleCommandChange">
					<el-option class="el-option" v-for="opt of commandTypeOpts" :key="opt.value" :label="opt.label" :value="opt.value"> </el-option>
				</el-select>
				<el-button class="input-append-btn" slot="append" type="primary" @click="handleExec">执行</el-button>
			</el-input>

			<input ref="shadowFileInput" v-if="commandType == '3'" type="file" style="display: none" />

			<div class="main-table">
				<el-table :data="tableData">
					<el-table-column prop="path" label="路径" width="350"></el-table-column>
					<el-table-column prop="startTime" label="开始时间" width="200"></el-table-column>
					<el-table-column prop="status" label="状态">
						<template slot-scope="scope">
							<el-tag type="success" effect="dark" size="mini">{{ scope.row.status }}</el-tag>
						</template>
					</el-table-column>
					<el-table-column prop="option" label="操作">
						<template slot-scope="scope">
							<el-button size="mini" type="danger" @click="handleInterrupt(scope, scope.row)">中断</el-button>
						</template>
					</el-table-column>
				</el-table>
			</div>
		</div>
		<el-dialog title="确认信息" :visible.sync="confirmInfoDialog" width="500px" center @closed="isPrintLog = '1'">
			<span class="dialog-label">操作类型：</span><span class="dialog-content">{{ commandTypeOpts[commandType - 1].label }}</span
			><br />
			<span class="dialog-label">文件路径：</span><span class="dialog-content">{{ command }}</span
			><br />
			<span class="dialog-label">是否打印日志：</span>
			<el-radio class="dialog-content" v-model="isPrintLog" label="1">是</el-radio>
			<el-radio class="dialog-content" v-model="isPrintLog" label="0">否</el-radio>
			<span slot="footer">
				<el-button size="small" @click="handleCloseDialog">取消</el-button>
				<el-button size="small" type="primary" @click="handleSubmit">确认</el-button>
			</span>
		</el-dialog>
	</div>
</template>

<script>
export default {
	data() {
		return {
			// command: "ping 10.128.103.45",
			command: "",
			commandType: "1",
			commandTypeOpts: [
				{ label: "执行脚本", value: "1", placeholder: "请输入文件路径或命令..." },
				{ label: "浏览日志", value: "2", placeholder: "请输入日志文件路径..." },
				{ label: "上传文件", value: "3", placeholder: "请选择准备上传的文件..." },
			],
			tableData: [],
			confirmInfoDialog: false,
			isPrintLog: "1",
			pollingTimer: null,
		};
	},
	mounted() {
		this.getExecList();
		var that = this;
		// 三秒轮询一次
		this.pollingTimer = setInterval(function() {
			that.getExecList();
		}, 3000);
	},
	methods: {
		execShell() {
			return this.axios
				.post("http://localhost:8080/shell/exec", {
					userId: "admin",
					shellPath: this.command,
					charser: "gb2312",
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
			if (this.commandType && this.command) {
				this.confirmInfoDialog = true;
			} else {
				this.$message.error("文件路径或命令不能为空！");
			}
		},
		handleInterrupt(scope, row) {
			var that = this;
			this.axios
				.post("http://localhost:8080/shell/interrupt", {
					shellPath: row.path,
				})
				.then((res) => {
					if (res.data.success) {
						that.$message({
							message: res.data.message,
							type: "success",
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
			this.execShell().then((res) => {
				that.handleCloseDialog();
				if (res.success) {
					that.getExecList();
					if (that.isPrintLog == "1") {
						var url = that.$router.resolve({
							path: "/LogView",
						});
						var windowName = "_" + that.command;
						if (window.localStorage.getItem(windowName) == url.href) {
							this.$message.error("该日志消息页面已存在，请勿重复打开！");
						} else {
							this.$message({
								message: res.message,
								type: "success",
							});
							// 延迟0.5秒打开新页面，等待原页面执行完毕
							setTimeout(function() {
								var newPage = window.open(url.href, windowName);
								newPage.onload = function() {
									newPage.document.title = that.command + " 日志消息";
									window.localStorage.setItem(windowName, url.href);
									return false;
								};
							}, 500);
						}
					} else {
						this.$message({
							message: res.message,
							type: "success",
						});
					}
				}
			});
		},
		getExecList() {
			var that = this;
			return this.axios.get("http://localhost:8080/shell/list").then((res) => {
				if (res.data.success) {
					var data = res.data.data;
					var records = [];
					for (var record of data)
						records.push({
							path: record.path,
							startTime: record.startTime,
							status: "运行中",
						});
					that.tableData = records;
				}
			});
		},
		handleSelectFile(e) {
			if (this.commandType == "3" && e.target.tagName == "INPUT") {
				this.$refs.shadowFileInput.click();
			}
		},
		handleCommandChange() {
			this.command = null;
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
	content: "*";
	color: #02ac49;
	position: absolute;
	width: 6px;
	height: 16px;
	background: #02ac49;
	left: 4px;
	top: 3px;
	border-radius: 2px;
}
</style>
