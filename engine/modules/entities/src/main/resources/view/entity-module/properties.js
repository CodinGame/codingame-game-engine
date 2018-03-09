import { lerp, lerpColor, lerpAngle } from "../core/utils.js";

const noLerp = (a, b, u) => a;

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
  lerpMethod: lerpAngle,
  convert(value) {
    return Math.radians(value);
  }
};

const constOpts = {
  type: Number,
  lerpMethod: noLerp
}

const boolOpts = {
  type: value => {
    return value === '1';
  },
  lerpMethod: noLerp
};

export const PROPERTIES = {
  default: {
    type: Number,
    lerpMethod: lerp
  },

  visible: boolOpts,

  rotation: angleOpts,

  fillColor: colorOpts,
  lineColor: colorOpts,
  strokeColor: colorOpts,
  tint: colorOpts,

  baseWidth: constOpts,
  baseHeight: constOpts,
  image: stringOpts,
  images: stringOpts,
  started: {
    type: String,
    convert(value, globalData, frameInfo, t) {
      if (value) {
        return {
          date: frameInfo.date + (t * frameInfo.frameDuration)
        };
      }
      return null;
    },
    lerpMethod: noLerp
  },

  duration: constOpts,
  blendMode: constOpts,

  loop: boolOpts,

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
  children: {
    ...stringOpts,
    convert(value) {
      return value ? value.split(',').map(id => +id) : [];
    }
  }
};

