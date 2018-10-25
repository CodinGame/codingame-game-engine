import { TextureBasedEntity } from './TextureBasedEntity.js'
import { ErrorLog } from '../core/ErrorLog.js'
import { MissingImageError } from './errors/MissingImageError.js'
import { unlerp, unlerpUnclamped } from '../core/utils.js'

/* global PIXI */

export class SpriteAnimation extends TextureBasedEntity {
  constructor () {
    super()
    Object.assign(this.defaultState, {
      images: '',
      loop: false,
      duration: 1000,
      playing: true,
      restarted: null,
      animationProgressTime: 0,
      date: 0
    })
  }

  initDisplay () {
    super.initDisplay()
    this.graphics = new PIXI.Sprite(PIXI.Texture.EMPTY)
  }

  addState (t, params, frame, frameInfo) {
    super.addState(t, params, frame)
    const toModify = this.states[frame].find(v => v.t === t)
    const date = frameInfo.date + frameInfo.frameDuration * t
    toModify.date = date
  }

  updateDisplay (state, changed, globalData, frame, progress) {
    super.updateDisplay(state, changed, globalData)

    if (state.images) {
      const duration = state.duration
      const images = state.images.split(',')

      const animationProgress = (state.loop ? unlerpUnclamped : unlerp)(0, duration, state.animationProgressTime)
      if (animationProgress >= 0) {
        const animationIndex = Math.floor(images.length * animationProgress)
        const image = state.loop ? images[animationIndex % images.length] : (images[animationIndex] || images[images.length - 1])
        try {
          this.graphics.texture = PIXI.Texture.fromFrame(image)
        } catch (error) {
          ErrorLog.push(new MissingImageError(image, error))
        }
      }
    } else {
      this.graphics.texture = PIXI.Texture.EMPTY
    }
  }

  computeAnimationProgressTime (prevState, currState) {
    if (currState.restarted && currState.restarted.date === currState.date) {
      currState.animationProgressTime = 0
    } else {
      currState.animationProgressTime = prevState.animationProgressTime
      if (prevState.playing) {
        currState.animationProgressTime += currState.date - prevState.date
      }
    }
  }
}
