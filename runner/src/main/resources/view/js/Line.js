import {Shape} from './Shape.js';
import {lerp, unlerp, lerpColor} from './utils.js';

export default class Line extends Shape {

  static defaultPosition() {
    return 100;
  }
  //TODO: automate all the property operations
  constructor() {
    super();
    this.defaultState.x2 = Line.defaultPosition();
    this.defaultState.y2 = Line.defaultPosition();
  }
  
  initDisplay() {
    super.initDisplay();
  }

  updateDisplay(start, end, t) {
    super.updateDisplay(start, end, t);
    var x2, y2, lineColor, lineWidth;
    if (end.lerp) {
      lineWidth = lerp(start.lineWidth, end.lineWidth, t);
      lineColor = lerpColor(start.lineColor, end.lineColor, t);
      x2 = lerp(start.x2, end.x2, t);
      y2 = lerp(start.y2, end.y2, t);

    } else {
      lineWidth = end.lineWidth;
      lineColor = end.lineColor;
      x2 = end.x2;
      y2 = end.y2;
    }
    if (this.currentState.lineWidth !== lineWidth ||
      this.currentState.lineColor !== lineColor ||
      this.currentState.x2 !== x2 ||
      this.currentState.y2 !== y2) {
      this.graphics.clear();
      this.graphics.lineStyle(lineWidth, lineColor);
      this.graphics.moveTo(0, 0);
      this.graphics.lineTo(-this.container.x + x2, -this.container.y + y2);
      this.currentState.x2 = x2;
      this.currentState.y2 = y2;
      this.currentState.lineWidth = lineWidth;
      this.currentState.lineColor = lineColor;
    }
  }
}
