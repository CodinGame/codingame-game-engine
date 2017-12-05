import {Circle} from "./Circle.js";
import Line from "./Line.js";
import {Rectangle} from "./Rectangle.js";
import {Sprite} from "./Sprite.js";

export class EntityFactory {
  static create(type) {
    var entity;
    switch (type) {
      case 'CIRCLE':
        entity = new Circle();
        break;
      case 'RECTANGLE':
        entity = new Rectangle();
        break;
      case 'LINE':
        entity = new Line();
        break;
      case 'SPRITE':
        entity = new Sprite();
        break;
      default:
        throw "Exception: entity type not found: " + type;
    }
    return entity;
  }
}