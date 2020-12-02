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
      const bufferBoundsWidth = this.buffer.getBounds().width
      const bufferBoundsHeight = this.buffer.getBounds().height
      if (this.gameTexture == null || this.gameTexture.width < bufferBoundsWidth || this.gameTexture.height < bufferBoundsHeight) {
        const width = Math.min(Math.max(bufferBoundsWidth, 512), 4096)
        const height = Math.min(Math.max(bufferBoundsHeight, 512), 4096)
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
