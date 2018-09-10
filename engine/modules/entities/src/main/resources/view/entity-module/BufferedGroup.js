import { Group } from './Group.js'
import { getRenderer, flagForDestructionOnReinit } from '../core/rendering.js'
import { WIDTH, HEIGHT } from '../core/constants.js'

/* global PIXI */

export class BufferedGroup extends Group {
  initDisplay () {
    super.initDisplay()
    this.gameTexture = PIXI.RenderTexture.create(WIDTH, HEIGHT)
    flagForDestructionOnReinit(this.gameTexture)
    this.graphics = new PIXI.Sprite(this.gameTexture)
    this.buffer = new PIXI.Container()
    this.needsRender = true
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
  }

  postUpdate () {
    if (this.needsRender) {
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
