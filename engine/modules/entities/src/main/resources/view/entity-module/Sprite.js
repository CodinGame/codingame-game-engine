import { SpriteBasedEntity } from './SpriteBasedEntity.js'

/* global PIXI */

export class Sprite extends SpriteBasedEntity {
  initDisplay () {
    super.initDisplay()
    if (this.defaultState.image === null) {
      this.graphics = new PIXI.Sprite(PIXI.Texture.EMPTY)
    } else {
      this.graphics = PIXI.Sprite.from(this.defaultState.image)
    }
  }
}
