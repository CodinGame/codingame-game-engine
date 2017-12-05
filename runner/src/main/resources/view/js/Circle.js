import {Shape} from "./Shape.js";
import {lerp, unlerp, lerpColor} from './utils.js';

export class Circle extends Shape {
  static defaultRadius() {
    return 100;
  }

  constructor() {
    super();
    this.defaultState.radius = Circle.defaultRadius();
  }

  initDisplay() {
    super.initDisplay();
    this.graphics.drawCircle(0, 0, this.defaultState.radius);
    if (this.defaultState.fillColor !== null) {
      this.graphics.endFill();
    }
  }

  updateDisplay(start, end, p) {
    super.updateDisplay(start, end, p);

    const t = end.lerp ? p : 1;

    const lineColor = lerpColor(start.lineColor, end.lineColor, t);
    const lineWidth = lerp(start.lineWidth, end.lineWidth, t);
    const radius = lerp(start.radius, end.radius, t);
    const fillColor = lerpColor(start.fillColor, end.fillColor, t);
    const fillAlpha = lerp(start.fillAlpha, end.fillAlpha, t);
    const lineAlpha = lerp(start.lineAlpha, end.lineAlpha, t);

    if (this.currentState.radius != radius ||
      this.currentState.lineColor != lineColor ||
      this.currentState.lineWidth != lineWidth ||
      this.currentState.fillColor != fillColor ||
      this.currentState.fillAlpha != fillAlpha ||
      this.currentState.lineAlpha != lineAlpha) {
      this.graphics.clear();
      
      if (fillColor !== null) {
        this.graphics.beginFill(fillColor, fillAlpha);
      }
      this.graphics.lineStyle(lineWidth, lineColor);
      this.graphics.drawCircle(0, 0, radius);
      if (fillColor !== null) {
        this.graphics.endFill();
      }
      this.currentState.fillColor = fillColor;
      this.currentState.lineColor = lineColor;
      this.currentState.lineWidth = lineWidth;
      this.currentState.radius = radius;
      this.currentState.fillAlpha = fillAlpha;
      this.currentState.lineAlpha = lineAlpha;
    }
  }
}