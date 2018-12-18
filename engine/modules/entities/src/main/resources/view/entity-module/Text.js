import { TextureBasedEntity } from './TextureBasedEntity.js'

/* global PIXI */

export class Text extends TextureBasedEntity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      text: '',
      strokeColor: 0,
      strokeThickness: 0,
      fillColor: 0,
      fontSize: 26,
      fontFamily: 'Lato',
      fontWeight: 'normal'
    })
  }

  initDisplay () {
    super.initDisplay()

    this.graphics = new PIXI.Text(this.defaultState.text, {
      fontSize: this.defaultState.fontSize + 'px',
      fontFamily: this.defaultState.fontFamily,
      fill: this.defaultState.fillColor
    })
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
    this.graphics.text = state.text
    this.graphics.style.stroke = state.strokeColor
    this.graphics.style.strokeThickness = globalData.atLeastOnePixel(state.strokeThickness)
    this.graphics.style.fill = state.fillColor
    this.graphics.style.fontSize = state.fontSize || 1
    this.graphics.style.fontFamily = state.fontFamily
    this.graphics.style.fontWeight = state.fontWeight
  }
}
