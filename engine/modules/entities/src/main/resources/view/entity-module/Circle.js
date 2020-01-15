import { Shape } from './Shape.js'

export class Circle extends Shape {
  static defaultRadius () {
    return 100
  }

  constructor () {
    super()
    this.defaultState.radius = Circle.defaultRadius()
  }

  initDisplay () {
    super.initDisplay()
    this.graphics.drawCircle(0, 0, this.defaultState.radius)
    if (this.defaultState.fillColor !== null) {
      this.graphics.endFill()
    }
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)

    if (changed.radius ||
      changed.lineColor ||
      changed.lineWidth ||
      changed.fillColor ||
      changed.fillAlpha ||
      changed.lineAlpha) {
      this.graphics.clear()

      if (state.fillColor !== null) {
        this.graphics.beginFill(state.fillColor, state.fillAlpha)
      }
      this.graphics.lineStyle(state.lineWidth, state.lineColor)
      this.graphics.drawCircle(0, 0, state.radius * globalData.toWorldUnits)
      if (state.fillColor !== null) {
        this.graphics.endFill()
      }
    }
  }
}
