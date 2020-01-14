import { Shape } from './Shape.js'

export class RoundedRectangle extends Shape {
  static defaultSideLength () {
    return 100
  }

  static defaultRadius () {
    return 20
  }

  constructor () {
    super()
    Object.assign(this.defaultState, {
      width: RoundedRectangle.defaultSideLength(),
      height: RoundedRectangle.defaultSideLength(),
      radius: RoundedRectangle.defaultRadius()
    })
  }

  initDisplay () {
    super.initDisplay()
    this.graphics.drawRoundedRect(0, 0, this.defaultState.width, this.defaultState.height, this.defaultState.radius)
    this.graphics.endFill()
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    if (changed.lineWidth ||
      changed.lineColor ||
      changed.lineAlpha ||
      changed.fillColor ||
      changed.radius ||
      changed.height ||
      changed.width) {
      this.graphics.clear()
      if (state.fillColor !== null) {
        this.graphics.beginFill(state.fillColor, state.fillAlpha)
      }

      this.graphics.lineStyle(state.lineWidth, state.lineColor, state.lineAlpha)
      this.graphics.drawRoundedRect(0, 0,
        state.width * globalData.toWorldUnits,
        state.height * globalData.toWorldUnits,
        state.radius * globalData.toWorldUnits)
      if (state.fillColor !== null) {
        this.graphics.endFill()
      }
    }
  }
}
