import { unlerp } from '../core/utils.js'
import { PROPERTIES } from './properties.js'
import { ErrorLog } from '../core/ErrorLog.js'

/* global PIXI */

export class Entity {
  constructor () {
    this.defaultState = {
      x: 0,
      y: 0,
      scaleX: 1,
      scaleY: 1,
      skewX: 0,
      skewY: 0,
      zIndex: 0,
      alpha: 1,
      visible: false,
      rotation: 0,
      mask: -1
    }

    this.states = {}
  }

  init () {
    this.properties = Object.keys(this.defaultState)
    this.initDisplay()
    this.currentState = Object.assign({}, this.defaultState)
    if (typeof this.graphics === 'object') {
      this.container.addChild(this.graphics)
    }
  }

  setHidden (hide) {
    this.hide = hide
    this.container.visible = this.currentState.visible && !this.hide
  }

  addState (t, params, frame) {
    if (!this.states[frame]) {
      this.states[frame] = []
    }
    const state = Entity.createState(t, params.values, params.curve)

    const collision = this.states[frame].find(v => v.t === t)
    if (collision && Object.keys(state.curve).length === 0) {
      state.curve = collision.curve
    }
    if (collision) {
      if (!isStateEmpty(state) && !isStateEmpty(collision)) {
        ErrorLog.push(new Error('Different updates for same t ' + t))
      }
      Object.assign(collision, state)
    } else {
      this.states[frame].push(state)
    }
  }

  set (t, params, frame) {
    this.addState(t, params, frame)
  }

  render (progress, data, globalData) {
    const subframes = this.states[data.number]
    this.container.visible = false
    let start = null
    let end
    // This t is used to animate the interpolation
    let t = 1
    if (subframes && subframes.length) {
      let index = 0
      while (index < subframes.length - 1 && subframes[index].t < progress) {
        index++
      }

      start = subframes[index - 1]
      end = subframes[index]

      if (!start) {
        // The start frame must be at the end of a previous turn
        const frameNumbers = Object.keys(this.states)
        let index = frameNumbers.indexOf(data.number.toString()) - 1
        // Failsafe
        if (index === -1) {
          index = frameNumbers.length - 1
        }
        while (index >= 0 && frameNumbers[index] >= data.number) {
          index--
        }
        const prev = this.states[frameNumbers[index]] || []
        start = prev[prev.length - 1]

        // If it didn't exist on the previous turn, don't even animate it
        if (!start && progress >= end.t) {
          start = end
          t = 1
        } else {
          // Interpolate from zero since their is always a substate at t=1 no matter what
          t = unlerp(0, end.t, progress)
        }
      } else {
        t = unlerp(start.t, end.t, progress)
      }
    } else {
      // Look for the most recent state, but don't interpolate?
      const frameNumbers = Object.keys(this.states)
      let index = frameNumbers.length - 1
      while (index >= 0 && frameNumbers[index] > data.number) {
        index--
      }
      const substates = this.states[frameNumbers[index]]

      if (substates != null) {
        start = substates[substates.length - 1]
        end = start
      } else {
        Object.assign(this.currentState, this.defaultState)
      }
    }
    if (start) {
      const changed = {}
      const state = Object.assign({}, this.currentState)
      for (const property of this.properties) {
        const opts = PROPERTIES[property] || PROPERTIES.default
        const lerpMethod = opts.lerpMethod
        const curve = end.curve[property] || (a => a)
        const newValue = lerpMethod(start[property], end[property], curve(t))
        if (newValue !== this.currentState[property]) {
          changed[property] = true
          state[property] = newValue
        }
      }
      this.updateDisplay(state, changed, globalData, data, progress)
      Object.assign(this.currentState, state)
      this.container.visible = this.container._visible
      if (changed.children) {
        globalData.mustResetTree = true
        if (typeof this.postUpdate === 'function') {
          globalData.updatedBuffers.push(this)
        }
      }
      if (changed.mask) {
        globalData.maskUpdates[this.id] = state.mask
      }
      if (Object.keys(changed).length !== 0 && this.parent) {
        this.parent.notifyChange()
      }
    }
  }

  notifyChange () {
    if (this.parent) {
      this.parent.notifyChange()
    }
  }

  initDisplay () {
    this.container = new PIXI.Container()
    this.container.zIndex = this.defaultState.zIndex
    this.container.id = this.id
    this.container._visible = this.defaultState.visible
  }

  updateDisplay (state, changed, globalData) {
    // We don't want to set the scale to exactly zero or PIXI may crash.
    const eps = 1e-8

    this.container.zIndex = state.zIndex
    this.container.alpha = state.alpha
    if (changed.x || changed.y) {
      this.container.position.set(state.x * globalData.toWorldUnits, state.y * globalData.toWorldUnits)
    }
    if (changed.scaleX || changed.scaleY) {
      this.container.scale.set(state.scaleX || eps, state.scaleY || eps)
    }
    this.container.rotation = state.rotation
    this.container._visible = state.visible && !this.hide
    this.container.skew.set(state.skewX, state.skewY)
  }

  static createState (time = 1, values = {}, curve = {}) {
    return {
      t: time,
      ...values,
      curve: curve
    }
  }
}

function isStateEmpty (state) {
  return Object.keys(state).length === Object.keys(Entity.createState()).length
}
