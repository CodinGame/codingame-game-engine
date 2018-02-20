import {PROPERTIES} from "./properties.js";
import {EntityFactory} from './EntityFactory.js';
import * as transitions from "../core/transitions.js";
import { lerp} from '../core/utils.js';

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
  d: 'duration'
};

var INSTANCE_COUNT = 0;

export class CreateCommand {
  constructor(args) {
    this.id = ++INSTANCE_COUNT;
    this.type = args[0];
  }

  apply(entities, frameNumber) {
    let entity = EntityFactory.create(this.type);
    entity.id = this.id;
    entities.set(this.id, entity);
  }
}

export class PropertiesCommand {
  static get curves() {
    return {
      // '/': (a => a), this will be used by default
      '_': (a => 1),
      'S': transitions.ease,
      '~': transitions.elastic
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