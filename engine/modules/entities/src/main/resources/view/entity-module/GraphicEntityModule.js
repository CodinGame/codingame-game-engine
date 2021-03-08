import { CommandParser } from './CommandParser.js'
import { fitAspectRatio } from '../core/utils.js'
import { WIDTH, HEIGHT } from '../core/constants.js'
import { ContainerBasedEntity } from './ContainerBasedEntity.js'
export const api = {}

export class GraphicEntityModule {
  constructor (assets) {
    this.entities = new Map()
    this.frames = []
    this.loadingAssets = 0

    this.extrapolationMap = {}

    this.globalData = {
      toWorldUnits: 1,
      mustResetTree: true,
      maskUpdates: {},
      updatedBuffers: [],
      players: [],
      instanceCount: 0
    }

    api.entities = this.entities
  }

  static get name () {
    return 'entitymodule'
  }

  handleFrameData (frameInfo, frameData) {
    if (frameData) {
      const commands = CommandParser.parse(frameData, this.globalData, frameInfo)
      if (commands) {
        commands.forEach(command => {
          const loadPromise = command.apply(this.entities, frameInfo)
          if (loadPromise) {
            this.loadingAssets++
            loadPromise.then(() => {
              this.loadingAssets--
            })
          }
        })
      }
    }

    const parsedFrame = { ...frameInfo }

    parsedFrame.previous = this.frames[this.frames.length - 1] || parsedFrame

    this.extrapolate(parsedFrame)

    if (parsedFrame !== parsedFrame.previous) {
      parsedFrame.previous.next = parsedFrame
    }

    this.frames.push(parsedFrame)

    return parsedFrame
  }

  lastElementOf (arr) {
    return arr[arr.length - 1]
  }

  extrapolate (frameInfo) {
    const frameNumber = frameInfo.number
    const previousFrameNumber = frameInfo.previous.number
    this
      .entities.forEach(entity => {
        // Only create if entity is affected by this frameInfo
        if (entity.stateAdded) {
          entity.stateAdded = false
        } else {
          return
        }

        // Create empty substate array if none
        if (!entity.states[frameNumber]) {
          entity.states[frameNumber] = []
        }
        // Copy default state if extrapolation just started
        if (!this.extrapolationMap[entity.id]) {
          this.extrapolationMap[entity.id] = { ...entity.defaultState }
        }

        const subStates = entity.states[frameNumber]

        // Sort on t to begin extrapolation
        subStates.sort((a, b) => a.t - b.t)

        if (!subStates.length || this.lastElementOf(subStates).t !== 1) {
          // Create a subState at t=1
          entity.addState(1, {}, frameNumber, frameInfo)
        }

        let prevState = this.extrapolationMap[entity.id] // currentState
        // If the entity had a state in the previous frame get the last one of them
        if (entity.states[previousFrameNumber] && previousFrameNumber !== frameNumber) {
          prevState = entity.states[previousFrameNumber][entity.states[previousFrameNumber].length - 1]
        }

        entity.states[frameNumber] = subStates.map((subState) => {
          // Extrapolate through existing substates, updating the extrapolationMap in the process (currentState)
          const state = this.extrapolationMap[entity.id] = { ...this.extrapolationMap[entity.id], ...subState }

          if (typeof entity.computeAnimationProgressTime === 'function') {
            entity.computeAnimationProgressTime(prevState, state)
          }
          prevState = state
          return state
        })
      })
  }

  reinitScene (container, canvasData) {
    this.globalData.toPixel = (WIDTH / canvasData.width) * canvasData.oversampling
    this.globalData.mustResetTree = true
    api.container = this.container = container
    container.sortableChildren = true
    this.entities.forEach((e) => {
      e.init()
    })
  }

  stillLoading () {
    return this.loadingAssets > 0
  }

  updateScene (previousData, currentData, progress) {
    if (this.stillLoading()) {
      return
    }

    this.entities.forEach(e => e.render(progress, currentData, this.globalData))

    // Flags are set by Entity when a group has different children
    if (this.globalData.mustResetTree) {
      this.reconstructTree()
      this.globalData.mustResetTree = false
    }
    for (const entityId in this.globalData.maskUpdates) {
      const entity = this.entities.get(+entityId)
      const maskId = this.globalData.maskUpdates[entityId]
      if (maskId === -1) {
        entity.container.mask = null
      } else {
        entity.container.mask = this.entities.get(maskId).graphics
      }
    }
    for (const entity of this.globalData.updatedBuffers) {
      entity.postUpdate()
    }
    this.globalData.maskUpdates = {}
  }

  
  reconstructTree () {
    this.container.removeChildren()

    // Groups
    this.entities.forEach(e => {
      if (e instanceof ContainerBasedEntity) {
        e.childrenContainer.removeChildren()
        e.currentState.children.forEach(id => {
          const child = this.entities.get(id)
          e.childrenContainer.addChild(child.container)
          child.parent = e
        })
      }
    })

    // Parent
    this.entities.forEach(e => {
      if (!e.container.parent) {
        this.container.addChild(e.container)
      }
    })
  }

  handleGlobalData (players, globalData) {
    this.globalData.players = players
    const width = globalData.width
    const height = globalData.height
    this.globalData.toWorldUnits = fitAspectRatio(width, height, WIDTH, HEIGHT)
    api.toWorldUnits = this.globalData.toWorldUnits

    // Retro-compatibility
    if (!api.hasOwnProperty('coeff')) {
      Object.defineProperty(api, 'coeff', {
        get: () => {
          const msg = 'The "coeff" property of GraphicEntityModule\'s API is deprecated, please use "toWorldUnits" instead'
          const stack = (new Error()).stack

          if (console.groupCollapsed) {
            console.groupCollapsed(
              '%cDeprecation Warning: %c%s',
              'color:#614108;background:#fffbe6',
              'font-weight:normal;color:#614108;background:#fffbe6',
              msg
            )
            console.warn(stack)
            console.groupEnd()
          } else {
            console.warn('Deprecation Warning: ', msg)
          }

          return api.toWorldUnits
        }
      })
    }
  }
}
