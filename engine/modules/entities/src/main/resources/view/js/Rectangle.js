import { Shape } from './Shape.js';

export class Rectangle extends Shape {
  static defaultSideLength() {
    return 100;
  }
  constructor() {
    super();
    Object.assign(this.defaultState, {
      width: Rectangle.defaultSideLength(),
      height: Rectangle.defaultSideLength()
    });
  }


  initDisplay() {
    super.initDisplay();
    this.graphics.drawRect(0, 0, this.defaultState.width, this.defaultState.height);
    this.graphics.endFill();
  }

  updateDisplay(state, changed, globalData) {
    super.updateDisplay(state, changed, globalData);
    if (changed.lineWidth ||
      changed.lineColor ||
      changed.width ||
      changed.height ||
      changed.fillColor) {
      this.graphics.clear();
      if (state.fillColor !== null) {
        this.graphics.beginFill(state.fillColor, state.fillAlpha);
      }
      this.graphics.lineStyle(state.lineWidth, state.lineColor);
      this.graphics.drawRect(0, 0, state.width, state.height);
      if (state.fillColor !== null) {
        this.graphics.endFill();
      }
      
    }
  }
}