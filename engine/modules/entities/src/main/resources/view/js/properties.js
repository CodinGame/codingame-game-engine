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
  tint: colorOpts,

  image: stringOpts,
  text: {
    ...stringOpts,
    convert(value, globalData) {
      const regexp = /\$(\d)/g;
      let stop = false;
      let match;
      let res = '';
      let prevIdx = 0;
      while (match = regexp.exec(value)) {
        res += value.substring(prevIdx, match.index);
        res += globalData.players[+match[1]].name;
        prevIdx = match.index + match[0].length;
      }
      res += value.substring(prevIdx);
      return res;
    }
  },
  fontFamily: stringOpts,
  children: stringOpts
};

