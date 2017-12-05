import {lerp, lerpColor} from "./utils.js";

export const PROPERTIES = {
  default: {
    type: Number,
    lerpMethod: lerp
  },
  color: {
    type: Number,
    lerpMethod: lerpColor
  },
  lineColor: {
    type: Number,
    lerpMethod: lerpColor
  },
  image: {
    type: String
  }
};