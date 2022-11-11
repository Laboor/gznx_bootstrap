<template>
	<div
		class="log-container"
		:class="{ [theme]: true }"
		ref="logContainer"
		:style="{ height: containerHeight }"
		@mousedown="handleStopScroll"
		@mouseup="handleAllowScroll"
	>
		<p class="log-message" ref="logMessage" v-html="logMessage"></p>
	</div>
</template>

<script>
export default {
	name: "LogMessage",
	props: {
		url: {
			type: String,
			required: true,
		},
		theme: {
			type: String,
			default: "light",
		},
		suspend: {
			type: Boolean,
			default: false,
		},
	},
	data() {
		return {
			webSocket: null,
			isConnect: false,
			retry: 3,
			logMessage: "",
			isStopScroll: false,
			containerHeight: "800px",
		};
	},
	mounted() {
		this.calcContainerHeight();
		this.createWebSocket();
		var that = this;
		window.onresize = function() {
			that.calcContainerHeight();
		};
		// setInterval(function() {
		// 	that.logMessage += Date.now() + "<br/>";
		// 	if (!that.isStopScroll) {
		// 		that.$refs.logMessage.scrollIntoView(false);
		// 	}
		// }, 100);
	},
	methods: {
		createWebSocket() {
			try {
				if (typeof WebSocket === "undefined") {
					alert("你的浏览器不支持WebSocket!");
					return;
				}
				this.webSocket = new WebSocket(this.url);
				this.initWebSocket();
			} catch (e) {
				console.log("尝试创建websocket连接失败！");
				this.isConnect = false;
				this.reConnect();
			}
		},
		initWebSocket() {
			var that = this;
			this.webSocket.onopen = function() {
				console.log("websocket连接成功！");
				that.isConnect = true;
				that.retry = 3;
				that.$emit("connected");
			};
			this.webSocket.onmessage = function(e) {
				that.webSocketOnMessage(e);
			};
			this.webSocket.onerror = function(e) {
				console.error("websocket连接发生错误！", e);
				that.isConnect = false;
				that.reConnect();
			};
			this.webSocket.onclose = function() {
				console.log("websocket连接已关闭！");
			};
		},
		reConnect() {
			if (this.isConnect || !this.retry) return;
			console.log("正在尝试重新连接...");
			this.retry--;
			var that = this;
			setTimeout(function() {
				that.createWebSocket();
			}, 2000);
		},
		webSocketOnMessage(e) {
			if (this.suspend) return;
			this.logMessage += e.data + " <br/>";
			if (!this.isStopScroll) {
				this.$refs.logMessage.scrollIntoView(false);
			}
		},
		closeWebSocket() {
			try {
				this.webSocket.close();
			} catch (e) {
				console.error("尝试关闭websocket连接失败！", e);
			} finally {
				this.webSocket = null;
				this.isConnect = false;
			}
		},
		clearLogMessage() {
			this.logMessage = "";
		},
		handleStopScroll(e) {
			if (e.target == this.$refs.logContainer) {
				this.isStopScroll = true;
			}
		},
		handleAllowScroll() {
			this.isStopScroll = false;
		},
		calcContainerHeight() {
			this.containerHeight = window.innerHeight + "px";
		},
	},
	destroyed() {
		this.closeWebSocket();
	},
};
</script>

<style scoped>
.log-container {
	position: relative;
	overflow: auto;
}
.light {
	background: white;
	color: #2c3e50;
}
.dark {
	background: black;
	color: green;
}
.log-message {
	text-align: left;
	padding: 20px;
}
</style>
