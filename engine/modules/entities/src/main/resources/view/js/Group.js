import { Entity } from './Entity.js';

export class Group extends Entity {
  constructor() {
    super();
    Object.assign(this.defaultState, {
      children: []
    });
  }

  initDisplay() {
    super.initDisplay();
    this.graphics = new PIXI.Container();
  }

  updateDisplay(state, changed, globalData) {
    super.updateDisplay(state, changed, globalData);
    
  }
}
