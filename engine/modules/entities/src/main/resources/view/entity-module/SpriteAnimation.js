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
      animationProgress: 0,
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
      const images = state.images.split(',')

      if (state.animationProgress >= 0) {
        const currentDate = frame.date + frame.frameDuration * progress
        const extrapolatedState = { ...state, date: currentDate }
        this.computeAnimationProgressTime(state, extrapolatedState)

        const animationIndex = Math.floor(images.length * extrapolatedState.animationProgress)
        const image = state.loop ? images[animationIndex % images.length] : (images[animationIndex] || images[images.length - 1])

        try {
          this.graphics.texture = PIXI.Texture.from(image)
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
      currState.animationProgress = 0
    } else {
      currState.animationProgress = prevState.animationProgress
      if (prevState.playing) {
        currState.animationProgress += (currState.date - prevState.date) / prevState.duration
      }
    }
  }
}
