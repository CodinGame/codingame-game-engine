import { CameraModule } from './CameraModule.js';
import { ErrorLog } from '../core/ErrorLog.js'
import { DuplicateToggleValueError } from "./errors/DuplicateToggleValueError.js";

export class CameraToggleModule {

    constructor(_assets) {
        this.previousFrame = {}
        this.missingToggles = {}
        this.d = false
        CameraToggleModule.refreshContent = () => {
            this.d = !this.d
            if (CameraToggleModule.toggles.cameraMode) {
                CameraModule.setActive(true)
            } else {
                CameraModule.setActive(false)
            }
        }
        pushDuplicateErrors()
    }

    registerToggle(entity, name, state) {
        this.previousFrame.registered[entity.id] = {
            "name": name,
            "state": state
        }
    }

    static refreshContent() {}

    static defineToggle(option) {
        CameraToggleModule.toggles[option.toggle] = option.default
        option.get = () => CameraToggleModule.toggles[option.toggle]
        option.set = (value) => {
            CameraToggleModule.toggles[option.toggle] = value
            CameraToggleModule.refreshContent()
        }
        return option
    }

    static get moduleName () {
        return 'camera-toggle'
    }

    static get dependencies () {
        return ['camera']
    }
    

    updateScene(previousData, currentData, progress) {
        this.currentFrame = currentData
        this.currentProgress = progress
        CameraToggleModule.refreshContent()
    }

    handleFrameData(frameInfo, data) {
    }

    reinitScene(_container, _canvasData) {
        CameraToggleModule.refreshContent()
    }
}

CameraToggleModule.toggles = {}
CameraToggleModule.optionValues = {}

function pushDuplicateErrors () {
    for (const toggle in CameraToggleModule.optionValues) {
        for (const optionValues of CameraToggleModule.optionValues[toggle]) {
            if (optionValues.keys.length > 1) {
                ErrorLog.push(new DuplicateToggleValueError(toggle, optionValues))
            }
        }
    }
}
