import axios from 'axios';

// axios 全局配置
axios.defaults.baseURL = process.env.VUE_APP_HOST;
axios.defaults.timeout = 1000 * 10;
axios.defaults.responseType = 'json';
axios.defaults.withCredentials = true; // 跨域时携带cookies
axios.defaults.validateStatus = (status) => {
  return status >= 200 && status < 300;
};

// 请求拦截器
axios.interceptors.request.use(function (config) {
  // 在发送请求之前做些什么
  // TODO...
  return config;
}, function (error) {
  // 对请求错误做些什么
  // TODO...
  return Promise.reject(error);
});

// 响应拦截器
axios.interceptors.response.use(function (response) {
  // 对响应数据做点什么
  // TODO...
  return response;
}, function (error) {
  // 对响应错误做点什么
  // TODO...
  return Promise.reject(error);
});

export default axios;