import { Shape } from './Shape.js';

export class Rectangle extends Shape {
  static defaultSideLength() {
    return 100;
  }
  constructor() {
    super();
    this.currentState.width = this.defaultState.width = Rectangle.defaultSideLength();
    this.currentState.height = this.defaultState.height = Rectangle.defaultSideLength();
  }


  initDisplay() {
    super.initDisplay();
    this.graphics.drawRect(0, 0, this.defaultState.width, this.defaultState.height);
    this.graphics.endFill();
  }

  updateDisplay(start, end, p) {
    super.updateDisplay(start, end, p);

    const t = end.lerp ? p : 1;
    
    let color = lerpColor(start.color, end.color, t);
    let lineWidth = lerp(start.lineWidth, end.lineWidth, t);
    let lineColor = lerpColor(start.lineColor, end.lineColor, t);
    let width = lerp(start.width, end.width, t);
    let height = lerp(start.height, end.height, t);

    if (this.currentState.lineWidth != lineWidth ||
      this.currentState.lineColor != lineColor ||
      this.currentState.width != end.width ||
      this.currentState.height != end.height ||
      this.currentState.color != end.color) {
      this.graphics.clear();
      this.graphics.beginFill(color, this.container.alpha);
      this.graphics.lineStyle(lineWidth, lineColor);
      this.graphics.drawRect(0, 0, width, height);
      this.graphics.endFill();
      this.currentState.width = width;
      this.currentState.height = height;
      this.currentState.lineWidth = lineWidth;
      this.currentState.lineColor = lineColor;
      this.currentState.color = color;
    }
  }
}