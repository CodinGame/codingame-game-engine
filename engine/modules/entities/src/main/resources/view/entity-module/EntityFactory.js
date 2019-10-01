import { Circle } from './Circle.js'
import Line from './Line.js'
import { Rectangle } from './Rectangle.js'
import { Sprite } from './Sprite.js'
import { Text } from './Text.js'
import { BitmapText } from './BitmapText.js'
import { Group } from './Group.js'
import { BufferedGroup } from './BufferedGroup.js'
import { SpriteAnimation } from './SpriteAnimation.js'
import { RoundedRectangle } from './RoundedRectangle.js'
import { Polygon } from './Polygon.js'
import { TilingSprite } from './TilingSprite.js'

export class EntityFactory {
  static create (type) {
    const EntityClass = {
      C: Circle,
      R: Rectangle,
      L: Line,
      S: Sprite,
      T: Text,
      X: BitmapText,
      G: Group,
      B: BufferedGroup,
      A: SpriteAnimation,
      K: RoundedRectangle,
      P: Polygon,
      D: TilingSprite
    }[type]
    if (!EntityClass) {
      throw new Error('Exception: entity type not found: ' + type)
    }
    return new EntityClass()
  }
}
