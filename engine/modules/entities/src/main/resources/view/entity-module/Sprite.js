import { TextureBasedEntity } from './TextureBasedEntity.js';
import { ErrorLog } from '../core/ErrorLog.js';
import { MissingImageError } from './errors/MissingImageError.js';

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
      try {
        this.graphics.texture = PIXI.Texture.fromFrame(state.image);
      } catch (error) {
        ErrorLog.push(new MissingImageError(state.image, error));
      }
    }
  }
}
