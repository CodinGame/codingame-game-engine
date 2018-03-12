import {PROPERTIES} from "./properties.js";
import {EntityFactory} from './EntityFactory.js';
import * as transitions from "../core/transitions.js";
import { lerp } from '../core/utils.js';
import { assets } from '../assets.js';

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
  T: 'text',
  C: 'children',
  sx: 'scaleX',
  sy: 'scaleY',
  ax: 'anchorX',
  ay: 'anchorY',
  v: 'visible',
  z: 'zIndex',
  b: 'blendMode',
  I: 'images',
  p: 'started',
  l: 'loop',
  d: 'duration',
  bw: 'baseWidth',
  bh: 'baseHeight'
};

export class CreateCommand {
  constructor(args, globalData) {
    this.id = ++globalData.instanceCount;
    this.type = args[0];
  }

  apply(entities, frameNumber) {
    let entity = EntityFactory.create(this.type);
    entity.id = this.id;
    entities.set(this.id, entity);
  }
}

export class LoadCommand {
  constructor([assetName, sourceImage, imageWidth, imageHeight, origRow, origCol, imageCount, imagesPerRow], globalData) {
    this.loader = new PIXI.loaders.Loader();

    const _imagesPerRow = imagesPerRow > 0 ? imagesPerRow : imageCount;
    const data = {
      frames: {},
      meta: {
        image: assets.images[sourceImage]
      }
    };
    for (let i = 0; i < imageCount; i++) {
      const frameName = imageCount > 1 ? (assetName + i) : assetName;
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
      };
    }

    this.loader.add('data:text/json;charset=UTF-8,' + JSON.stringify(data));
  }

  apply() {
    this.loader.load();
  }
}

export class PropertiesCommand {
  static get curves() {
    return {
      // '/': (a => a), this will be used by default
      '_': (a => 0),
      'S': transitions.ease,
      '~': transitions.elastic,
      'Γ': (a => 1)
    }
  }

  constructor(args, globalData, frameInfo) {
    let idx = 0;
    this.id = +args[idx++];
    this.t = +args[idx++];
    this.params = {};
    this.curve = {};
    while (idx < args.length) {
      const key = PROPERTY_KEY_MAP[args[idx]] || args[idx];
      const opts = (PROPERTIES[key] || PROPERTIES.default);
      let value = opts.type(args[idx + 1]);
      if (typeof opts.convert === 'function') {
        value = opts.convert(value, globalData, frameInfo, this.t);
      }
      let method = PropertiesCommand.curves[args[idx + 2]];
      
      this.params[key] = value;
      idx += 2;

      if (method) {
        this.curve[key] = method;
        idx += 1;
      }
    }
  }

  apply(entities, frameNumber) {
    let entity = entities.get(this.id);
    entity.addState(this.t, {values: this.params, curve: this.curve}, frameNumber);
  }
}