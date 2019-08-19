import {SpriteBasedEntity} from './SpriteBasedEntity.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {MissingImageError} from './errors/MissingImageError.js'

/* global PIXI */

export class Sprite extends SpriteBasedEntity {
  initDisplay () {
    super.initDisplay()
    if (this.defaultState.image === null) {
      this.graphics = new PIXI.Sprite(PIXI.Texture.EMPTY)
    } else {
      this.graphics = PIXI.Sprite.fromFrame(this.defaultState.image)
    }
  }
}
