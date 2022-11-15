<template>
  <div></div>
</template>

<script>
import WebUploader from 'webuploader/dist/webuploader.fis';
import api from '@/api';

export default {
  name: 'Uploader',
  data() {
    return {
      uploader: null,
      fileNumLimit: 5,
      fileSizeLimit: 5 * 1024 * 1024 * 1024,
      fileSingleSizeLimit: 1 * 1024 * 1024 * 1024,
    };
  },
  mounted() {
    this.initWebUpload();
  },
  methods: {
    initWebUpload() {
      WebUploader.Uploader.register({
        // 在文件发送之前执行
        'before-send-file': this.beforeSendFile,
        // 在文件分片（如果没有启用分片，整个文件被当成一个分片）后，上传之前执行
        'before-send': this.beforeSend,
        // 在文件所有分片都上传完后，且服务端没有错误返回后执行
        'after-send-file': this.afterSendFile,
      });
      this.uploader = WebUploader.create({
        auto: false, // 选完文件后，是否自动上传
        server: process.env.VUE_APP_HOST + api.uploadFile, // 文件接收服务端
        method: 'POST', // 上传方法
        threads: 3, // 上传并发数
        fileNumLimit: this.fileNumLimit, // 上传文件数限制
        formData: { uploadDir: '' }, // 数据荷载
        chunked: true, //分片上传
        chunkSize: 5 * 1024 * 1024, //分片大小 5M
        duplicate: true,
        fileSizeLimit: this.fileSizeLimit, // 验证文件总大小是否超出限制, 超出则不允许加入队列
        fileSingleSizeLimit: this.fileSingleSizeLimit, // 单文件大小
      });
      const fun = [
        'fileQueued',
        'fileDequeued',
        'uploadStart',
        'uploadProgress',
        'uploadSuccess',
        'uploadError',
        'error',
        'uploadFinished',
      ];
      for (const item of fun) {
        this.uploader.on(item, this[item]);
      }
      return this.uploader;
    },
    beforeSendFile(file) {
      var that = this;
      var deferred = WebUploader.Deferred();
      // 计算文件的唯一标记MD5，用于断点续传
      that.uploader
        .md5File(file, 0, 3 * 1024 * 1024)
        .then(async function(md5Value) {
          file.md5 = md5Value || '';
          file.uid = WebUploader.Base.guid();
          // 配置文件上传参数
          that.uploader.option('formData', {
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
      this.$emit('file-queued', file);
    },
    fileDequeued(file) {
      this.$emit('file-dequeued', file);
    },
    uploadStart(file) {
      this.$emit('start', file);
    },
    uploadProgress(file, percentage) {
      this.$emit('progress', file, percentage);
    },
    uploadSuccess(file, res) {
      this.$emit('success', file, res);
    },
    uploadError(file, reason) {
      this.$emit('upload-error', file, reason);
    },
    error(type) {
      var msg;
      if (type == 'Q_EXCEED_NUM_LIMIT') {
        msg = '添加的文件数量超出' + this.fileNumLimit + '个';
      } else if (type == 'Q_EXCEED_SIZE_LIMIT') {
        msg =
          '添加的文件总大小超过' +
          WebUploader.Base.formatSize(this.fileSizeLimit);
      } else if (type == 'F_EXCEED_SIZE') {
        msg =
          '添加的文件大小超过' +
          WebUploader.Base.formatSize(this.fileSingleSizeLimit);
      } else {
        msg = type;
      }
      this.$emit('add-file-error', msg);
    },
    uploadFinished() {
      this.$emit('finished');
    },
    getFiles(status) {
      return status ? this.uploader.getFiles(status) : this.uploader.getFiles();
    },
    addFiles(file) {
      this.uploader.addFiles(file);
    },
    removeFile(file) {
      file = this.uploader.getFiles()[0];
      this.uploader.removeFile(file, true);
    },
    upload(file) {
      file ? this.uploader.upload(file) : this.uploader.upload();
    },
    retry(file) {
      file ? this.uploader.retry(file) : this.uploader.retry();
    },
    stop(file) {
      file === true ? this.uploader.stop(true) : this.uploader.stop(file);
    },
    cancelFile(file) {
      this.uploader.cancelFile(file);
      this.axios.post(api.cancelUpload, {
        uploadDir: file.uploadDir,
        fileMd5: file.md5,
      });
    },
  },
  destroyed() {
    if (this.uploader) {
      this.uploader.destroy();
    }
  },
};
</script>
