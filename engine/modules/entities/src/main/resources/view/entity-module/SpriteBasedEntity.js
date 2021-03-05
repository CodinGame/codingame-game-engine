import { TextureBasedEntity } from './TextureBasedEntity.js'
import { ErrorLog } from '../core/ErrorLog.js'
import { MissingImageError } from './errors/MissingImageError.js'

/* global PIXI */

export class SpriteBasedEntity extends TextureBasedEntity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      image: null,
      baseWidth: null,
      baseHeight: null,
      scaleMode: 'LINEAR'
    })
    this.missingTextures = {}
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    if (changed.image) {
      try {
        if (state.image !== null) {
          this.graphics.texture = PIXI.Texture.from(state.image)
        } else {
          this.graphics.texture = PIXI.Texture.EMPTY
        }
        this.graphics.texture.baseTexture.scaleMode = PIXI.SCALE_MODES[state.scaleMode]
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
