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
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
  }

  postUpdate () {
    getRenderer().render(this.buffer, this.gameTexture)
  }

  get childrenContainer () {
    return this.buffer
  }
}
