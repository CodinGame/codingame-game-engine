import { lerp, lerpColor, lerpAngle } from "./utils.js";

const noLerp = (a, b, u) => b;

const colorOpts = {
  type: Number,
  lerpMethod: lerpColor,
  convert(value, globalData) {
    return value < 0 ? (globalData.players[-(value + 1)].color) : value;
  }
};

const stringOpts = {
  type: String,
  lerpMethod: noLerp
};

const angleOpts = {
  type: Number,
  lerpMethod: lerpAngle
};

export const PROPERTIES = {
  default: {
    type: Number,
    lerpMethod: lerp
  },

  visible: {
    type: Number,
    lerpMethod: noLerp
  },

  rotation: angleOpts,

  fillColor: colorOpts,
  lineColor: colorOpts,
  strokeColor: colorOpts,

  image: stringOpts,
  text: {
    ...stringOpts,
    convert(value, globalData) {
      if (/^\$\d$/.test(value)) {
        return globalData.players[+value[1]].name;
      }
      return value;
    }
  },
  fontFamily: stringOpts,
  children: stringOpts
};

