import Vue from 'vue';
import App from './App.vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import router from './router';
import store from './store';
import http from './http';
import VueAxios from 'vue-axios';

// 插件注册
Vue.use(VueAxios, http);
Vue.use(ElementUI);
Vue.config.productionTip = false
Vue.prototype.sub = new Vue();


new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
