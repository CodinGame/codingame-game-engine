import {api as entityModule} from '../entity-module/GraphicEntityModule.js'

const BOTH = "B";
const HOVER_ONLY = "H";
const CLICK_ONLY = "C";

const DISPLAY = "D"
const RESIZE = "R"

/**********************************
 Legacy from the tooltip module
 **********************************/
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

/**********************************
 Legacy from the tooltip module
 **********************************/
function getEntityState(entity, frame) {
    const subStates = getEntityCurrentSubStates(entity, frame)
    if (subStates && subStates.length) {
        return subStates[subStates.length - 1]
    }
    return null
}

/**
 * Multiply scale of an entity
 * @param entity the entity to scale
 * @param factor the factor to multiply the scale by
 */
function multiplyScale(entity, factor) {
    entity.container.scale.x = entity.currentState.scaleX * factor
    entity.container.scale.y = entity.currentState.scaleY * factor
}

/**
 * Reset the scale of an entity
 * @param entity the entity to reset
 */
function resetScale(entity) {
    entity.container.scale.x = entity.currentState.scaleX
    entity.container.scale.y = entity.currentState.scaleY
}

/**
 * Perform the transformation for an entity
 * @param entity the entity to transform
 * @param parameters the parameters of the transformation
 */
function performTransformation(entity, parameters) {
    if (typeof parameters.split !== 'function') {
        return;
    }
    const infos = parameters.split(',')
    const interaction = infos[0]
    const params = infos[2]
    switch (interaction) {
        case DISPLAY:
            entity.container.visible = true
            return
        case RESIZE:
            multiplyScale(entity, parseFloat(params))
            return
        default:
            console.warn('[WARNING InteractiveDisplayModule] Unknown interaction: ' + interaction)
    }
}

/**
 * Revert the transformation of an entity
 * @param entity the entity to revert
 * @param parameters the parameters of the transformation
 */
function revertTransformation(entity, parameters) {
    if (typeof parameters.split !== 'function') {
        return;
    }
    const infos = parameters.split(',')
    const interaction = infos[0]
    switch (interaction) {
        case DISPLAY:
            entity.container.visible = entity.currentState.visible
            return
        case RESIZE:
            resetScale(entity)
            return
        default:
            console.warn('[WARNING InteractiveDisplayModule] Unknown interaction: ' + interaction)
    }
}

function getInteractionMode(params) {
    if (typeof params.split === 'function') {
        return params.split(",")[1]
    } else {
        return undefined
    }
}


/**
 * Show all entities associated to an entity
 * @param id the id of the entity
 * @param module a reference to the InteractiveDisplayModule
 * @param unless the entity should not be displayed if its display mode is equal to unless
 */
function transformAssociated(id, module, unless = -1) {
    const to_display = module.currentFrame.registered[id]
    Object.keys(to_display).map(id2 => +id2).forEach(display_id => {
        if (getInteractionMode(to_display[display_id]) !== unless) {
            performTransformation(entityModule.entities.get(display_id), to_display[display_id])
        }
    })
}

/**
 * Hide all entities associated to an entity
 * @param id the id of the entity
 * @param module a reference to the InteractiveDisplayModule
 * @param only set to a number to hide only entities that have a specific display mode
 */
function revertAssociated(id, module, only) {
    const to_hide = module.currentFrame.registered[id]
    Object.keys(to_hide).map(id2 => +id2).forEach(hide_id => {
        if (!only || getInteractionMode(to_hide[hide_id]) === only) {
            revertTransformation(entityModule.entities.get(hide_id), to_hide[hide_id])
        }
    })
}

/**
 * Show all entities associated to a set of entities
 * @param ids the ids of the entities
 * @param module a reference to the InteractiveDisplayModule
 * @param unless the entity should not be displayed if its display mode is equal to unless
 */
function transformAllAssociated(ids, module, unless = -1) {
    for (let id of ids) {
        transformAssociated(id, module, unless)
    }
}

/**
 * Returns function called when the mouse is over the entity
 * @param id the id of the entity
 * @param module a reference to the InteractiveDisplayModule
 */
function getMouseOverFunc(id, module) {
    return function () {
        module.inside[id] = true
    }
}

/**
 * Returns the function called when the mouse move out of the entity
 * @param id the id of the entity
 * @param module a reference to the InteractiveDisplayModule
 */
function getMouseOutFunc(id, module) {
    return function () {
        delete module.inside[id]
        if (module.hovered_entities.has(id)) {
            module.removeHovered(id)
            revertAssociated(id, module, module.clicked_entities.includes(id) ? HOVER_ONLY : undefined)
        }
    }
}

/**
 * Returns the function called when an entity is clicked
 * @param id the id of the entity
 * @param module a reference to the InteractiveDisplayModule
 */
function getMouseClickFunc(id, module) {
    return function (ev) {
        const mouseEvent = ev.data.originalEvent
        if (mouseEvent instanceof MouseEvent && InteractiveDisplayModule.enable_display_on_click) {
            if (mouseEvent.button === 0 && module.currentFrame.registered[id][0] !== HOVER_ONLY) {
                if (module.clicked_entities.includes(id)) {
                    module.removeClicked(id)
                    revertAssociated(id, module)
                } else if (!mouseEvent.altKey) {
                    // remove the first added entity if too many entities are clicked
                    if (module.clicked_entities.push(id) > InteractiveDisplayModule.max_clicked_entities) {
                        const removed_id = module.clicked_entities.shift()
                        if (!module.hovered_entities.has(removed_id)) {
                            revertAssociated(removed_id, module)
                        }
                    }
                    transformAssociated(id, module, HOVER_ONLY)
                }
            }
        }
    }

}

/**
 * Return a function that will be called everytime the user click on the screen
 */
function getResetOnClickFunc(module) {
    return function (ev) {
        const mouseEvent = ev.data.originalEvent
        if (mouseEvent instanceof MouseEvent) {
            if (mouseEvent.altKey) {
                for (let permanent_id of module.clicked_entities) {
                    revertAssociated(permanent_id, module)
                }
                module.clicked_entities = []
            }
        }
    }
}

/**
 Returns the function called everytime the user move the mouse
 */
function getMouseMoveFunc(module) {
    return function (_ev) {
        if (!InteractiveDisplayModule.enable_display_on_hover) {
            return
        }
        const showing = []
        const ids = Object.keys(module.inside).map(n => +n)
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
            const isPossible = id => getEntityState(entityModule.entities.get(id), module.currentFrame.number) !== null
                && module.currentFrame.registered[id] !== null
                && module.currentFrame.registered[id][0] !== CLICK_ONLY
            if (InteractiveDisplayModule.allow_multiple_hover_display) {
                for (let show of showing) {
                    if (isPossible(show)) {
                        module.hovered_entities.add(show)
                        transformAssociated(show, module, CLICK_ONLY)
                    }
                }
            } else {
                const to_remove = []
                for (let id of module.hovered_entities) {
                    const register = module.currentFrame.registered[id]
                    if (register.length === 0) {
                        to_remove.push(id)
                        delete module.inside[id]
                    }
                }
                for (let id of to_remove) {
                    module.removeHovered(id)
                }
                if (module.hovered_entities.size === 0) {
                    const available = showing.filter(isPossible)
                    if (available.length) {
                        const min = Math.min(...available)
                        module.hovered_entities.add(min)
                        transformAssociated(min, module, CLICK_ONLY)
                    }
                }
            }
        }
    }
}

export class InteractiveDisplayModule {
    constructor(_assets) {
        this.interactive = {}
        this.previousFrame = {
            registered: {}
        }
        this.inside = {}
        this.hovered_entities = new Set()
        this.clicked_entities = []
        this.lastProgress = 1
        this.lastFrame = 0

    }

    removeClicked(id) {
        this.clicked_entities = this.clicked_entities.filter(item => item !== id)
    }

    removeHovered(id) {
        this.hovered_entities.delete(id)
    }


    static get name() {
        return 'h'
    }

    updateScene(previousData, currentData, progress) {
        this.currentFrame = currentData
        this.currentProgress = progress
        transformAllAssociated(this.hovered_entities, this, CLICK_ONLY)
        transformAllAssociated(this.clicked_entities, this, HOVER_ONLY)
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
                // container is the pixi object which is displayed to represent the entity
                entity.container.interactive = true
                entity.container.mouseover = getMouseOverFunc(entity.id, this)
                entity.container.mouseout = getMouseOutFunc(entity.id, this)
                entity.container.click = getMouseClickFunc(entity.id, this)
            }
        })
        this.container = container
        container.interactive = true
        container.mousemove = getMouseMoveFunc(this)
        if (InteractiveDisplayModule.enable_display_on_click) {
            // Container containing all the displayed entities.
            const entityContainer = entityModule.container
            // WARNING : this may somehow conflict with other custom modules by introducing unexpected behavior
            entityContainer.interactive = true
            // getResetOnClick will be triggered everytime the user click on the player
            entityContainer.click = getResetOnClickFunc(this)
        }
    }


}

/*
 * Module parameters
 */
InteractiveDisplayModule.enable_display_on_hover = true
InteractiveDisplayModule.enable_display_on_click = true
InteractiveDisplayModule.allow_multiple_hover_display = false
InteractiveDisplayModule.max_clicked_entities = Infinity