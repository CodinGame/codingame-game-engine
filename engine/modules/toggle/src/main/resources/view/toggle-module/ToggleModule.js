import { api as entityModule } from '../entity-module/GraphicEntityModule.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {MissingToggleError} from './errors/MissingToggleError.js'
import {DuplicateToggleValueError} from './errors/DuplicateToggleValueError.js'

export class ToggleModule {
	   
  constructor (assets) {
    this.previousFrame = {}
    this.missingToggles = {}
    
    ToggleModule.refreshContent = () => {
      if (!this.currentFrame || !ToggleModule.toggles) {
        return
      }
      for (const registeredEntity in this.currentFrame.registered) {
        const entity = entityModule.entities.get(parseInt(registeredEntity))
        const toggleInfo = this.currentFrame.registered[registeredEntity]
        const toggleState = ToggleModule.toggles[toggleInfo.name]
        
        if (toggleState == null && !this.missingToggles[toggleInfo.name]) {
          ErrorLog.push(new MissingToggleError(toggleInfo.name))
          this.missingToggles[toggleInfo.name] = true
        }
        entity.setHidden(
          toggleState !== toggleInfo.state
        )
      }
    }
    
    for (const e of ToggleModule.errors) {
    	ErrorLog.push(e)
    }
    
  }

  static refreshContent () {}

  static defineToggle(option) {
	  if (new Set(Object.values(option.values)).size != Object.values(option.values).length) {
		  ToggleModule.errors.push(new DuplicateToggleValueError(option.toggle))
	  }

	  ToggleModule.toggles[option.toggle] = option.default
	  option.get = () => ToggleModule.toggles[option.toggle]
	  option.set = (value) => {
		  ToggleModule.toggles[option.toggle] = value
		  ToggleModule.refreshContent()
	  }
	  return option
  }

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

ToggleModule.toggles = {}
ToggleModule.errors = []
