import { Entity } from './Entity.js'

/* global PIXI */

export class Shape extends Entity {
  static defaultLineWidth () {
    return 0
  }

  static defaultFillColor () {
    return 0xffffff
  }

  static defaultLineColor () {
    return 0x0
  }

  static defaultLineAlpha () {
    return 1
  }

  static defaultFillAlpha () {
    return 1
  }

  constructor () {
    super()
    Object.assign(this.defaultState, {
      fillColor: Shape.defaultFillColor(),
      lineWidth: Shape.defaultLineWidth(),
      lineColor: Shape.defaultLineColor(),
      fillAlpha: Shape.defaultFillAlpha(),
      lineAlpha: Shape.defaultLineAlpha(),
      blendMode: PIXI.BLEND_MODES.NORMAL
    })
  }

  initDisplay () {
    super.initDisplay()
    this.graphics = new PIXI.Graphics()
    this.graphics.lineStyle(this.defaultState.lineWidth, this.defaultState.lineColor, this.defaultState.lineAlpha)
    if (this.defaultState.fillColor !== null) {
      this.graphics.beginFill(this.defaultState.fillColor, this.defaultState.fillAlpha)
    }
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    this.graphics.blendMode = state.blendMode
  }
}
