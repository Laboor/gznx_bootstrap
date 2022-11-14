<template>
	<div>
		<div id="uploader" class="wu-example">
			<!--用来存放文件信息-->
			<div id="thelist" class="uploader-list"></div>
			<div class="btns">
				<div id="picker">选择文件</div>
				<input type="file" ref="shadowFileInput" @change="handleAddFile" />
			</div>
			<button @click="getFileList">获取文件列表</button>
			<button @click="handleRemoveFile">删除文件</button>
			<button @click="handleUpload">开始上传</button>
		</div>
	</div>
</template>

<script>
import WebUploader from "webuploader";
import api from "@/api";

export default {
	name: "Uploader",
	data() {
		return {
			uploader: null,
		};
	},
	mounted() {
		this.initWebUpload();
	},
	methods: {
		initWebUpload() {
			WebUploader.Uploader.register({
				// 在文件发送之前执行
				"before-send-file": this.beforeSendFile,
				// 在文件分片（如果没有启用分片，整个文件被当成一个分片）后，上传之前执行
				"before-send": this.beforeSend,
				// 在文件所有分片都上传完后，且服务端没有错误返回后执行
				"after-send-file": this.afterSendFile,
			});
			this.uploader = WebUploader.create({
				auto: false, // 选完文件后，是否自动上传
				server: process.env.VUE_APP_HOST + api.uploadFile, // 文件接收服务端
				method: "POST", // 上传方法
				threads: 3, // 上传并发数
				fileNumLimit: 5, // 上传文件数限制
				formData: { uploadDir: "" }, // 数据荷载
				chunked: true, //分片上传
				chunkSize: 10 * 1024 * 1024, //分片大小 10M
				duplicate: true,
				fileSizeLimit: 50 * 1024 * 1024 * 1024, //50G 验证文件总大小是否超出限制, 超出则不允许加入队列
				fileSingleSizeLimit: 10 * 1024 * 1024 * 1024,
			});
			const fun = ["fileQueued", "uploadStart", "uploadProgress", "uploadSuccess", "error"];
			for (const item of fun) {
				this.uploader.on(item, this[item]);
			}
			return this.uploader;
		},
		beforeSendFile(file) {
			var that = this;
			var deferred = WebUploader.Deferred();
			// 计算文件的唯一标记MD5，用于断点续传
			that.uploader.md5File(file, 0, 3 * 1024 * 1024).then(async function(md5Value) {
				file.md5 = md5Value || "";
				file.uid = WebUploader.Base.guid();
        file.uploadDir = file.source.source.uploadDir;
				// 配置文件上传参数
				that.uploader.option("formData", {
					md5: file.md5,
					uid: WebUploader.Base.guid(),
					uploadDir: file.uploadDir,
				});
				// 判断文件是否上传过，是否存在分片，断点续传
				await that.axios
					.post(api.checkFile, {
						fileName: file.name,
						fileMd5: file.md5,
						uploadDir: file.uploadDir,
					})
					.then((res) => {
						var resultCode = res.data.data;
						if (resultCode == -1) {
							// 文件已经上传过,忽略上传过程，直接标识上传成功；
							that.uploader.skipFile(file);
							file.pass = true;
						} else {
							// 文件没有上传过，下标为0
							// 文件上传中断过，返回当前已经上传到的下标
							file.indexcode = resultCode;
						}
					});
				// 获取文件信息后进入下一步
				deferred.resolve();
			});
			return deferred.promise();
		},
		beforeSend(block) {
			// 获取已经上传过的下标
			var indexchunk = block.file.indexcode;
			var deferred = WebUploader.Deferred();
			if (indexchunk > 0) {
				if (block.chunk > indexchunk) {
					// 分块不存在，重新发送该分块内容
					deferred.resolve();
				} else {
					// 分块存在，跳过
					deferred.reject();
				}
			} else {
				// 分块不存在，重新发送该分块内容
				deferred.resolve();
			}
			return deferred.promise();
		},
		afterSendFile(file) {
			// 如果所有分块上传成功，则通知后台合并分块
			this.axios
				.post(api.mergeFile, {
					fileName: file.name,
					fileMd5: file.md5,
					uploadDir: file.uploadDir,
				})
				.then((res) => {
					console.log(res);
				});
		},
		fileQueued(file) {
			console.log("fileQueued", file);
		},
		uploadStart() {
			console.log("uploadStart");
		},
		uploadProgress(file, percentage) {
			console.log("uploadProgress", file, percentage);
		},
		uploadSuccess(file, res) {
			console.log("uploadSuccess", file, res);
		},
		error(type) {
			console.log("error", type);
		},
		handleAddFile() {
			var file = this.$refs.shadowFileInput.files[0];
			file.uploadDir = "C:\\Users\\DELL\\Desktop\\upload_temp";
			this.uploader.addFiles(file);
		},
		getFileList() {
			var fileList = this.uploader.getFiles();
			console.log(fileList);
			return fileList;
		},
		handleRemoveFile() {
			// var list = this.getFileList();
			this.uploader.removeFile("WU_FILE_0", true);
			console.log(this.uploader.getStats());
		},
		handleUpload() {
			this.uploader.upload();
		},
	},
};
</script>

<style scoped></style>
