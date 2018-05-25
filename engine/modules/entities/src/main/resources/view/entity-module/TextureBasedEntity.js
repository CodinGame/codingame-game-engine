import { Entity } from './Entity.js'

/* global PIXI */

export class TextureBasedEntity extends Entity {
  static defaultAnchor () {
    return 0
  }

  constructor () {
    super()
    Object.assign(this.defaultState, {
      anchorX: TextureBasedEntity.defaultAnchor(),
      anchorY: TextureBasedEntity.defaultAnchor(),
      blendMode: PIXI.BLEND_MODES.NORMAL,
      tint: 0xFFFFFF
    })
  }

  initDisplay () {
    super.initDisplay()
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    this.graphics.anchor.set(state.anchorX, state.anchorY)
    this.graphics.blendMode = state.blendMode
    this.graphics.tint = state.tint
  }
}
