import { Group } from './Group.js'
import { shared } from '../core/shared.js'
import { WIDTH, HEIGHT } from '../core/constants.js'

/* global PIXI */

export class BufferedGroup extends Group {
  initDisplay () {
    super.initDisplay()
    this.gameTexture = PIXI.RenderTexture.create(WIDTH, HEIGHT)
    this.graphics = new PIXI.Sprite(this.gameTexture)
    this.buffer = new PIXI.Container()
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    shared.renderer.render(this.buffer, this.gameTexture)
  }

  postUpdate () {
    shared.renderer.render(this.buffer, this.gameTexture)
  }

  get childrenContainer () {
    return this.buffer
  }
}
