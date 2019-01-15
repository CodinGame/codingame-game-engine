import {TextureBasedEntity} from './TextureBasedEntity.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {MissingImageError} from './errors/MissingImageError.js'

/* global PIXI */

export class Sprite extends TextureBasedEntity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      image: null,
      baseWidth: null,
      baseHeight: null
    })
    this.missingTextures = {}
  }

  initDisplay () {
    super.initDisplay()
    if (this.defaultState.image === null) {
      this.graphics = new PIXI.Sprite(PIXI.Texture.EMPTY)
    } else {
      this.graphics = PIXI.Sprite.fromFrame(this.defaultState.image)
    }
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    if (changed.image) {
      try {
        if (state.image !== null) {
          this.graphics.texture = PIXI.Texture.fromFrame(state.image)
        } else {
          this.graphics.texture = PIXI.Texture.EMPTY
        }
      } catch (error) {
        if (!this.missingTextures[state.image]) {
          this.missingTextures[state.image] = true
          ErrorLog.push(new MissingImageError(state.image, error))
        }
      }
    }
    if (changed.baseWidth) {
      this.graphics.width = state.baseWidth
    }
    if (changed.baseHeight) {
      this.graphics.height = state.baseHeight
    }
  }
}
