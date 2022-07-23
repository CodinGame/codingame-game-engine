// noinspection JSUnusedGlobalSymbols

import {api as entityModule} from '../entity-module/GraphicEntityModule.js'

/* when the mouse hover an entity */
function getMouseOverFunc(id, module) {
    return function () {
        module.inside[id] = true
    }
}

/*
when the mouse leave an entity
 */
function getMouseOutFunc(id, module) {
    return function () {
        delete module.inside[id]
        if (module.currently_displayed.has(id)) {
            module.currently_displayed.delete(id)
            for (let display_id of module.currentFrame.registered[id]) {
                const display_entity = entityModule.entities.get(display_id)
                display_entity.container.visible = false
            }
        }
    }
}

/*
Stuff copied from Tooltip module
 */
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

/*
Function called everytime the mouse moves
 */
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
            if (DisplayOnHoverModule.allow_multiple_display) {
                for (let show of showing) {
                    const entity = entityModule.entities.get(show)
                    const state = getEntityState(entity, module.currentFrame.number)
                    if (state !== null && module.currentFrame.registered[id] !== null) {
                        module.currently_displayed.add(show)
                        for (let display_id of module.currentFrame.registered[show]) {
                            const display_entity = entityModule.entities.get(display_id)
                            display_entity.container.visible = true
                        }
                    }
                }
            } else {
                const to_remove = []
                for (let id of module.currently_displayed) {
                    const register = module.currentFrame.registered[id]
                    if (register.length === 0) {
                        to_remove.push(id)
                        delete module.inside[id]
                    }
                }
                for (let id of to_remove) {
                    module.currently_displayed.delete(id)
                }
                if (module.currently_displayed.size === 0) {
                    const available = showing.filter(id =>
                        getEntityState(entityModule.entities.get(id), module.currentFrame.number) !== null
                        && module.currentFrame.registered[id] !== null)
                    if (available.length) {
                        const min = Math.min(...available)
                        module.currently_displayed.add(min)
                        for (let display_id of module.currentFrame.registered[min]) {
                            const display_entity = entityModule.entities.get(display_id)
                            display_entity.container.visible = true
                        }
                    }
                }
            }
        }
    }
}

export class DisplayOnHoverModule {
    constructor(assets) {
        this.interactive = {}
        this.previousFrame = {
            registered: {}
        }
        this.inside = {}
        this.currently_displayed = new Set()
        this.lastProgress = 1
        this.lastFrame = 0
    }

    static get name() {
        return 'h'
    }

    /*
    render scene
     */
    updateScene(previousData, currentData, progress) {
        this.currentFrame = currentData
        this.currentProgress = progress
        for (let id of this.currently_displayed) {
            for (let display_id of this.currentFrame.registered[id]) {
                const display_entity = entityModule.entities.get(display_id)
                display_entity.container.visible = true
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

/*
Set to true if you want to allow multiple display (if the mouse hovers more than one entity)
 */
DisplayOnHoverModule.allow_multiple_display = false