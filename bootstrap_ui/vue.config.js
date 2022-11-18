const resolve = require('path').resolve;

module.exports = {
  publicPath: '/bootstrap',
	outputDir: 'dist',
	assetsDir: '',
	indexPath: 'index.html',
	filenameHashing: true,
  productionSourceMap: false,
  runtimeCompiler: true,
  pages: {
		index: {
			entry: 'src/main.js',
			template: 'public/index.html',
			filename: 'index.html',
			title: process.env.VUE_APP_BASE_TITLT,
			favicon: resolve('public/favicon.ico'),
			chunks: ['chunk-vendors', 'chunk-common', 'index'],
		},
	},
  configureWebpack: {
    devtool: 'source-map'
  },
}