/**
 * A few transition functions contained within [0,1] on both axes
 */

/**
 * Traces a bell shaped curve
 */
export function bell (x) {
  return 1 - Math.pow((x - 0.5) * 2, 2)
}

/**
 * The output value quickly increases and wobbles around 1 a little before settling.
 */
export function elastic (t) {
  let b = 0
  let c = 1
  let d = 1
  let s = 1.70158
  let p = 0
  let a = c
  if (t === 0) { return b }
  if ((t /= d) === 1) { return b + c }
  if (!p) { p = d * 0.3 }
  if (a < Math.abs(c)) {
    a = c
    s = p / 4
  } else { s = p / (2 * Math.PI) * Math.asin(c / a) }
  return a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b
}

/**
 * The output value slowly increases and decreases
 */
export function ease (t) {
  return t < 0.5
    ? 2 * t * t
    : -1 + (4 - 2 * t) * t
}

export const easeIn = t => t**3

export const easeOut = t => 1 + (t - 1)**3