import { lerp, unlerp, lerpColor } from '../core/utils.js';
import { PROPERTIES } from "./properties.js";

export class Entity {
  constructor() {
    this.defaultState = {
      x: 0,
      y: 0,
      scaleX: 1,
      scaleY: 1,
      zIndex: 0,
      alpha: 1,
      visible: true,
      rotation: 0
    };

    this.states = {};
  }

  init() {
    this.properties = Object.keys(this.defaultState);
    this.initDisplay();
    this.currentState = Object.assign({}, this.defaultState);
    if (typeof this.graphics === 'object') {
      this.container.addChild(this.graphics);
    }

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

  render(progress, data, globalData) {
    const number = data.number;
    let subframes = this.states[data.number];
    this.container.visible = false;
    if (subframes && subframes.length) {

      let index = 0;
      while (index < subframes.length - 1 && subframes[index].t < progress) {
        index++;
      }
      let start = subframes[index - 1];
      let end = subframes[index];
      //This t is used to animate the interpolation 
      let t;

      if (!start) {
        // The start frame must be at the end of the previous turn
        var prev = this.states[data.previous.number] || [];
        start = prev[prev.length - 1];
        
        // If it didn't exist on the previous turn, don't even animate it
        if (!start && progress >= end.t) { 
          start = end;
          t = 1;
        } else {
          // Interpolate from zero since their is always a substate at t=1 no matter what
          t = unlerp(0, end.t, progress); 
        }
      } else {
        t = unlerp(start.t, end.t, progress);
      }
      if (start) {
        const changed = {};
        const state = Object.assign({}, this.currentState);

        for (let property of this.properties) {
          const opts = PROPERTIES[property] || PROPERTIES.default;
          const lerpMethod = opts.lerpMethod;
          const newValue = end.lerp ? lerpMethod(start[property], end[property], t) : end[property];
          if (newValue !== this.currentState[property]) {
            changed[property] = true;
            state[property] = newValue;
          }
        }
        this.updateDisplay(state, changed, globalData, data, progress);
        Object.assign(this.currentState, state);
        this.container.visible = this.container._visible;
        if (changed.children) {
          globalData.mustResetTree = true;
        }
        if (changed.zIndex) {
          globalData.mustResort = true;
        }
      }
    } else {
      Object.assign(this.currentState, this.defaultState);
    }
  }

  initDisplay() {
    this.container = new PIXI.Container();
    this.container.zIndex = this.defaultState.zIndex;
    this.container.id = this.id;
    this.container._visible = this.defaultState.visible;

  }

  updateDisplay(state, changed, globalData) {
    // We don't want to set the scale to exactly zero or PIXI may crash.
    const eps = 1e-8;

    this.container.zIndex = state.zIndex;
    this.container.alpha = state.alpha;
    this.container.position.set(state.x * globalData.coeff, state.y * globalData.coeff);
    this.container.scale.set(state.scaleX ||  eps, state.scaleY ||  eps);
    this.container.rotation = state.rotation;
    this.container._visible = state.visible;
  }
}