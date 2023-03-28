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
      const bufferBounds = this.buffer.getBounds()
      const positiveWidth = Math.max(0, bufferBounds.x + bufferBounds.width)
      const positiveHeight = Math.max(0, bufferBounds.y + bufferBounds.height)

      if (this.gameTexture == null || this.gameTexture.width < positiveWidth || this.gameTexture.height < positiveHeight) {
        const width = Math.min(Math.max(positiveWidth, 512), 4096)
        const height = Math.min(Math.max(positiveHeight, 512), 4096)
        this.gameTexture = PIXI.RenderTexture.create(width, height)
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
