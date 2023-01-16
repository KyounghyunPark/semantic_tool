<script>
import { mapGetters, mapMutations } from "vuex";

export default {
  name: "LoginPage",
  data() {
    return {
      id: "",
      password: "",
    };
  },
  computed: {
    ...mapGetters(["getUserInfo"]),
  },
  mounted() {
    if (this.getUserInfo && this.getUserInfo.id) {
      this.$router.push("/");
    }
  },
  methods: {
    ...mapMutations(["setUserInfo"]),
    login() {
      this.emitter.emit("setLoading", true);

      setTimeout(() => {
        let userInfo = {
            userId: "admin"
        };    
        this.setUserInfo(userInfo);

        localStorage.setItem("userInfo", JSON.stringify(this.getUserInfo));
        this.emitter.emit("setLoading", false);

        this.$router.push("/");
      }, 1200);


      return;
      this.emitter.emit("setLoading", true);

      fetch("/api/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          userId: this.id,
          userPassword: this.password,
        }),
      })
        .then((resp) => resp.json())
        .then((data) => {
          this.emitter.emit("setLoading", false);

          if (data.result) {
            let userInfo = {
              userId: data.data.user_id,
              userName: data.data.user_name,
              description: data.data.description,
            };
            this.setUserInfo(userInfo);
            localStorage.setItem("userInfo", JSON.stringify(this.getUserInfo));
            this.$router.push("/");
          } else {
            console.error(data);

            let errorMsg = data;
            if (data.data) {
              errorMsg = data.data;
            }
            this.emitter.emit("showErrorDialog", errorMsg);
          }
        })
        .catch((err) => {
          this.emitter.emit("setLoading", false);

          console.error(err);
          this.emitter.emit("showErrorDialog", err);
        });
    },
    closeDialog(event) {
      this.receivedIsShowDialog = false;
    },
  },
};
</script>

<template>
  <div id="loginPage">
    <form class="formPanel" @submit.prevent="login">
      <div class="loginPanel">
        <img src="/assets/digital-twin.png" />
        <div class="loginTitle">
          Welcome Back!
        </div>

        <div class="loginSubTitle">
          Let's build something greate
        </div>

        <div style="margin: 30px 0px;">
          <div>
            <label>ID</label>

            <DxTextBox :value="id">
                <DxValidator>
                  <DxRequiredRule message="ID is required"/>
                </DxValidator>
            </DxTextBox>
          </div>
          
          <div style="margin-top: 20px;">
            <label >Password</label>

            <DxTextBox :value="password" mode="password">
                <DxValidator>
                  <DxRequiredRule message="Password is required"/>
                </DxValidator>
            </DxTextBox>
          </div>
        </div>
        

        <DxButton
          id="button"
          :use-submit-behavior="true"
          text="Login"
          style="background-color: #7364db;"
        />

        <div class="copyrightPanel">
          <div>
              <label>Copyright</label>
          </div>

          <div style="margin: 0px 15px;">
            <font-awesome-icon icon="fa-regular fa-copyright" />
          </div>
          
    
          <div fxFlex>
              <label> 2022 ETRI </label>
          </div>
        </div>
      </div>
    </form>
  </div>
</template>

<style scoped lang="scss">
#loginPage {
  display: flex;
  width: 100%;
  height: 100%;

  img {
    width: 100px;
    align-items: center;
    background: white;
    align-self: center;
    border-radius: 10px;
  }

  .loginTitle {
    font-size: 24px;
    font-weight: bold;
    text-align: center;
    margin-top: 20px;
  }

  .loginSubTitle {
    color: #a6a6a6;
    font-size: 13px;
    text-align: center;
  }

  .formPanel {
    display: flex;
    flex-direction: column;
    width: 100%;
    height: 100%;
    justify-content: center;
    align-items: center;
    
    .loginPanel {
      display: flex;
      flex-direction: column;
      width: 300px;
      background: #1F2128;
      border-radius: 10px;
      border: 1px solid #303440;
      padding: 30px;

      .inputPanel {
        display: flex;
        flex-direction: column;
        height: calc(100% - 70px);
        justify-content: center;
        padding: 0 20px 0 20px;
      }

      .btnPanel {
        display: flex;
        flex-direction: row;
        height: 70px;
        border-top: 1px solid #b4b4b4;
        align-items: center;
        justify-content: right;

        button {
          margin-right: 10px;
        }
      }

      .copyrightPanel {
        display: flex;
        flex-direction: row;
        width: 100%;
        align-items: center;
        justify-content: center;
        margin-top: 20px;
        font-size: 14px;

        label {
          font-size: 13px !important;
        }
      }
    }
  }
}
</style>
