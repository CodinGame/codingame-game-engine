import { lerp, unlerp, lerpColor } from './utils.js';

export class Entity {
  constructor() {
    this.defaultState = {
      x: 0,
      y: 0,
      scaleX: 1,
      scaleY: 1,
      zIndex: 0,
      alpha: 1
    };

    this.states = {};
  }

  init() {
    this.initDisplay();
    this.currentState = Object.assign({}, this.defaultState);
    this.container.addChild(this.graphics);
  }

  addState(t, params, frame, withLerp) {
    if (!this.states[frame]) {
      this.states[frame] = [];
    }

    let state = Object.assign({ t: t, lerp: withLerp }, params);

    if (this.states[frame].find(v => v.t === t)) {
      throw new Exception('Different updates for same t ' + t);
    } else {
      this.states[frame].push(state);
    }
  }
  update(t, params, frame) {
    this.addState(t, params, frame, true);
  }
  set(t, params, frame) {
    this.addState(t, params, frame, false);
  }

  render(frame, progress, data) {
    let subframes = this.states[data.number];
    this.graphics.visible = false;
    if (subframes.length) {

      let index = 0;
      while (index < subframes.length - 1 && subframes[index].t < progress) {
        index++;
      }
      let start = subframes[index - 1];
      let end = subframes[index];
      var t;
      if (!start) {
        // The start frame must be at the end of the previous turn
        var prev = this.states[data.previous.number];
        start = prev[prev.length - 1];
        t = unlerp(0, end.t, progress);
        if (!start && progress >= end.t) {
          start = end;
        }
      } else {
        t = unlerp(start.t, end.t, progress);
      }
      if (start) {
        this.graphics.visible = true;
        this.updateDisplay(start, end, t);
      }
    }
  }

  updateDisplay(start, end, p) {
    this.container.zIndex = start.zIndex;
    let t = end.lerp ? p : 1;

    this.container.alpha = lerp(start.alpha, end.alpha, t);
    this.container.x = lerp(start.x, end.x, t);
    this.container.y = lerp(start.y, end.y, t);
    this.container.scale.x = lerp(start.scaleX, end.scaleX, t);
    this.container.scale.y = lerp(start.scaleY, end.scaleY, t);
  }


  initDisplay() {
    this.container = new PIXI.Container();
    this.container.zIndex = 0;
  }
}