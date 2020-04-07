import { lerp, lerpColor, lerpAngle } from '../core/utils.js'
import { ErrorLog } from '../core/ErrorLog.js'
import { InvalidPlayerColorToken } from './errors/InvalidPlayerColorToken.js'

const noLerp = (a, b, u) => u < 1 ? a : b
const timeLerp = (a, b, u) => b < a ? b : lerp(a, b, u)

const colorOpts = {
  type: Number,
  lerpMethod: lerpColor,
  convert (value, globalData) {
    const playerIdx = -(value + 1)

    try {
      return value < 0 ? (globalData.players[playerIdx].color) : value
    } catch (error) {
      ErrorLog.push(new InvalidPlayerColorToken(playerIdx))
      return 0xffffff
    }
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
const timeOpts = {
  type: Number,
  lerpMethod: timeLerp
}

export const PROPERTIES = {
  default: {
    type: Number,
    lerpMethod: lerp
  },

  visible: boolOpts,

  rotation: angleOpts,
  skewX: angleOpts,
  skewY: angleOpts,

  fillColor: colorOpts,
  lineColor: colorOpts,
  strokeColor: colorOpts,
  tint: colorOpts,

  animationProgressTime: timeOpts,

  maxWidth: constOpts,
  mask: constOpts,
  baseWidth: constOpts,
  baseHeight: constOpts,
  image: stringOpts,
  images: stringOpts,
  scaleMode: stringOpts,
  restarted: {
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
  playing: boolOpts,
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
        if (match[1] < globalData.players.length) {
          res += value.substring(prevIdx, match.index)
          res += globalData.players[+match[1]].name
        }
        prevIdx = match.index + match[0].length
        match = regexp.exec(value)
      }
      res += value.substring(prevIdx)
      return res
    }
  },
  textAlign: {
    ...stringOpts,
    convert: (value) => (
      {
        0: 'left',
        1: 'center',
        2: 'right'
      }[value]
    )
  },
  fontFamily: stringOpts,
  fontWeight: stringOpts,
  children: {
    ...stringOpts,
    convert (value) {
      return value ? value.split(',').map(id => +id) : []
    }
  },

  points: {
    ...stringOpts,
    convert (value) {
      if (!value) {
        return []
      }
      return value.split(',').map(v => parseInt(v))
    },
    lerpMethod: (a, b, u) => {
      if (a.length === b.length) {
        return a.map((v, idx) => lerp(v, b[idx], u))
      }
      return noLerp(a, b, u)
    }
  }
}

function unescape (text) {
  // replace \' by '
  const unescaped = text.split("\\'").join("'")

  if (unescaped.includes(' ') || unescaped.includes(';') || unescaped.includes('\n')) {
    return unescaped.slice(1, unescaped.length - 1)
  } else {
    return unescaped
  }
}
