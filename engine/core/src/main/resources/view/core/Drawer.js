import { assets } from '../assets.js'
import * as config from '../config.js'
import { unlerp, fitAspectRatio } from './utils.js'
import { WIDTH, HEIGHT, BASE_FRAME_DURATION } from './constants.js'
import { ErrorLog } from './ErrorLog.js'
import { demo as defaultDemo } from '../demo.js'
import { setRenderer, destroyFlagged } from './rendering.js'
import { ModuleError } from './ModuleError.js'
/* global PIXI requestAnimationFrame $ */

export class Drawer {
  constructor (customDemo) {
    PIXI.settings.STRICT_TEXTURE_CACHE = true
    this.toDestroy = []
    this.stepByStepAnimateSpeed = config.stepByStepAnimateSpeed || null

    const demo = customDemo || defaultDemo

    if (demo) {
      const frames = demo.views
      const agents = demo.agents
      const logo = 'logo.png'
      this.demo = {
        playerCount: agents.length,
        logo,
        overlayAlpha: 0.2,
        agents,
        frames
      }
      if (config.overlayAlpha || config.overlayAlpha === 0) {
        this.demo.overlayAlpha = config.overlayAlpha
      }
      this.demo.agents.forEach(agent => {
        agent.color = Drawer.playerColors[agent.index]
      })
    } else {
      this.demo = config.demo
    }
  }

  static get requirements () {
    return {
      PIXI: 'PIXI6'
    }
  }

  static get VERSION () {
    return 2
  }

  static get WIDTH () {
    return WIDTH
  }

  static get HEIGHT () {
    return HEIGHT
  }

  static getGameRatio () {
    return Drawer.WIDTH / Drawer.HEIGHT
  }

  static get playerColors () {
    return config.playerColors || [
      '#ff1d5c', // radical red
      '#22a1e4', // curious blue
      '#ff8f16', // west side orange
      '#6ac371', // mantis green
      '#9975e2', // medium purple
      '#3ac5ca', // scooter blue
      '#de6ddf', // lavender pink
      '#ff0000' // solid red
    ]
  }

  static get SDK_GAME () {
    return true
  }

  getDefaultOverSampling () {
    return config.defaultOverSampling || 2
  }

  handleModuleError (name, error) {
    ErrorLog.push(new ModuleError(name, error))
    console.error(error)
    ErrorLog.push({
      message: `<Module "${name}" disabled>\n`
    })
    delete this.modules[name]
  }

  instantiateModules () {
    this.modules = {}
    for (const Module of config.modules) {
      try {
        this.modules[Module.moduleName || Module.name] = new Module(assets)
      } catch (error) {
        this.handleModuleError(Module.moduleName || Module.name, error)
      }
    }
  }

  destroy () {
    if (this.alreadyLoaded) {
      this.renderer.destroy()
      this.endCallback = null
    }
    this.destroyed = true
  }

  destroyScene (scope) {
    destroyFlagged()
  }

  /** Mandatory */
  getGameName () {
    return config.gameName
  }

  canSwapPlayers () {
    return false
  }

  addErrorListener (callback) {
    ErrorLog.listen(callback)
  }

  /** Mandatory */
  getResources () {
    return Object.assign({
      baseUrl: '',
      images: {},
      spines: {},
      sprites: [],
      fonts: [],
      others: []
    }, assets)
  }

  getOptions () {
    // Return an array of copies
    if (config.options) {
      return config.options.map(v => ({ ...v }))
    }
    return []
  }

  setDebug (v) {
    this.asyncRenderingTime = Drawer.RenderTimeout
  }

  /** Mandatory */
  initPreload (scope, container, progress, canvasWidth, canvasHeight) {
    scope.canvasWidth = canvasWidth
    scope.canvasHeight = canvasHeight

    scope.loaderProgress = new PIXI.Text('100', {
      fontSize: (canvasHeight * 0.117) || 30,
      fontFamily: 'Lato',
      fontWeight: '900',
      fill: 'white',
      align: 'center'
    })

    scope.loaderProgress.anchor.y = 1
    scope.loaderProgress.anchor.x = 1.3
    scope.progress = scope.realProgress = progress
    scope.loaderProgress.position.y = canvasHeight

    scope.progressBar = new PIXI.Graphics()
    container.addChild(scope.progressBar)
    container.addChild(scope.loaderProgress)
  }

  /** Mandatory */
  preload (scope, container, progress, canvasWidth, canvasHeight, obj) {
    scope.progress = progress
  }

  /** Mandatory */
  renderPreloadScene (scope, step) {
    var stepFactor = Math.pow(0.998, step)
    scope.realProgress = stepFactor * scope.realProgress + (1 - stepFactor) * scope.progress
    scope.loaderProgress.text = ((scope.realProgress * 100).toFixed(0))
    scope.loaderProgress.position.x = scope.realProgress * scope.canvasWidth

    scope.progressBar.clear()

    scope.progressBar.beginFill(0x0, 1)
    scope.progressBar.drawRect(0, 0, scope.canvasWidth * scope.realProgress + 1, scope.canvasHeight)
    scope.progressBar.endFill()

    scope.progressBar.beginFill(0x3f4446, 1)
    scope.progressBar.drawRect(scope.canvasWidth * scope.realProgress, 0, scope.canvasWidth, scope.canvasHeight)
    scope.progressBar.endFill()
    return true
  }

  /** Mandatory */
  initDefaultScene (scope, container, canvasWidth, canvasHeight) {
    var scene = new PIXI.Container()

    scope.drawer = this
    scope.time = 0

    if (this.demo) {
      if (this.demo.logo) {
        try {
          const logo = PIXI.Sprite.from(this.demo.logo)
          logo.position.set(Drawer.WIDTH / 2, Drawer.HEIGHT / 2)
          logo.anchor.set(0.5)
          logo.baseScale = fitAspectRatio(logo.texture.width, logo.texture.height, 2 * Drawer.WIDTH / 3, Drawer.HEIGHT / 2, 0)
          scene.addChild(logo)
          scope.logo = logo
        } catch (error) {
          ErrorLog.push({
            cause: error,
            message: 'Missing "logo.png" to complete replay.'
          })
          scope.logo = new PIXI.Container()
          scope.logo.baseScale = 1
          scene.addChild(scope.logo)
          scope.missingLogo = true
        }
      }

      var darkness = new PIXI.Graphics()
      if (!scope.missingLogo) {
        darkness.beginFill(0, this.demo.overlayAlpha || 0)
        darkness.drawRect(0, 0, Drawer.WIDTH + 20, Drawer.HEIGHT + 20)
        darkness.endFill()
        darkness.x -= 10
        darkness.y -= 10
      }

      var demoContainer = new PIXI.Container()
      try {
        this.initDefaultFrames(this.demo.playerCount, this.demo.frames, this.demo.agents)
        /** **************************************************************************************************************************************** */
        this.preconstructScene(this.scope, container, this.initWidth, this.initHeight)
        this.initScene(this.scope, demoContainer, this.frames, true)
        this.updateScene(this.scope, this.question, this.frames, this.currentFrame, this.progress, 1, this.reasons[this.currentFrame], true)
        /** **************************************************************************************************************************************** */
      } catch (error) {
        ErrorLog.push({
          cause: error,
          message: 'Cannot load demo, you might want to reset the demo'
        })
      }
      scope.demo = demoContainer
      scope.demotime = 0

      this.currentFrame = -1
      container.addChild(demoContainer)
      container.addChild(darkness)
      container.addChild(scene)
    }

    scope.updateTime = 0
    scope.frameTime = 0
  }

  initDefaultFrames (playerCount, frames, agents) {
    var drawer = this

    this.instantiateModules()

    this.playerInfo = agents.map(function (agent, index) {
      var agentData = {
        name: agent.name || 'Anonymous',
        color: agent.color ? drawer.parseColor(agent.color) : '#ffffff',
        number: index,
        index: agent.index,
        type: agent.type,
        isMe: agent.type === 'CODINGAMER' && agent.typeData.me,
        avatar: agent.avatarTexture
      }

      return agentData
    })

    this.instantiateModules()

    this._frames = frames.map(f => {
      const splittedF = f.split('\n')
      const header = splittedF[0].split(' ')

      let data
      try {
        data = JSON.parse(splittedF.slice(1).join('\n'))
      } catch (err) {
        data = {}
      }
      return { ...data, key: header[0] === 'KEY_FRAME' }
    }).filter(x => x.key)

    this.parseGlobalData(this._frames[0].global)
    this.playerCount = playerCount
    this.reasons = []
    this.frames = []
    this.currentFrame = 0
    this.currentFrameDuration = 1000
    this.progress = 1
    const firstFrame = this._frames[0].frame
    firstFrame.key = this._frames[0].key
    this.frames.push(this.parseFrame(firstFrame, this.frames))
    for (var i = 1; i < this._frames.length; ++i) {
      this.frames.push(this.parseFrame(this._frames[i], this.frames))
    }

    this.asyncRenderingTime = Drawer.RenderTimeout
  };

  /** Mandatory */
  renderDefaultScene (scope, step) {
    step = Math.min(80, step)

    if (this.demo === undefined) {
      return false
    }

    this.currentFrame = this.currentFrameTemp || 0

    scope.frameTime += step
    scope.updateTime += step
    scope.demotime += step / 1000

    var animProgress = Math.max(0, Math.min(1, (scope.demotime - 1) / 0.5))
    if (scope.logo) {
      scope.logo.alpha = animProgress
      scope.logo.scale.x = scope.logo.scale.y = (3 - animProgress * 2) * scope.logo.baseScale
    }

    if (!scope.missingLogo && scope.demotime > 1.5 && scope.demotime <= 2.2) {
      var amplitude = Math.max(0, 1 - (scope.demotime - 1.5) / 0.7) * 15

      scope.demo.x = (Math.random() * 2 - 1) * amplitude
      scope.demo.y = (Math.random() * 2 - 1) * amplitude
    } else {
      scope.demo.x = scope.demo.y = 0
    }
    var updateInterval = 30
    var frameInterval = this.getFrameDuration(this.currentFrame)

    if (scope.updateTime >= updateInterval) {
      scope.updateTime -= updateInterval
      if (this.currentFrame === 0) {
        this.progress = 1
      } else {
        this.progress = unlerp(0, frameInterval, scope.frameTime)
      }
      this.updateScene(this.scope, this.question, this.frames, this.currentFrame, this.progress, 1, this.reasons[this.currentFrame], true)
    }

    if (scope.frameTime >= frameInterval) {
      scope.frameTime -= frameInterval
      this.currentFrame = (this.currentFrame + 1) % this.frames.length
    }
    this.renderScene(this.scope, this.question, this.frames, this.currentFrame, this.progress, 1, this.reasons[this.currentFrame], step, true)

    this.currentFrameTemp = this.currentFrame
    this.currentFrame = -1
    return true
  }

  endDefaultScene (scope, step) {
    return true
  }

  /** Mandatory */
  parseGlobalData (globalData) {
    for (const moduleName in this.modules) {
      const module = this.modules[moduleName]
      if (typeof module.handleGlobalData === 'function') {
        module.handleGlobalData(this.playerInfo, globalData[moduleName])
      }
    }
  }

  /** Mandatory */
  parseFrame (frame, previousFrames) {
    const parsedFrame = {
      data: {},
      frameInfo: {
        number: previousFrames.length
      }
    }

    parsedFrame.previous = previousFrames[previousFrames.length - 1] || parsedFrame
    if (parsedFrame !== parsedFrame.previous) {
      parsedFrame.previous.next = parsedFrame
    }

    if (!frame.key) {
      return parsedFrame.previous
    }

    if (frame.duration) {
      this.currentFrameDuration = frame.duration
    }
    parsedFrame.frameInfo.frameDuration = this.currentFrameDuration

    if (parsedFrame === parsedFrame.previous) {
      parsedFrame.frameInfo.date = 0
      parsedFrame.frameInfo.frameDuration = 0
    } else {
      parsedFrame.frameInfo.date = parsedFrame.previous.frameInfo.date + parsedFrame.previous.frameInfo.frameDuration
    }

    for (const moduleName in this.modules) {
      const module = this.modules[moduleName]
      if (typeof module.handleFrameData === 'function') {
        parsedFrame.data[moduleName] = module.handleFrameData(parsedFrame.frameInfo, frame[moduleName])
      }
    }

    return parsedFrame
  }

  preconstructScene (scope, container, canvasWidth, canvasHeight) {
    scope.canvasHeight = canvasHeight
    scope.canvasWidth = canvasWidth

    scope.time = 0
    scope.endTime = 0

    scope.playerInfo = this.playerInfo

    container.scale.x = canvasWidth / Drawer.WIDTH
    container.scale.y = canvasHeight / Drawer.HEIGHT
  }

  initScene (scope, container, frames) {
    for (const moduleName in this.modules) {
      const module = this.modules[moduleName]
      var stage = new PIXI.Container()
      try {
        module.reinitScene(stage, {
          width: scope.canvasWidth,
          height: scope.canvasHeight,
          oversampling: this.oversampling
        })
      } catch (error) {
        this.handleModuleError(moduleName, error)
      }
      container.addChild(stage)
    }
  }

  updateScene (scope, question, frames, frameNumber, progress, speed, reason, demo, force) {
    const parsedFrame = frames[frameNumber]
    if (!force && this.stepByStepAnimateSpeed) {
      if (this.checkSteppedToNextFrame(scope, parsedFrame, progress)) {
        this.startAsynchronousAnimation(scope, parsedFrame)
        return
      } else if (this.checkSteppedToPreviousFrame(scope, parsedFrame, progress)) {
        const reversed = true
        this.startAsynchronousAnimation(scope, scope.currentFrame, reversed)
        return
      }
      scope.targetProgress = null
    }

    /** ************************************* */
    /*        SYNCHRONOUS                     */
    /** ************************************* */

    scope.currentFrame = parsedFrame
    scope.currentProgress = progress
    scope.reason = reason

    for (const moduleName in this.modules) {
      const module = this.modules[moduleName]
      if (parsedFrame.data.hasOwnProperty(moduleName)) {
        try {
          module.updateScene(parsedFrame.previous.data[moduleName], parsedFrame.data[moduleName], progress, speed)
        } catch (error) {
          this.handleModuleError(moduleName, error)
        }
      }
    }
  }

  startAsynchronousAnimation (scope, frameToAnimate, reversed = false) {
    scope.targetProgress = reversed ? 0 : 1
    if (!reversed && scope.currentProgress === 1) {
      scope.currentProgress = 0
    }
    scope.currentFrame = frameToAnimate
    scope.reverseAsynchronousAnimation = reversed
  }

  checkStepped (scope, selectedFrame, progress) {
    return scope.currentFrame && this.speed === 0 && progress === 1
  }

  checkSteppedToNextFrame (scope, selectedFrame, progress) {
    return this.checkStepped(scope, selectedFrame, progress) &&
    ((scope.currentFrame === selectedFrame.previous && scope.currentFrame !== selectedFrame) || (scope.currentFrame === selectedFrame && scope.currentProgress !== progress))
  }

  checkSteppedToPreviousFrame (scope, selectedFrame, progress) {
    return this.checkStepped(scope, selectedFrame, progress) &&
    scope.currentFrame.previous === selectedFrame &&
    scope.currentFrame !== selectedFrame
  }

  initEndScene (scope, failure) {
    scope.endSceneViewed = false
  }

  destroyEndScene (scope) {
  }

  renderEndScene (scope, step, failure) {
    var endOfEnd
    if (scope.endTime === 0) {
      this.initEndScene(scope, failure)
    }

    scope.endTime += step

    if (scope.endTime >= endOfEnd && !scope.endSceneViewed) {
      if (this.endCallback) {
        this.endCallback()
      }
      scope.endSceneViewed = true
    }
  }

  renderScene (scope, question, frames, frameNumber, progress, speed, reason, step) {
    /** ************************************* */
    /*        ASYNCHRONOUS                    */
    /** ************************************* */
    step = Math.min(80, step)

    var endFrame = !this.debugMode && (frameNumber === frames.length - 1 && progress === 1)

    if (endFrame) {
      this.renderEndScene(scope, step, (reason !== 'Win'))
    } else {
      if (scope.endTime > 0) {
        this.destroyEndScene(scope)
      }
      scope.endTime = 0
    }

    if (this.stepByStepAnimateSpeed && this.isAsynchronousAnimationOngoing(scope)) {
      let frameToShow = this.currentFrame
      let progressToShow

      if (scope.reverseAsynchronousAnimation) {
        frameToShow++
      }

      const progressDelta = step / 200 * this.getFrameSpeed(frameToShow) * this.stepByStepAnimateSpeed

      if (scope.reverseAsynchronousAnimation) {
        progressToShow = scope.currentProgress - progressDelta
        if (progressToShow <= 0) {
          frameToShow = this.currentFrame
          progressToShow = 1
          scope.targetProgress = 1
        }
      } else {
        progressToShow = scope.currentProgress + progressDelta
        progressToShow = Math.min(scope.targetProgress, progressToShow)
      }
      this.updateScene(this.scope, this.question, this.frames, frameToShow, progressToShow, this.speed, this.reasons[frameToShow], false, true)
    }

    for (const moduleName in this.modules) {
      const module = this.modules[moduleName]
      if (typeof module.animateScene === 'function') {
        try {
          module.animateScene(step)
        } catch (e) {
          this.handleModuleError(moduleName, e)
        }
      }
    }
    return true
  }

  isAsynchronousAnimationOngoing (scope) {
    return scope.targetProgress != null && scope.currentProgress !== scope.targetProgress
  }

  getFrameSpeed (frameNumber) {
    return BASE_FRAME_DURATION / this.getFrameDuration(frameNumber)
  }

  getFrameDuration (frameNumber) {
    return (this.frames && this.frames[frameNumber] && this.frames[frameNumber].frameInfo.frameDuration) || 1000
  }

  static get RenderTimeout () { return window.location.hostname === 'localhost' ? Infinity : 20000 }

  enableAsyncRendering (enabled) {
    this.asyncRendering = enabled
    this.asyncRenderingTime = Drawer.RenderTimeout
  }

  purge () {
    this.scope = {}
    this.changed = true

    this.container.interactiveChildren = false
    this.container.destroy({
      texture: false,
      children: true
    })

    delete this.container
    this.container = new PIXI.Container()
  }

  reinitScene () {
    if (this.loaded >= 1) {
      this.destroyScene(this.scope)
      this.purge()
      this.asyncRenderingTime = Drawer.RenderTimeout
      this.preconstructScene(this.scope, this.container, this.initWidth, this.initHeight)
      this.initScene(this.scope, this.container, this.frames)
      this.updateScene(this.scope, this.question, this.frames, this.currentFrame, this.progress, this.speed, this.reasons[this.currentFrame])
      this.changed = true
    }
  }

  reinitDefaultScene () {
    if (this.loaded >= 1) {
      this.intro = true
      this.destroyScene(this.scope)
      this.purge()
      this.asyncRenderingTime = Drawer.RenderTimeout
      this.initDefaultScene(this.scope, this.container, this.initWidth, this.initHeight)
      this.changed = true
    }
  }

  reinitLoadingScene () {
    if (this.loaded < 1) {
      this.purge()
      this.asyncRenderingTime = Drawer.RenderTimeout
      this.initPreload(this.scope, this.container, this.loaded, this.initWidth, this.initHeight)
    }
  }

  reinit (force) {
    if (this.loaded >= 1 && !this.loading) {
      if (this.currentFrame >= 0 && !this.intro) {
        this.reinitScene()
      } else {
        if (!this.intro || force) { this.reinitDefaultScene() }
      }
    } else {
      this.reinitLoadingScene()
    }
  }

  animate (time) {
    if (this.destroyed) {
      return
    }

    if (!this.lastRenderTime) { this.lastRenderTime = time }
    var step = time - this.lastRenderTime
    if (this.asynchronousStep) {
      step = this.asynchronousStep
    }
    if (this.onBeforeRender) {
      this.onBeforeRender()
    }

    if (!this.loading) {
      if (this.loaded < 1) {
        this.changed |= this.renderPreloadScene(this.scope, step)
      } else if (this.changed || (this.asyncRendering && this.asyncRenderingTime > 0) || this.isAsynchronousAnimationOngoing(this.scope)) {
        if (this.currentFrame < 0) {
          this.changed |= this.renderDefaultScene(this.scope, step)
        } else if (this.intro) {
          this.changed = true
          if (this.endDefaultScene(this.scope, step)) {
            this.intro = false
            this.reinit(true)
          }
        } else {
          this.changed |= this.renderScene(this.scope, this.question, this.frames, this.currentFrame, this.progress, this.speed, this.reasons[this.currentFrame], step)
        }
      }
      if (this.changed) {
        this.renderer.render(this.container)
        this.changed = false
      }
    }
    if (this.onAfterRender) {
      this.onAfterRender()
    }
    this.lastRenderTime = time
    if (!this.destroyed) { requestAnimationFrame(this.animate.bind(this)) }

    this.asyncRenderingTime -= step
  }

  _initFrames (playerCount, frames) {
    this.instantiateModules()

    this._frames = frames.map(f => {
      const header = f[0].split(' ')

      let data
      try {
        data = JSON.parse(f.slice(1).join('\n'))
      } catch (err) {
        data = {}
      }
      return { ...data, key: header[0] === 'KEY_FRAME' }
    })

    this.parseGlobalData(this._frames[0].global)
    this.playerCount = playerCount
    this.reasons = []
    this.frames = []
    this.currentFrame = 0
    this.currentFrameDuration = 1000
    this.progress = 1
    const firstFrame = this._frames[0].frame
    firstFrame.key = this._frames[0].key
    this.frames.push(this.parseFrame(firstFrame, this.frames))
    for (var i = 1; i < this._frames.length; ++i) {
      this.frames.push(this.parseFrame(this._frames[i], this.frames))
    }
  }

  isTurnBasedGame () {
    return false
  }

  initFrames (frames, agents) {
    if (this.playerInfo) {
      this.playerInfo.forEach(function (playerInfo) {
        if (playerInfo.avatar) {
          playerInfo.avatar.destroy(true)
        }
      })
    }

    var drawer = this

    var loader = new PIXI.Loader(window.location.origin)
    this.playerInfo = agents.map(function (agent, index) {
      var agentData = {
        name: agent.name || 'Anonymous',
        color: agent.color ? drawer.parseColor(agent.color) : '#ffffff',
        number: index,
        index: agent.index,
        type: agent.type,
        isMe: agent.type === 'CODINGAMER' && agent.typeData.me,
        avatar: null
      }

      if (agent.avatar != null) {
        loader.add('avatar' + index, agent.avatar, { loadType: 2, crossOrigin: true }, function (event) {
          agentData.avatar = event.texture
          PIXI.Texture.addToCache(event.texture, '$' + agentData.index)
        })
      }
      return agentData
    })
    this.loading = true

    let completed = false
    const onComplete = function () {
      drawer._initFrames(agents.length, frames)
      drawer.loading = false
      drawer.reinit(false)
      completed = true
    }

    loader.onComplete.add(onComplete)
    loader.onError.add(function (e) {
      console.warn(e)
      // The loader won't complete now, let's just go ahead and see what happens
      if (!completed) {
        onComplete()
      }
    })
    loader.load()
  }

  update (currentFrame, progress, speed) {
    if (this.currentFrame >= 0) {
      this.asyncRenderingTime = Drawer.RenderTimeout
      this.changed = true
      this.speed = speed
      this.currentFrame = currentFrame
      this.progress = progress
      if (this.loaded >= 1 && !this.intro) {
        this.updateScene(this.scope, this.question, this.frames, currentFrame, progress, this.speed, this.reasons[this.currentFrame])
      }
    }
  }

  parseColor (color) {
    if (Array.isArray(color)) {
      var i
      var parsedColor = []
      for (i = 0; i < color.length; ++i) {
        parsedColor.push(this.parseColor(color[i]))
      }
      return parsedColor
    } else {
      return parseInt(color.substring(1), 16)
    }
  }

  init (canvas, width, height, colors, oversampling, endCallback, location) {
    var key
    window.PIXI = Drawer.PIXI || window.PIXI
    this.oversampling = oversampling || 1

    const notifyRenderer = () => {
      if (this.currentFrame >= 0) {
        this.changed = true
      }
    }

    this.canvas = $(canvas)
    this.canvas.off('mousemove')
    this.canvas.off('wheel')
    this.canvas.bind('wheel', notifyRenderer)
    this.canvas.mousemove(notifyRenderer)

    if (colors) this.colors = this.parseColor(colors)

    if (location === 'ide') {
      if (!this.debugModeSetByUser) {
        this.debugMode = true
      }
      ErrorLog.listen((err) => console.error(err.cause ? err.cause : err))
    }
    this.asyncRendering = true
    this.asyncRenderingTime = 0
    this.destroyed = false
    this.asynchronousStep = null
    var self = this
    this.initWidth = width | 0
    this.initHeight = height | 0
    this.endCallback = endCallback || this.endCallback

    if (!this.alreadyLoaded) {
      this.alreadyLoaded = true
      // Initialisation
      this.question = null
      this.scope = null
      this.currentFrame = -1
      this.loaded = 0
      // Engine instanciation
      this.container = new PIXI.Container()
      var resources = this.getResources()
      this.renderer = this.createRenderer(this.initWidth, this.initHeight, canvas)
      setRenderer(this.renderer)
      var loader = new PIXI.Loader(resources.baseUrl)
      for (key in resources.images) {
        loader.add(key, resources.images[key], { crossOrigin: true })
      }
      var i
      for (i = 0; i < resources.sprites.length; ++i) {
        loader.add(resources.sprites[i], { crossOrigin: true })
      }
      for (i = 0; i < resources.fonts.length; ++i) {
        loader.add(resources.fonts[i], { crossOrigin: true })
      }
      for (key in resources.spines) {
        loader.add(key, resources.spines[key], { crossOrigin: true })
      }
      for (i = 0; i < resources.others.length; ++i) {
        loader.add(resources.others[i], { crossOrigin: true })
      }

      if (this.demo) {
        this.demo.agents.forEach(agent => {
          loader.add('avatar' + agent.index, agent.avatar, { loadType: 2, crossOrigin: true }, function (event) {
            agent.avatarTexture = event.texture
            PIXI.Texture.addToCache(event.texture, '$' + agent.index)
          })
        })
      }

      self.scope = {}

      const onStart = function (loader, resource) {
        requestAnimationFrame(self.animate.bind(self))
        self.initPreload(self.scope, self.container, self.loaded = 0, self.initWidth, self.initHeight)
      }
      loader.onStart.add(onStart)
      loader.onProgress.add(function (loader, resource) {
        if (loader.progress < 100) {
          self.preload(self.scope, self.container, self.loaded = loader.progress / 100, self.initWidth, self.initHeight, resource)
        }
      })

      const onComplete = function () {
        var key
        for (key in resources.spines) {
          if (resources.spines.hasOwnProperty(key)) {
            PIXI.AnimCache[key] = PIXI.AnimCache[resources.baseUrl + resources.spines[key]]
          }
        }
        self.loaded = 1
        self.reinit(true)
        self.changed = true
      }

      loader.onComplete.add(onComplete)
      loader.onError.add(function (e) {
        console.warn(e)
      })

      // PIXI bug workaround: if there is nothing to load, don't even try.
      if (Object.keys(loader.resources).length) {
        loader.load()
      } else {
        onStart()
        onComplete()
      }
    } else {
      this.changed = true
      this.renderer.resize(this.initWidth, this.initHeight)
      this.reinit(true)
    }
  }

  createRenderer (width, height, canvas) {
    return PIXI.autoDetectRenderer({
      width, 
      height,
      view: canvas,
      clearBeforeRender: true,
      preserveDrawingBuffer: false
    })
  }

  isReady () {
    return this.loaded >= 1
  }
}
