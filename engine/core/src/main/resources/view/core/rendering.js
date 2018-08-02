let renderer = null

let toDestroy = []

export function getRenderer () {
  return renderer
}

export function setRenderer (instance) {
  renderer = instance
}

export function destroyFlagged () {
  for (var i = 0, l = toDestroy.length; i < l; ++i) {
    var destroyable = toDestroy[i]
    destroyable.destroy(true)
  }
  toDestroy = []
}

export function flagForDestructionOnReinit (destroyable) {
  toDestroy.push(destroyable)
}
