import {lerp, lerpColor, lerpAngle} from '../core/utils.js'

const noLerp = (a, b, u) => u < 1 ? a : b

const colorOpts = {
  type: Number,
  lerpMethod: lerpColor,
  convert (value, globalData) {
    return value < 0 ? (globalData.players[-(value + 1)].color) : value
  }
}

const stringOpts = {
  type: String,
  lerpMethod: noLerp,
  convert (value) {
    return unescape(value)
  }
}

const angleOpts = {
  type: Number,
  lerpMethod: lerpAngle,
  convert (value) {
    return value * Math.PI / 180
  }
}

const constOpts = {
  type: Number,
  lerpMethod: noLerp
}

const boolOpts = {
  type: value => {
    return value === '1'
  },
  lerpMethod: noLerp
}

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

  mask: constOpts,
  baseWidth: constOpts,
  baseHeight: constOpts,
  image: stringOpts,
  images: stringOpts,
  started: {
    type: String,
    convert (value, globalData, frameInfo, t) {
      if (value) {
        return {
          date: frameInfo.date + (t * frameInfo.frameDuration)
        }
      }
      return null
    },
    lerpMethod: noLerp
  },

  duration: constOpts,
  blendMode: constOpts,

  loop: boolOpts,

  text: {
    ...stringOpts,
    convert (value, globalData) {
      value = unescape(value)
      const regexp = /\$(\d)/g
      let match = regexp.exec(value)
      let res = ''
      let prevIdx = 0
      while (match) {
        res += value.substring(prevIdx, match.index)
        res += globalData.players[+match[1]].name
        prevIdx = match.index + match[0].length
        match = regexp.exec(value)
      }
      res += value.substring(prevIdx)
      return res
    }
  },
  fontFamily: stringOpts,
  children: {
    ...stringOpts,
    convert (value) {
      return value ? value.split(',').map(id => +id) : []
    }
  }
}

function unescape (text) {
  // replace \' by '
  const unescaped = text.split("\\'").join("'")

  if (unescaped.includes(' ')) {
    return unescaped.slice(1, unescaped.length - 1)
  } else {
    return unescaped
  }
}
