import { PROPERTIES } from './properties.js'
import { EntityFactory } from './EntityFactory.js'
import * as transitions from '../core/transitions.js'
import { assets } from '../assets.js'

/* global PIXI */

const PROPERTY_KEY_MAP = {
  r: 'rotation',
  R: 'radius',
  X: 'x2',
  Y: 'y2',
  w: 'width',
  h: 'height',
  t: 'tint',
  f: 'fillColor',
  F: 'fillAlpha',
  c: 'lineColor',
  W: 'lineWidth',
  A: 'lineAlpha',
  a: 'alpha',
  i: 'image',
  S: 'strokeThickness',
  sc: 'strokeColor',
  ff: 'fontFamily',
  s: 'fontSize',
  fw: 'fontWeight',
  T: 'text',
  ta: 'textAlign',
  ch: 'children',
  sx: 'scaleX',
  sy: 'scaleY',
  ax: 'anchorX',
  ay: 'anchorY',
  kx: 'skewX',
  ky: 'skewY',
  v: 'visible',
  z: 'zIndex',
  b: 'blendMode',
  I: 'images',
  rs: 'restarted',
  p: 'playing',
  l: 'loop',
  d: 'duration',
  bw: 'baseWidth',
  bh: 'baseHeight',
  ps: 'points',
  tx: 'tileX',
  ty: 'tileY',
  tsx: 'tileScaleX',
  tsy: 'tileScaleY',
  sm: 'scaleMode'
}

export class CreateCommand {
  constructor (args, globalData) {
    this.id = ++globalData.instanceCount
    this.type = args[0]
  }

  apply (entities) {
    const entity = EntityFactory.create(this.type)
    entity.id = this.id
    entities.set(this.id, entity)
  }
}

export class CreateCommands {
  constructor (args, globalData) {
    this.commands = []
    this.id = ++globalData.instanceCount
    this.type = args[0]
  }

  apply (entities, frameInfo) {
    this.commands.forEach(command => command.apply(entities, frameInfo.number, frameInfo))
  }
}

export class LoadCommand {
  constructor ([assetName, sourceImage, imageWidth, imageHeight, origRow, origCol, imageCount, imagesPerRow], globalData) {
    this.loader = new PIXI.Loader()

    if (imageWidth === '0') {
      imageWidth = PIXI.utils.TextureCache[sourceImage].width
    }
    if (imageHeight === '0') {
      imageHeight = PIXI.utils.TextureCache[sourceImage].height
    }

    const _imagesPerRow = imagesPerRow > 0 ? imagesPerRow : imageCount
    const data = {
      frames: {},
      meta: {
        image: (assets.baseUrl ? assets.baseUrl : '') + assets.images[sourceImage]
      }
    }
    for (let i = 0; i < imageCount; i++) {
      const frameName = imageCount > 1 ? (assetName + i) : assetName
      data.frames[frameName] = {
        frame: {
          x: imageWidth * origCol + (i % _imagesPerRow) * imageWidth,
          y: imageHeight * origRow + Math.floor(i / _imagesPerRow) * imageHeight,
          w: imageWidth,
          h: imageHeight
        },
        sourceSize: {
          w: imageWidth,
          h: imageHeight
        },
        rotated: false,
        trimmed: false
      }
    }
    const dataString = 'data:text/json;charset=UTF-8,' + JSON.stringify(data)
    if (!PIXI.utils.TextureCache[dataString + '_image']) {
      this.loader.add(dataString, { crossOrigin: true })
    }
  }

  apply () {
    if (Object.keys(this.loader.resources).length > 0) {
      return new Promise((resolve) => {
        this.loader.load()
        this.loader.onComplete.add(resolve)
      })
    }
  }
}

export class PropertiesCommand {
  static get curves () {
    return {
      // '/': (a => a), this will be used by default
      '_': a => a < 1 ? 0 : 1,
      '∫': transitions.ease,
      '🙖': transitions.easeIn,
      '🙒': transitions.easeOut,
      '~': transitions.elastic,
      'Γ': a => 1
    }
  }

  constructor (args, globalData, frameInfo) {
    this.id = +args[0]
    this.t = +args[1]
    this.params = {}
    this.curve = {}
    let idx = 2
    while (idx < args.length) {
      const key = PROPERTY_KEY_MAP[args[idx]] || args[idx]
      const opts = (PROPERTIES[key] || PROPERTIES.default)
      let value = opts.type(args[idx + 1])
      if (typeof opts.convert === 'function') {
        value = opts.convert(value, globalData, frameInfo, this.t)
      }
      const method = PropertiesCommand.curves[args[idx + 2]]

      this.params[key] = value
      idx += 2

      if (method) {
        this.curve[key] = method
        idx += 1
      }
    }
  }

  apply (entities, frameInfo) {
    const entity = entities.get(this.id)
    entity.addState(this.t, { values: this.params, curve: this.curve }, frameInfo.number, frameInfo)
    entity.stateAdded = true
  }
}
