import {Entity} from "./Entity.js";

export class Sprite extends Entity {
  static defaultAnchor() {
    return 0.5;
  }

  constructor() {
    super();
    this.defaultState.anchor = Sprite.defaultAnchor();
    this.defaultState.image = null;
  }

  updateDisplay(start, end, t) {
    super.updateDisplay(start, end, t);
    if (this.currentState.image !== end.image) {
      this.graphics.texture = PIXI.Texture.fromFrame(end.image);
      this.currentState.image = end.image;
    }
    if (this.currentState.anchor !== end.anchor) {
      this.graphics.anchor.set(end.anchor);
      this.currentState.anchor = end.anchor;
    }
  }

  initDisplay() {
    super.initDisplay();
    if (this.defaultState.image === null) {
      this.graphics = new PIXI.Sprite(PIXI.Texture.WHITE);
    } else {
      this.graphics = PIXI.Sprite.fromFrame(this.defaultState.image);
    }
    
    this.graphics.anchor.set(this.defaultState.anchor);
  }
}
