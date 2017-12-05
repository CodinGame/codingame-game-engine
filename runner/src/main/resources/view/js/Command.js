import {PROPERTIES} from "./properties.js";
import {EntityFactory} from './EntityFactory.js';

export class CreateCommand {
  constructor(args) {
    this.id = +args[0];
    this.type = args[1];
  }

  apply(stage, entities, frameNumber) {
    let entity = EntityFactory.create(this.type);
    entity.id = this.id;
    entities.set(this.id, entity);
  }
}

export class PropertiesCommand {
  constructor(args) {
    let idx = 0;
    this.id = +args[idx++];
    this.t = +args[idx++];
    this.params = {};

    while (idx + 1 < args.length) {
      let key = args[idx];
      let type = (PROPERTIES[key] || PROPERTIES.default).type;
      let value = type(args[idx + 1]);
      this.params[key] = value;
      idx += 2;
    }
  }
}

export class UpdateCommand extends PropertiesCommand {
  apply(stage, entities, frameNumber) {
    let entity = entities.get(this.id);
    entity.update(this.t, this.params, frameNumber);
  }
}

export class SetCommand extends PropertiesCommand {
  apply(stage, entities, frameNumber) {
    let entity = entities.get(this.id);
    entity.set(this.t, this.params, frameNumber);
  }
}