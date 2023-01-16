// import { h, resolveComponent } from "vue";
import { createRouter, createWebHistory } from "vue-router";
import store from "@/store.js";

const requireAuth = () => async (to, from, next) => {
  let userInfo = store.getters.getUserInfo;

  if (userInfo && userInfo.userId) {
    return next();
  } else {
    userInfo = JSON.parse(localStorage.getItem("userInfo"));

    if (userInfo && userInfo.userId) {
      return next();
    } else {
      return next("login");
    }
  }
};

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/Login.vue"),
  },
  {
    path: "/",
    name: "Main",
    redirect: "/twinAllList",
    component: () => import("@/layout/MainLayout.vue"),
    children: [
      {
        path: "/twinAllList",
        name: "TwinAllList",
        component: () => import("@/views/twinAllList/TwinAllList.vue"),
        beforeEnter: requireAuth(),
      },
      {
        path: "/sparqlList",
        name: "SparqlList",
        component: () => import("@/views/sparqlList/SparqlList.vue"),
        beforeEnter: requireAuth(),
      },
      {
        path: "/detailInformation/:id",
        name: "DetailInformation",
        component: () => import("@/views/detailInformation/DetailInformation.vue"),
        beforeEnter: requireAuth(),
      }
    ],
  },
  {
    path: "/:catchAll(.*)",
    name: "ErrorPages",
    component: () => import("@/components/error/ErrorPage.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    // always scroll to top
    return { top: 0 };
  },
});
export default router;
