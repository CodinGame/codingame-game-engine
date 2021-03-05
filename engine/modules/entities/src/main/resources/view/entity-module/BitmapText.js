import { Entity } from './Entity.js'
import { ErrorLog } from '../core/ErrorLog.js'
import { MissingBitmapFontError } from './errors/MissingBitmapFontError.js'
import { TextureBasedEntity } from './TextureBasedEntity.js'
import { ellipsis } from './textUtils.js'

/* global PIXI */

export class BitmapText extends Entity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      text: '',
      textAlign: 'left',
      fontSize: 26,
      fontFamily: null,
      anchorX: TextureBasedEntity.defaultAnchor(),
      anchorY: TextureBasedEntity.defaultAnchor(),
      blendMode: PIXI.BLEND_MODES.NORMAL,
      tint: 0xFFFFFF,
      maxWidth: 0
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
      if (PIXI.BitmapFont.available[state.fontFamily]) {
        if (this.graphics.children.length === 0) {
          this.displayed = new PIXI.BitmapText('', {
             fontSize: state.fontSize || 1, 
             fontName: state.fontFamily
          })
          this.graphics.addChild(this.displayed)
        } else {
          this.displayed.fontName = state.fontFamily
          this.displayed.fontSize = state.fontSize || 1
        }
        this.displayed.anchor.set(state.anchorX, state.anchorY)
        this.displayed.blendMode = state.blendMode
        this.displayed.tint = state.tint
        this.displayed.align = state.textAlign

        if (changed.text || changed.maxWidth || changed.fontSize ||
            changed.fontFamily) {
          this.displayed.text = state.text
          if (state.maxWidth) {
            ellipsis(this.displayed, state.maxWidth)
          }
        }
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
