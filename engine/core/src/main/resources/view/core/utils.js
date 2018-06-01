/**
 * return word padded to width with char characters
 */
export function paddingString (word, width, char) {
  char = char || ' '
  word = word + ''
  return Array(Math.max(width - word.length + 1, 0)).join(char) + word
}

/**
 * Returns a random integer from [0;a[ if b is null.
 * Returns a random integer from [a;b[ if b is not null.
 */
export function randInt (a, b = null) {
  if (b != null && b > a) { return a + Math.floor(Math.random() * (b - a)) }
  return Math.floor(Math.random() * a)
}

/**
 * Gets the number from [a;b] at percentage u
 */
export function lerp (a, b, u) {
  if (a <= b) {
    return a + (b - a) * u
  } else {
    return b + (a - b) * (1 - u)
  }
}

/**
 * Gets the percentage position in [a;b] of number v
 */
export function unlerpUnclamped (a, b, v) {
  return (v - a) / (b - a)
}

/**
 * Gets the angle between start & end at percentage amount
 */
export function lerpAngle (start, end, amount, maxDelta) {
  while (end > start + Math.PI) { end -= Math.PI * 2 }
  while (end < start - Math.PI) { end += Math.PI * 2 }
  var value
  if (maxDelta !== undefined && Math.abs(end - start) > maxDelta) {
    value = end
  } else {
    value = (start + ((end - start) * amount))
  }
  return (value % (Math.PI * 2))
}

/**
 * Gets the x,y coordinate between 2 points at percentage p
 */
export function lerpPosition (from, to, p) {
  return {
    x: lerp(from.x, to.x, p),
    y: lerp(from.y, to.y, p)
  }
}

/**
 * Gets a color which is the RGB interpolation between start and end by percentage amount
 */
export function lerpColor (start, end, amount) {
  if (end === null) {
    return null
  }
  var from = {
    r: (start & 0xFF0000) >> 16,
    g: (start & 0x00FF00) >> 8,
    b: (start & 0x0000FF)
  }
  var to = {
    r: (end & 0xFF0000) >> 16,
    g: (end & 0x00FF00) >> 8,
    b: (end & 0x0000FF)
  }
  var result = {
    r: lerp(from.r, to.r, amount) << 16,
    g: lerp(from.g, to.g, amount) << 8,
    b: lerp(from.b, to.b, amount)
  }
  return result.r | result.g | result.b
}
/**
 * Gets the percentage position in [a;b] of number v, clamped into [0;1]
 */
export function unlerp (a, b, v) {
  return Math.min(1, Math.max(0, unlerpUnclamped(a, b, v)))
}

/**
 * calls self.push on all elements of arr
 */
export function pushAll (self, arr) {
  self.push.apply(self, arr)
}

/**
 * Returns the scale needed to fit (srcWidth, srcHeight) inside (maxWidth, maxHeight)
 */
export function fitAspectRatio (srcWidth, srcHeight, maxWidth, maxHeight, padding) {
  padding = padding || 0
  return Math.min(maxWidth / (srcWidth + padding), maxHeight / (srcHeight + padding))
}
