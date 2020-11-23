import { getRenderer, flagForDestructionOnReinit } from '../core/rendering.js'
import { ContainerBasedEntity } from './ContainerBasedEntity.js'

/* global PIXI */

export class BufferedGroup extends ContainerBasedEntity {
  initDisplay () {
    super.initDisplay()
    this.buffer = new PIXI.Container()
    this.gameTexture = null
    this.graphics = new PIXI.Sprite()
    this.needsRender = true
  }

  postUpdate () {
    if (this.needsRender) {
      if (this.gameTexture == null || this.gameTexture.width < this.buffer.width || this.gameTexture.height < this.buffer.height) {
        this.gameTexture = PIXI.RenderTexture.create(Math.min(this.buffer.width, 4096), Math.min(this.buffer.height, 4096))
        flagForDestructionOnReinit(this.gameTexture)
        this.graphics.texture = this.gameTexture
      }

      getRenderer().render(this.buffer, this.gameTexture)
      this.needsRender = false
    }
  }

  get childrenContainer () {
    return this.buffer
  }

  notifyChange () {
    this.needsRender = true
    if (this.parent) {
      this.parent.notifyChange()
    }
  }
}
