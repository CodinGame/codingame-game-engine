import {PROPERTIES} from "./properties.js";
import {EntityFactory} from './EntityFactory.js';

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
  depreacted_constructor(args, globalData) {
    let idx = 0;
    this.id = +args[idx++];
    this.t = +args[idx++];
    this.params = JSON.parse(args.slice(2).join(' '));
    for (const key in this.params) {
      const value = this.params[key];
      let opts = (PROPERTIES[key] || PROPERTIES.default);
      if (typeof opts.convert === 'function') {
        this.params[key] = opts.convert(value, globalData);
      }
    }
  }
  
  constructor(args, globalData) {
    let idx = 0;
    this.id = +args[idx++];
    this.t = +args[idx++];
    this.params = {};

    while (idx + 1 < args.length) {
      let key = args[idx];
      let opts = (PROPERTIES[key] || PROPERTIES.default);
      let value = opts.type(args[idx + 1]);
      if (typeof opts.convert === 'function') {
        value = opts.convert(value, globalData);
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