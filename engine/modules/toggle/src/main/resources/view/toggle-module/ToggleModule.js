import { api as entityModule } from '../../entity-module/GraphicEntityModule.js'
import { toggles } from '../../config.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {MissingToggleError} from './errors/MissingToggleError.js'

export class ToggleModule {
  constructor (assets) {
    this.previousFrame = {}
    this.missingToggles = {}
    ToggleModule.refreshContent = () => {
      if (!this.currentFrame) {
        return
      }
      for (const registeredEntity in this.currentFrame.registered) {
        const entity = entityModule.entities.get(parseInt(registeredEntity))
        const toggleInfo = this.currentFrame.registered[registeredEntity]
        const toggleState = toggles[toggleInfo.name]

        if (toggleState == null && !this.missingToggles[toggleInfo.name]) {
          ErrorLog.push(new MissingToggleError(toggleInfo.name))
          this.missingToggles[toggleInfo.name] = true
        }
        entity.setHidden(
          toggleState !== toggleInfo.state
        )
      }
    }
  }
  static refreshContent () {}

  static get name () {
    return 'toggles'
  }

  updateScene (previousData, currentData, progress) {
    this.currentFrame = currentData
    this.currentProgress = progress
    ToggleModule.refreshContent()
  }

  handleFrameData (frameInfo, data) {
    if (!data) {
      return
    }
    const newRegistration = data[0]
    const registered = { ...this.previousFrame.registered, ...newRegistration }
    const frame = { registered, number: frameInfo.number }
    this.previousFrame = frame
    return frame
  }

  reinitScene (container, canvasData) {
    ToggleModule.refreshContent()
  }
}
