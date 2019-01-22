import { WIDTH, HEIGHT } from '../core/constants.js'
import { api as entityModule } from '../entity-module/GraphicEntityModule.js'

/* global PIXI */

function getMouseOverFunc (id, tooltip) {
  return function () {
    tooltip.inside[id] = true
  }
}

function getMouseOutFunc (id, tooltip) {
  return function () {
    delete tooltip.inside[id]
  }
}

function getEntityState (entity, frame) {
  const subStates = entity.states[frame]
  if (subStates && subStates.length) {
    return subStates[subStates.length - 1]
  }
  return null
}

function getMouseMoveFunc (tooltip, container, module) {
  return function (ev) {
    if (tooltip) {
      var pos = ev.data.getLocalPosition(container)
      tooltip.x = pos.x
      tooltip.y = pos.y

      const showing = []
      const ids = Object.keys(tooltip.inside).map(n => +n)

      for (let id of ids) {
        if (tooltip.inside[id]) {
          const entity = entityModule.entities.get(id)
          const state = entity && getEntityState(entity, module.currentFrame.number)
          if (!state) {
            delete tooltip.inside[id]
          } else {
            showing.push(id)
          }
        }
      }

      if (showing.length) {
        const tooltipBlocks = []
        for (let show of showing) {
          const entity = entityModule.entities.get(show)
          const state = getEntityState(entity, module.currentFrame.number)
          if (state !== null) {
            tooltip.visible = true
            const text = module.currentFrame.registeredText[show]
            if (text && text.length && String(text).valueOf() !== '0') {
              tooltipBlocks.push(text)
            }
          }
        }
        tooltip.label.text = tooltipBlocks.join('\n──────────\n')
      } else {
        tooltip.visible = false
      }

      tooltip.background.width = tooltip.label.width + 20
      tooltip.background.height = tooltip.label.height + 20

      tooltip.pivot.x = -30
      tooltip.pivot.y = -50

      if (tooltip.y - tooltip.pivot.y + tooltip.height > HEIGHT) {
        tooltip.pivot.y = 10 + tooltip.height
        tooltip.y -= tooltip.y - tooltip.pivot.y + tooltip.height - HEIGHT
      }

      if (tooltip.x - tooltip.pivot.x + tooltip.width > WIDTH) {
        tooltip.pivot.x = tooltip.width
      }
    }
  }
};

export class TooltipModule {
  constructor (assets) {
    this.interactive = {}
    this.previousFrame = {
      registered: {}
    }
    this.lastProgress = 1
    this.lastFrame = 0
  }

  static get name () {
    return 'tooltips'
  }

  updateScene (previousData, currentData, progress) {
    this.currentFrame = currentData
    this.currentProgress = progress
  }

  handleFrameData (frameInfo, data) {
    if (!data) {
      return
    }
    const registered = data[0]
    const registeredText = { ...this.previousFrame.registeredText, ...registered }

    Object.keys(registered).forEach(
      k => {
        this.interactive[k] = true
      }
    )

    const frame = { registeredText, number: frameInfo.number }
    this.previousFrame = frame
    return frame
  }

  reinitScene (container) {
    this.tooltip = this.initTooltip()
    entityModule.entities.forEach(entity => {
      if (this.interactive[entity.id]) {
        entity.container.interactive = true
        entity.container.mouseover = getMouseOverFunc(entity.id, this.tooltip)
        entity.container.mouseout = getMouseOutFunc(entity.id, this.tooltip)
      }
    })
    this.container = container
    container.interactive = true
    container.mousemove = getMouseMoveFunc(this.tooltip, container, this)
    container.addChild(this.tooltip)
  }

  generateText (text, size, color, align) {
    var textEl = new PIXI.Text(text, {
      fontSize: Math.round(size / 1.2) + 'px',
      fontFamily: 'Lato',
      fontWeight: 'bold',
      fill: color
    })

    textEl.lineHeight = Math.round(size / 1.2)
    if (align === 'right') {
      textEl.anchor.x = 1
    } else if (align === 'center') {
      textEl.anchor.x = 0.5
    }

    return textEl
  };

  initTooltip () {
    var tooltip = new PIXI.Container()
    var background = tooltip.background = new PIXI.Graphics()
    var label = tooltip.label = this.generateText('', 36, 0xFFFFFF, 'left')

    background.beginFill(0x0, 0.7)
    background.drawRect(0, 0, 200, 185)
    background.endFill()
    background.x = -10
    background.y = -10

    tooltip.visible = false
    tooltip.inside = {}

    tooltip.addChild(background)
    tooltip.addChild(label)

    tooltip.interactiveChildren = false
    return tooltip
  };
}
