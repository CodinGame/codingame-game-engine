import { TextureBasedEntity } from './TextureBasedEntity.js'
import { ellipsis } from './textUtils.js'

/* global PIXI */

export class Text extends TextureBasedEntity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      text: '',
      textAlign: 'left',
      strokeColor: 0,
      strokeThickness: 0,
      fillColor: 0,
      fontSize: 26,
      fontFamily: 'Lato',
      fontWeight: 'normal',
      maxWidth: 0
    })
  }

  initDisplay () {
    super.initDisplay()

    this.graphics = new PIXI.Text(this.defaultState.text, {
      align: this.defaultState.textAlign,
      fontSize: this.defaultState.fontSize + 'px',
      fontFamily: this.defaultState.fontFamily,
      fill: this.defaultState.fillColor
    })
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)

    this.graphics.style.align = state.textAlign
    this.graphics.style.stroke = state.strokeColor
    this.graphics.style.strokeThickness = state.strokeThickness
    this.graphics.style.fill = state.fillColor
    this.graphics.style.fontSize = state.fontSize || 1
    this.graphics.style.fontFamily = state.fontFamily
    this.graphics.style.fontWeight = state.fontWeight

    if (changed.text || changed.strokeThickness || changed.fontSize ||
        changed.fontFamily || changed.fontWeight || changed.maxWidth ||
        changed.maxWidth) {
      this.graphics.text = state.text
      if (state.maxWidth) {
        ellipsis(this.graphics, state.maxWidth)
      }
    }
  }
}
