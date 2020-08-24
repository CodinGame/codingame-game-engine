import { api as entityModule } from '../entity-module/GraphicEntityModule.js'
import { ErrorLog } from '../core/ErrorLog.js'
import { MissingToggleError } from './errors/MissingToggleError.js'
import { DuplicateToggleValueError } from './errors/DuplicateToggleValueError.js'

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

    pushDuplicateErrors()
  }

  static refreshContent () {}

  static defineToggle (option) {
    checkDuplicates(option)

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
    const newRegistration = {}
    if (data) {
      Object.entries(data).forEach(([key, value]) => {
        value.match(/\d+./g).forEach(m => {
          const entityId = m.slice(0, -1)
          const state = m.slice(-1) === '+'
          newRegistration[entityId] = {
            name: key,
            state: state
          }
        })
      })
    }
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
ToggleModule.optionValues = {}

function checkDuplicates (option) {
  ToggleModule.optionValues[option.toggle] = []

  for (const key in option.values) {
    const value = option.values[key]
    let matchedPair = ToggleModule.optionValues[option.toggle].find(elem => elem.value === value)

    if (!matchedPair) {
      matchedPair = { keys: [], value }
      ToggleModule.optionValues[option.toggle].push(matchedPair)
    }

    matchedPair.keys.push(key)
  }
}

function pushDuplicateErrors () {
  for (const toggle in ToggleModule.optionValues) {
    for (const optionValues of ToggleModule.optionValues[toggle]) {
      if (optionValues.keys.length > 1) {
        ErrorLog.push(new DuplicateToggleValueError(toggle, optionValues))
      }
    }
  }
}
