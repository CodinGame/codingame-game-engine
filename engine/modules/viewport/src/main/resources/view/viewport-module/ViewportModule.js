import { HEIGHT, WIDTH } from '../core/constants.js'
import { getRenderer } from '../core/rendering.js'
import { api as entityModule } from '../entity-module/GraphicEntityModule.js'
import vp from './lib/viewport.es.js'

export class ViewportModule {

  static get name() {
    return 'viewport'
  }

  entityIds = []

  /**
   * Called when data is received.
   * Handles data for the given frame. Returns data that will be sent as parameter to updateScene.
   * @param frameInfo information about the current frame.
   * @param frameData data that has been sent from the Java module.
   */
  handleFrameData (frameInfo, frameData) {
    // Handle your data here
    const newEntityIds = frameData

    this.entityIds = [...this.entityIds, ...newEntityIds]

    // Return what is necessary to your module
    return { frameInfo, frameData }
  }

  /**
   * Called when the viewer needs to be rerendered (init phase, resized viewer).
   * @param container a PIXI Container. Add your elements to this object.
   * @param canvasData canvas data containing width and height.
   */
  reinitScene (container, canvasData) {
    if (this.vp == null) {
      this.vp = vp()
    }

    this.entityIds.forEach(entityId => {
      this._initViewPort(entityId);
    })
  }
  
  _initViewPort(entityId) {
    const entity = entityModule.entities.get(entityId)

    const viewport = new this.vp.Viewport({
      screenWidth: WIDTH,
      screenHeight: HEIGHT,
      worldWidth: WIDTH,
      worldHeight: HEIGHT,

      interaction: getRenderer().plugins.interaction, // the interaction module is important for wheel to work properly when renderer.view is placed or scaled,
      divWheel: getRenderer().view,
      passiveWheel: false,
      stopPropagation: true
    })

    viewport
      .drag()
      .wheel()

    viewport.fitWorld()
    viewport.interactiveChildren = false
    Object.defineProperty(viewport, 'cursor', {
      get: () => viewport.input.isMouseDown ? 'grabbing' : 'grab'
    })

    // Switch the GraphicEntityModule's Container with our Viewport 
    entity.container.removeChildren()
    viewport.addChild(entity.graphics)
    const parent = entity.container.parent
    if (parent) {
      parent.removeChild(entity.container)
    }
    entity.container = viewport
    if (parent) {
      parent.addChild(entity.container)
    }
  }

  /**
   * Called when the scene needs an update.
   * @param previousData data from the previous frame.
   * @param currentData data of the current frame.
   * @param progress progress of the frame. 0 <= progress <= 1
   * @param speed the speed of the viewer, setted up by the user.
   */
  updateScene (previousData, currentData, progress, speed) {
  }

}
