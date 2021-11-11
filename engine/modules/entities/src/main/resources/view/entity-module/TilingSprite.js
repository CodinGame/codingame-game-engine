import { SpriteBasedEntity } from './SpriteBasedEntity.js'

/* global PIXI */

export class TilingSprite extends SpriteBasedEntity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      tileX: 0,
      tileY: 0,
      tileScaleX: 1,
      tileScaleY: 1
    })
  }

  initDisplay () {
    super.initDisplay()
    this.graphics = new PIXI.TilingSprite(PIXI.Texture.EMPTY)
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)

    this.graphics.tilePosition.x = state.tileX
    this.graphics.tilePosition.y = state.tileY
    this.graphics.tileScale.x = state.tileScaleX
    this.graphics.tileScale.y = state.tileScaleY
  }
}
