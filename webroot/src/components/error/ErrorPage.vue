<script>
export default {
  name: "ErrorPage",
};
</script>

<template>
  <div id="errorPage">
    <div class="mainPanel">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        viewBox="0 0 36 36"
        version="1.1"
        preserveAspectRatio="xMidYMid meet"
      >
        <path
          class="clr-i-solid clr-i-solid-path-1"
          fill="#ff0000"
          d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm-1.49,6a1.49,1.49,0,0,1,3,0v6.89a1.49,1.49,0,1,1-3,0ZM18,25.5a1.72,1.72,0,1,1,1.72-1.72A1.72,1.72,0,0,1,18,25.5Z"
        />
        <rect x="0" y="0" width="100" height="100" fill-opacity="0" />
      </svg>

      <div class="textPanel">Page not found.</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
#errorPage {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  justify-content: center;
  align-items: center;

  .mainPanel {
    display: flex;
    flex-direction: column;
    width: 35vmin;
    height: 20vmin;
    min-width: 350px;
    min-height: 200px;
    background: rgba(255, 255, 255, 0.25);
    box-shadow: 0 8px 32px 0 rgb(31 38 135 / 37%);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    border-radius: 10px;
    border: 1px solid rgba(255, 255, 255, 0.18);

    svg {
      display: flex;
      position: absolute;
      opacity: 0.6;
      width: 7vmin;
      height: 7vmin;
    }

    .textPanel {
      display: flex;
      width: 100%;
      height: 100%;
      justify-content: center;
      align-items: center;
      font-weight: bold;
      font-size: 3.5vmin;
      color: rgba(255, 255, 255, 0.6);
      text-shadow: 1px 1px 0px rgb(255, 0, 0);
    }

    @media screen and (max-width: 1200px) {
      svg {
        width: 60px;
        height: 60px;
      }

      .textPanel {
        font-size: 20px;
      }
    }
  }
}
</style>
