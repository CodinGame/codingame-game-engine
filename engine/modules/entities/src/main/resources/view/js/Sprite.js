import { Entity } from './Entity.js';

export class Sprite extends Entity {
  static defaultAnchor() {
    return 0.5;
  }

  constructor() {
    super();
    Object.assign(this.defaultState, {
      anchorX: Sprite.defaultAnchor(),
      anchorY: Sprite.defaultAnchor(),
      image: null,
      blendMode: PIXI.BLEND_MODES.NORMAL,
      tint: 0xFFFFFF
    });
  }



  initDisplay() {
    super.initDisplay();
    if (this.defaultState.image === null) {
      this.graphics = new PIXI.Sprite(PIXI.Texture.WHITE);
    } else {
      this.graphics = PIXI.Sprite.fromFrame(this.defaultState.image);
    }
    this.graphics.anchor.set(this.defaultState.anchorX, this.defaultState.anchorY);
  }

  updateDisplay(state, changed, globalData) {
    super.updateDisplay(state, changed, globalData);
    if (changed.image) {
        this.graphics.texture = PIXI.Texture.fromFrame(state.image);
    }
    this.graphics.anchor.set(state.anchorX, state.anchorY);
    this.graphics.blendMode = state.blendMode;
    this.graphics.tint = state.tint;
  }
}
