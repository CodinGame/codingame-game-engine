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

    pushDuplicateErrors();
  }

  static refreshContent () {}

  static defineToggle(option) {
	  checkDuplicates(option);

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
ToggleModule.duplicateErrors = {}

function createIfNull(obj, key, value) {
  obj[key] = obj[key] || value
}

function insertNewDuplicate(dups, v, key) {
  createIfNull(dups, v.key, { keys: [], value: v.value })

  if (!dups[v.key].keys.includes(v.key)) {
    dups[v.key].keys.push(v.key)
  }
  dups[v.key].keys.push(key)
}

function checkDuplicates(option) {
  var values = []

  ToggleModule.duplicateErrors[option.toggle] = {}

	for (const key in option.values) {
    const value = option.values[key]
    const v = values.find(elem => elem.value == value)

    if (v) {
      insertNewDuplicate(ToggleModule.duplicateErrors[option.toggle], v, key)
    } else {
      values.push({ key: key, value: value })
    }
  }
}

function pushDuplicateErrors() {
  for (const toggle in ToggleModule.duplicateErrors) {
    for (const dup of Object.values(ToggleModule.duplicateErrors[toggle])) {
      ErrorLog.push(new DuplicateToggleValueError(toggle, dup))
    }
  }
}