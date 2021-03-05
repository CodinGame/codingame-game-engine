import { Entity } from './Entity.js'

/* global PIXI */

export class ContainerBasedEntity extends Entity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      children: []
    })
  }

  initDisplay () {
    super.initDisplay()
    this.graphics = new PIXI.Container()
    this.graphics.sortableChildren = true
  }

  updateDisplay (state, changed, globalData) {
    super.updateDisplay(state, changed, globalData)
  }

  get childrenContainer () {
    return this.graphics
  }
}
