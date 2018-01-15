import {PROPERTIES} from "./properties.js";
import {EntityFactory} from './EntityFactory.js';

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
  v: 'visiblz',
  z: 'zIndex',
  b: 'blendMode',
  I: 'images',
  p: 'started',
  l: 'loop',
  d: 'duration'
};

export class CreateCommand {
  constructor(args) {
    this.id = +args[0];
    this.type = args[1];
  }

  apply(entities, frameNumber) {
    let entity = EntityFactory.create(this.type);
    entity.id = this.id;
    entities.set(this.id, entity);
  }
}

export class PropertiesCommand {
  constructor(args, globalData, frameInfo) {
    let idx = 0;
    this.id = +args[idx++];
    this.t = +args[idx++];
    this.params = {};

    while (idx + 1 < args.length) {
      let key = PROPERTY_KEY_MAP[args[idx]] || args[idx];
      let opts = (PROPERTIES[key] || PROPERTIES.default);
      let value = opts.type(args[idx + 1]);
      if (typeof opts.convert === 'function') {
        value = opts.convert(value, globalData, frameInfo, this.t);
      }
      this.params[key] = value;
      idx += 2;
    }
  }
}

export class UpdateCommand extends PropertiesCommand {
  apply(entities, frameNumber) {
    let entity = entities.get(this.id);
    entity.update(this.t, this.params, frameNumber);
  }
}

export class SetCommand extends PropertiesCommand {
  apply(entities, frameNumber) {
    let entity = entities.get(this.id);
    entity.set(this.t, this.params, frameNumber);
  }
}