import { TextureBasedEntity } from './TextureBasedEntity.js';

export class Sprite extends TextureBasedEntity {

  constructor() {
    super();
    Object.assign(this.defaultState, {
      image: null
    });
  }



  initDisplay() {
    super.initDisplay();
    if (this.defaultState.image === null) {
      this.graphics = new PIXI.Sprite(PIXI.Texture.WHITE);
    } else {
      this.graphics = PIXI.Sprite.fromFrame(this.defaultState.image);
    }
  }

  updateDisplay(state, changed, globalData) {
    super.updateDisplay(state, changed, globalData);
    if (changed.image) {
        this.graphics.texture = PIXI.Texture.fromFrame(state.image);
    }
  }
}
