module.exports = {
  configureWebpack: {
    devtool: 'source-map'
  },
  chainWebpack: config => {
    config.plugin('html')
      .tap(args => {
        args[0].title = '贵州农信-脚本启动工具';
        return args;
      })
  },
}