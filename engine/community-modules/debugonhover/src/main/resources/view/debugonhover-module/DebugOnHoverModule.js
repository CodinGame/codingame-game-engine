// noinspection JSUnusedGlobalSymbols

import {api as entityModule} from '../entity-module/GraphicEntityModule.js'

/* global PIXI */
function getMouseOverFunc(id, module) {
    return function () {
        module.inside[id] = true
    }
}

function getMouseOutFunc(id, module) {
    return function () {
        delete module.inside[id]
        if (module.currently_debugged.has(id)) {
            module.currently_debugged.delete(id)
            for (let debug_id of module.currentFrame.registered[id]) {
                const debug_entity = entityModule.entities.get(debug_id)
                debug_entity.container.visible = false
            }
        }
    }
}

function getEntityCurrentSubStates(entity, frame) {
    if (entity.states[frame]) {
        return entity.states[frame]
    }
    let frameNumbers = Object.keys(entity.states)
    let index = frameNumbers.length - 1

    while (index >= 0 && frameNumbers[index] > frame) {
        index--
    }
    return entity.states[frameNumbers[index]] || []
}

function getEntityState(entity, frame) {
    const subStates = getEntityCurrentSubStates(entity, frame)
    if (subStates && subStates.length) {
        return subStates[subStates.length - 1]
    }
    return null
}

function getMouseMoveFunc(module) {
    return function (ev) {
        const showing = []
        const ids = Object.keys(module.inside).map(n => +n)
        // console.log("ids : ", ids, "inside : ", module.inside)
        for (let id of ids) {
            if (module.inside[id]) {
                const entity = entityModule.entities.get(id)
                const state = entity && getEntityState(entity, module.currentFrame.number)
                if (!state || (entity.container && !entity.container.visible)) {
                    delete module.inside[id]
                } else if (module.currentFrame.registered[id] !== undefined) {
                    showing.push(id)
                }
            }
        }
        if (showing.length) {
            if (DebugOnHoverModule.allow_multiple_debug) {
                for (let show of showing) {
                    const entity = entityModule.entities.get(show)
                    const state = getEntityState(entity, module.currentFrame.number)
                    if (state !== null && module.currentFrame.registered[id] !== null) {
                        module.currently_debugged.add(show)
                        for (let debug_id of module.currentFrame.registered[show]) {
                            const debug_entity = entityModule.entities.get(debug_id)
                            debug_entity.container.visible = true
                        }
                    }
                }
            } else {
                console.log("debugged : ", module.currently_debugged, module.currentFrame.registered[module.currently_debugged[0]])
                const to_remove = []
                for (let id of module.currently_debugged) {
                    const register = module.currentFrame.registered[id]
                    if (register.length === 0){
                        to_remove.push(id)
                        delete module.inside[id]
                    }
                }
                for (let id of to_remove) {
                    module.currently_debugged.delete(id)
                }
                if (module.currently_debugged.size === 0) {
                    const available = showing.filter(id =>
                        getEntityState(entityModule.entities.get(id), module.currentFrame.number) !== null
                            && module.currentFrame.registered[id] !== null)
                    if (available.length) {
                        const min = Math.min(...available)
                        module.currently_debugged.add(min)
                        for (let debug_id of module.currentFrame.registered[min]) {
                            const debug_entity = entityModule.entities.get(debug_id)
                            debug_entity.container.visible = true
                        }
                    }
                }
            }
        }
    }
}

export class DebugOnHoverModule {
    constructor(assets) {
        this.interactive = {}
        this.previousFrame = {
            registered: {}
        }
        this.inside = {}
        this.currently_debugged = new Set()
        this.lastProgress = 1
        this.lastFrame = 0
    }

    static get name() {
        return 'h'
    }

    updateScene(previousData, currentData, progress) {
        this.currentFrame = currentData
        this.currentProgress = progress
        for (let id of this.currently_debugged) {
            for (let debug_id of this.currentFrame.registered[id]) {
                const debug_entity = entityModule.entities.get(debug_id)
                debug_entity.container.visible = true
            }
        }
    }

    handleFrameData(frameInfo, data = []) {
        const newRegistration = data[0] || []
        const registered = {...this.previousFrame.registered, ...newRegistration}

        Object.keys(newRegistration).forEach(
            k => {
                this.interactive[k] = true
            }
        )

        const frame = {registered, number: frameInfo.number}
        this.previousFrame = frame
        return frame
    }

    reinitScene(container) {
        entityModule.entities.forEach(entity => {
            if (this.interactive[entity.id]) {
                entity.container.interactive = true
                entity.container.mouseover = getMouseOverFunc(entity.id, this)
                entity.container.mouseout = getMouseOutFunc(entity.id, this)
            }
        })
        this.container = container
        container.interactive = true
        container.mousemove = getMouseMoveFunc(this)
    }

}

DebugOnHoverModule.allow_multiple_debug = false