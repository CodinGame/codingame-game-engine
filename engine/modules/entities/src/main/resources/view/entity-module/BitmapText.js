import { Entity } from './Entity.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {MissingBitmapFontError} from './errors/MissingBitmapFontError.js'
import { TextureBasedEntity } from './TextureBasedEntity.js'

/* global PIXI */

export class BitmapText extends Entity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      text: '',
      fontSize: 26,
      fontFamily: null,
      anchorX: TextureBasedEntity.defaultAnchor(),
      anchorY: TextureBasedEntity.defaultAnchor(),
      blendMode: PIXI.BLEND_MODES.NORMAL,
      tint: 0xFFFFFF
    })
    this.missingFonts = {}
  }

  initDisplay () {
    super.initDisplay()
    this.graphics = new PIXI.Container()
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    if (state.fontFamily !== null) {
      if (PIXI.extras.BitmapText.fonts[state.fontFamily]) {
        if (this.graphics.children.length === 0) {
          this.displayed = new PIXI.BitmapText(state.text, {
            font: {size: state.fontSize || 1, name: state.fontFamily}
          })
          this.graphics.addChild(this.displayed)
        } else {
          this.displayed.text = state.text
          this.displayed.font = { size: state.fontSize || 1, name: state.fontFamily }
        }
        this.displayed.anchor.set(state.anchorX, state.anchorY)
        this.displayed.blendMode = state.blendMode
        this.displayed.tint = state.tint
      } else {
        if (!this.missingFonts[state.fontFamily]) {
          this.missingFonts[state.fontFamily] = true
          ErrorLog.push(new MissingBitmapFontError(state.fontFamily))
        }
        this.graphics.removeChildren()
      }
    } else {
      this.graphics.removeChildren()
    }
  }
}
