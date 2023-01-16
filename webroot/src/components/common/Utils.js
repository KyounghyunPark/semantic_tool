import { number } from "echarts";

let getChartGradientColor = (echarts, type) => {
  let color = null;

  if (typeof type === number) {
    type = type + "";
    type = Number(type.substring(type.length - 1));
  }

  switch (type) {
    case "blue":
    case 0:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#408bff" },
        { offset: 0.5, color: "#2262c6" },
        { offset: 1, color: "#1750ac" },
      ]);
      break;
    case "red":
    case 1:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#ff7c7c" },
        { offset: 0.5, color: "#ff2121" },
        { offset: 1, color: "#a90000" },
      ]);
      break;
    case "orange":
    case 2:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#ffb836" },
        { offset: 0.5, color: "#ffa500" },
        { offset: 1, color: "#a96e01" },
      ]);
      break;
    case "green":
    case 3:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#41e35b" },
        { offset: 0.5, color: "#1cab34" },
        { offset: 1, color: "#037816" },
      ]);
      break;
    case "gray":
    case 4:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#cfcfcf" },
        { offset: 0.5, color: "#9b9b9b" },
        { offset: 1, color: "#6e6e6e" },
      ]);
      break;
    case "pink":
    case 5:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#ffe4e8" },
        { offset: 0.5, color: "#ffc0cb" },
        { offset: 1, color: "#d57888" },
      ]);
      break;
    case "aqua":
    case 6:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#37ffff" },
        { offset: 0.5, color: "#00ffff" },
        { offset: 1, color: "#009f9f" },
      ]);
      break;
    case "blueviolet":
    case 7:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#b46ff5" },
        { offset: 0.5, color: "#8a2be2" },
        { offset: 1, color: "#600caf" },
      ]);
      break;
    case "yellow":
    case 8:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#ffdf36" },
        { offset: 0.5, color: "#ffd700" },
        { offset: 1, color: "#ab9000" },
      ]);
      break;
    case "black":
    case 9:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#a3a3a3" },
        { offset: 0.5, color: "#5a5a5a" },
        { offset: 1, color: "#1c1c1c" },
      ]);
      break;
    case "opacity_black":
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#c7c7c754" },
        { offset: 0.5, color: "#81818154" },
        { offset: 1, color: "#28282854" },
      ]);
      break;
    default:
      color = new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: "#ffffff" },
        { offset: 0.5, color: "#adadad" },
        { offset: 1, color: "#686868" },
      ]);
      break;
  }
  return color;
};

let getUserId = (userInfo) => {
  let userId = null;

  if (userInfo && userInfo.userId) {
    userId = userInfo.userId;
  } else {
    userInfo = JSON.parse(localStorage.getItem("userInfo"));
    if (userInfo && userInfo.userId) {
      userId = userInfo.userId;
    }
  }
  return userId;
};

let getDateFormat = (moment, date, type) => {
  switch (type) {
    case "Datetime":
      return moment(date).format("YYYY-MM-DD HH:mm:ss");
    case "MillisecondDatetime":
      return moment(date).format("YYYY-MM-DD HH:mm:ss.SSS");
    case "Timestamp":
      return moment(date).format("X");
    case "MillisecondTimestamp":
      return moment(date).format("x");
    default:
      return moment(date).format("YYYY-MM-DD");
  }
};

export { getChartGradientColor, getUserId, getDateFormat };
