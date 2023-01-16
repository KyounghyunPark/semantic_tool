import { createStore } from "vuex";

export default createStore({
  state: {
    userInfo: {
      userId: null
    }
  },
  mutations: {
    setUserInfo(state, userInfo) {
      state.userInfo.userId = userInfo.userId;
    },
    removeUserInfo(state) {
      state.userInfo.userId = null;
    },
  },
  getters: { getUserInfo: (state) => state.userInfo},
  actions: {},
});
