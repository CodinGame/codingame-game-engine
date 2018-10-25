import { Circle } from './Circle.js'
import Line from './Line.js'
import { Rectangle } from './Rectangle.js'
import { Sprite } from './Sprite.js'
import { Text } from './Text.js'
import { Group } from './Group.js'
import { BufferedGroup } from './BufferedGroup.js'
import { SpriteAnimation } from './SpriteAnimation.js'

export class EntityFactory {
  static create (type) {
    var entity
    switch (type) {
      case 'C':
        entity = new Circle()
        break
      case 'R':
        entity = new Rectangle()
        break
      case 'L':
        entity = new Line()
        break
      case 'S':
        entity = new Sprite()
        break
      case 'T':
        entity = new Text()
        break
      case 'G':
        entity = new Group()
        break
      case 'B':
        entity = new BufferedGroup()
        break
      case 'A':
        entity = new SpriteAnimation()
        break
      default:
        throw new Error('Exception: entity type not found: ' + type)
    }
    return entity
  }
}
