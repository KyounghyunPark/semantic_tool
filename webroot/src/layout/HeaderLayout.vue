<script>
import { mapGetters, mapMutations } from "vuex";

export default {
  name: "HeaderLayout",
  data() {
    return {
      userName: "",
      isMenuFolded: false
    };
  },
  computed: {
    ...mapGetters(["getUserInfo"]),
  },
  mounted() {
    //this.checkLogin();
  },
  methods: {
    ...mapMutations(["setUserInfo", "removeUserInfo"]),
    checkLogin() {
      if (this.getUserInfo && this.getUserInfo.userId) {
        this.userName = this.getUserInfo.userName;
      } else {
        let userInfo = JSON.parse(localStorage.getItem("userInfo"));
        if (userInfo && userInfo.userId) {
          this.userName = userInfo.userName;
          this.setUserInfo({
            id: userInfo.userId,
            name: userInfo.userName,
            description: userInfo.description,
          });
        } else {
          this.userName = "";
          this.$router.push("/login");
        }
      }
    },
    clickMain() {
      this.checkLogin();
    },

    clickFolded() {
      this.isMenuFolded = !this.isMenuFolded;
    }
  },
};
</script>
<template>
  <div id="headerLayout">

  </div>
</template>

<style scoped lang="scss">
#headerLayout {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100%;
  background: #1F2128;
  // border-bottom: 1px solid rgb(199 199 199 / 50%);

}
</style>
