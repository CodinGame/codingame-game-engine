import { Shape } from './Shape.js'

export default class Line extends Shape {
  static defaultPosition () {
    return 100
  }

  constructor () {
    super()
    this.defaultState.x2 = Line.defaultPosition()
    this.defaultState.y2 = Line.defaultPosition()
  }

  initDisplay () {
    super.initDisplay()
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)

    if (changed.lineWidth ||
      changed.lineColor ||
      changed.lineAlpha ||
      changed.x2 ||
      changed.y2 ||
      changed.x ||
      changed.y) {
      this.graphics.clear()
      this.graphics.lineStyle(state.lineWidth, state.lineColor, state.lineAlpha)
      this.graphics.moveTo(0, 0)
      this.graphics.lineTo(-this.container.x + state.x2 * globalData.toWorldUnits, -this.container.y + state.y2 * globalData.toWorldUnits)
    }
  }
}
