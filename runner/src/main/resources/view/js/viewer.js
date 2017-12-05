import { CommandParser } from './CommandParser.js';
import config from './config.js';

/*
 * #########################################################
 * ####                                                 ####
 * ####       EDIT FROM HERE                            ####
 * ####                                                 ####
 * #########################################################
 */


/**
 * Constant for physics values
 */
var PER_SEC = 1 / 1000;

var Drawer = function () {
  this.debugMode = false;
  this.toDestroy = [];
};

Drawer.requirements = {
  PIXI: 'PIXI4'
};
Drawer.VERSION = 2;
Drawer.WIDTH = 1920;
Drawer.HEIGHT = 1080;

Drawer.prototype.destroy = function () {
  if (this.alreadyLoaded) {
    this.renderer.destroy();
    this.endCallback = null;
  }
  this.destroyed = true;
};

Drawer.prototype.destroyScene = function (scope) {
  for (var i = 0, l = this.toDestroy.length; i < l; ++i) {
    var texture = this.toDestroy[i];
    texture.destroy(true);
  }
  this.toDestroy = [];
};

/** Mandatory */
Drawer.prototype.getGameName = function () {
  return "CodinGame";
};
Drawer.prototype.canSwapPlayers = function () {
  return true;
};

Drawer.getGameRatio = function () {
  return Drawer.WIDTH / Drawer.HEIGHT;
};

/** Mandatory */
Drawer.prototype.getResources = function () {
  var url = 'https://cdn-games.codingame.com/code-of-the-rings-game/';

  return Object.assign({
    baseUrl: url,
    images: {
      bunny: 'bunny.png'
    },
    spines: {},
    sprites: [],
    fonts: [],
    others: []
  }, config);
};

Drawer.prototype.getOptions = function () {
  var drawer = this;
  return [{
    get: function () {
      return drawer.debugMode;
    },
    set: function (value) {
      drawer.debugMode = value;
      drawer.setDebug(value);
    },
    title: 'DEBUG MODE',
    values: {
      'ON': true,
      'OFF': false
    }
  }];
};

Drawer.prototype.setDebug = function (v) {
  this.asyncRenderingTime = Drawer.RenderTimeout;
};

/** Mandatory */
Drawer.prototype.initPreload = function (scope, container, progress, canvasWidth, canvasHeight) {
  scope.canvasWidth = canvasWidth;
  scope.canvasHeight = canvasHeight;

  scope.loaderProgress = new PIXI.Text('100', {
    fontSize: (canvasHeight * 0.117),
    fontFamily: 'Lato',
    fontWeight: '900',
    fill: 'white',
    align: 'center'
  });

  scope.loaderProgress.anchor.y = 1;
  scope.loaderProgress.anchor.x = 1.3;
  scope.progress = scope.realProgress = progress;
  scope.loaderProgress.position.y = canvasHeight;

  scope.progressBar = new PIXI.Graphics();
  container.addChild(scope.progressBar);
  container.addChild(scope.loaderProgress);
};

/** Mandatory */
Drawer.prototype.preload = function (scope, container, progress, canvasWidth, canvasHeight, obj) {
  scope.progress = progress;
};

/** Mandatory */
Drawer.prototype.renderPreloadScene = function (scope, step) {
  var stepFactor = Math.pow(0.998, step);
  scope.realProgress = stepFactor * scope.realProgress + (1 - stepFactor) * scope.progress;
  scope.loaderProgress.text = ((scope.realProgress * 100).toFixed(0));
  scope.loaderProgress.position.x = scope.realProgress * scope.canvasWidth;

  scope.progressBar.clear();

  scope.progressBar.beginFill(0x0, 1);
  scope.progressBar.drawRect(0, 0, scope.canvasWidth * scope.realProgress + 1, scope.canvasHeight);
  scope.progressBar.endFill();

  scope.progressBar.beginFill(0x3f4446, 1);
  scope.progressBar.drawRect(scope.canvasWidth * scope.realProgress, 0, scope.canvasWidth, scope.canvasHeight);
  scope.progressBar.endFill();
  return true;
};

/** Mandatory */
Drawer.prototype.initDefaultScene = function (scope, container, canvasWidth, canvasHeight) {
  var scene = new PIXI.Container();
  container.addChild(scene);
  container.scale.x = canvasWidth / Drawer.WIDTH;
  container.scale.y = canvasHeight / Drawer.HEIGHT;
  scope.drawer = this;
  scope.renderables = [];
  scope.time = 0;

};

/** Mandatory */
Drawer.prototype.renderDefaultScene = function (scope, step) {
  step = Math.min(80, step);

  this.renderRenderables(step, scope);

  return true;
};

Drawer.prototype.endDefaultScene = function (scope, step) {
  return true;
};

/** Mandatory */
Drawer.prototype.parseInitData = function (question, view, playerCount) {
  var line = 0;

  this.commandParser = new CommandParser();
  this.entities = new Map();
  return {};
};

/** Mandatory */
Drawer.prototype.getInitLineCount = function (frame) {
  return 1;
};

/** Mandatory */
Drawer.prototype.parseFrame = function (frame, keyFrame, previousFrames, initData) {
  var number = previousFrames.length;
  var parsedFrame = {
    number: number
  };
  parsedFrame.previous = previousFrames[previousFrames.length - 1] || parsedFrame;
  if (parsedFrame !== parsedFrame.previous) {
    parsedFrame.previous.next = parsedFrame;
  }

  if (!keyFrame) {
    return parsedFrame.previous;
  }

  if (frame.entitymanager !== undefined) {
    this.createEmptyEntityStates(number);
    for (let line of frame.entitymanager.split('\n')) {
      if (line) {
        let command = this.commandParser.parse(line);
        command.apply(this.stage, this.entities, number);
      }
    }
    this.extrapolateSubStates(number);
  }

  return parsedFrame;
};

/*
 * #########################################################
 * ####                                                 ####
 * ####       DRAWING AREA..                            ####
 * ####                                                 ####
 * #########################################################
 */

/*
 * #########################################################
 * ####                                                 ####
 * ####       END OF DRAWING AREA..        .            ####
 * ####                                                 ####
 * #########################################################
 */
/**
 * Adds pixi instance 'child' to pixi instance 'container' as a display child and as a property named 'property'
 * 
 * @returns the child instance
 */
Drawer.prototype.sub = function (container, property, child, at) {
  container[property] = child;
  if (isNaN(at)) {
    container.addChild(child);
  } else {
    container.addChildAt(child, at);
  }

  return child;
};

/**
 * Returns the scale needed to fit (srcWidth, srcHeight) inside (maxWidth, maxHeight)
 */
Drawer.prototype.fitAspectRatio = function (srcWidth, srcHeight, maxWidth, maxHeight, padding) {
  padding = padding || 0;
  return Math.min(maxWidth / (srcWidth + padding), maxHeight / (srcHeight + padding));
};

/**
 * Interacts with the developer window. For debug purposes.
 */
Drawer.prototype.customAction = function (param) {
  var scope = this.scope;
  var d = scope.drawer;
  var width = lerp(0, 1920, param);
  var height = lerp(0, 1080, param);
  var ratio = Math.min(960 / width, 540 / height);
  var canvas = document.getElementsByTagName('canvas')[0];
  canvas.width = width;
  canvas.height = height;
  canvas.style.width = width + "px";
  canvas.style.height = height + "px";
  this.init(canvas, width, height, null, 1 / ratio);

  return param;
};

Drawer.prototype.preconstructScene = function (initData, scope, container, canvasWidth, canvasHeight) {
  scope.canvasHeight = canvasHeight;
  scope.canvasWidth = canvasWidth;

  scope.renderables = [];
  scope.updatables = [];

  scope.time = 0;
  scope.endTime = 0;

  scope.initData = initData;
  scope.playerInfo = this.playerInfo;

  container.scale.x = canvasWidth / Drawer.WIDTH;
  container.scale.y = canvasHeight / Drawer.HEIGHT;
};

Drawer.prototype.initStage = function () {
  var stage = new PIXI.Container();

  this.entities.forEach((e) => {
    e.init();
    stage.addChild(e.container);
  });

  return stage;
};

/** Mandatory */
Drawer.prototype.initScene = function (scope, container, frames) {
  this.stage = this.initStage();
  container.addChild(this.stage);

};

/**
 * A few transition functions contained within [0,1] on both axes
 */
Drawer.BELL = function (x) {
  return 1 - Math.pow((x - 0.5) * 2, 2);
};
Drawer.ELASTIC = function (t) {
  var b = 0, c = 1, d = 1;
  var s = 1.70158;
  var p = 0;
  var a = c;
  if (t === 0)
    return b;
  if ((t /= d) == 1)
    return b + c;
  if (!p)
    p = d * 0.3;
  if (a < Math.abs(c)) {
    a = c;
    s = p / 4;
  } else
    s = p / (2 * Math.PI) * Math.asin(c / a);
  return a * Math.pow(2, -10 * t) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b;
};
Drawer.EASE = function (t) {
  return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
};

/** Mandatory */
Drawer.prototype.updateScene = function (scope, question, frames, frameNumber, progress, speed, reason) {
  /** ************************************* */
  /*        SYNCHRONOUS                     */
  /** ************************************* */
  var data = frames[frameNumber];
  scope.currentFrame = data;
  scope.currentProgress = progress;
  scope.reason = reason;

  for (var i = 0; i < scope.updatables.length; ++i) {
    scope.updatables[i].update(frameNumber, progress, frame, scope);
  }
  this.entities.forEach(e => e.render(frameNumber, progress, data));
};

Drawer.prototype.initEndScene = function (scope, failure) {
  scope.endSceneViewed = false;
};

Drawer.prototype.destroyEndScene = function (scope) {
};

Drawer.prototype.renderEndScene = function (scope, step, failure) {
  var endOfEnd;
  if (scope.endTime === 0) {
    this.initEndScene(scope, failure);
  }

  scope.endTime += step;

  if (scope.endTime >= endOfEnd && !scope.endSceneViewed) {
    if (this.endCallback) {
      sCallback();
    }
    scope.endSceneViewed = true;
  }
};

Drawer.prototype.renderRenderables = function (step, scope) {
  var next = [];
  for (var i = 0; i < scope.renderables.length; ++i) {
    var updatable = scope.renderables[i];
    var remove = updatable.render(step, scope);
    if (!remove) {
      next.push(updatable);
    }
  }
  scope.renderables = next;
};

/** Mandatory */
Drawer.prototype.renderScene = function (scope, question, frames, frameNumber, progress, speed, reason, step) {
  /** ************************************* */
  /*        ASYNCHRONOUS                    */
  /** ************************************* */
  step = Math.min(80, step);

  var endFrame = !this.debugMode && (frameNumber == frames.length - 1 && progress == 1);

  if (endFrame) {
    this.renderEndScene(scope, step, (reason != "Win"));
  } else {
    if (scope.endTime > 0) {
      this.destroyEndScene(scope);
    }
    scope.endTime = 0;
  }

  this.renderRenderables(step, scope);

  return true;
};

/**
 * Optional
 */
Drawer.prototype.getFrameSpeed = function (frameNumber, playerSpeed) {
  //Will be multiplied by current playerSpeed
  return 1;
};

/*
 * #########################################################
 * ####                                                 ####
 * ####         EDIT TO HERE                            ####
 * ####                                                 ####
 * #########################################################
 */

Drawer.RenderTimeout = window.location.origin === "http://localhost" ? Infinity : 20000;

Drawer.prototype.getCurrentState = function () {
  if (this.loaded >= 1) {
    if (this.currentFrame >= 0) {
      return 'game';
    } else {
      return 'startScreen';
    }
  } else {
    return 'loading';
  }
};

Drawer.prototype.enableAsyncRendering = function (enabled) {
  this.asyncRendering = enabled;
  this.asyncRenderingTime = Drawer.RenderTimeout;
};

Drawer.prototype.purge = function () {
  this.scope = {};
  this.changed = true;

  this.container.interactiveChildren = false;
  this.container.destroy({
    texture: false,
    children: true
  });

  this.container = null;
  this.container = new PIXI.Container();
};

Drawer.prototype.reinitScene = function () {
  if (this.loaded >= 1) {

    this.destroyScene(this.scope);
    this.purge();
    this.asyncRenderingTime = Drawer.RenderTimeout;
    this.preconstructScene(this.initData, this.scope, this.container, this.initWidth, this.initHeight);
    this.initScene(this.scope, this.container, this.frames);
    this.updateScene(this.scope, this.question, this.frames, this.currentFrame, this.progress, this.speed, this.reasons[this.currentFrame]);
    this.changed = true;
  }

};

Drawer.prototype.reinitDefaultScene = function () {
  if (this.loaded >= 1) {
    this.intro = true;
    this.purge();
    this.asyncRenderingTime = Drawer.RenderTimeout;
    this.initDefaultScene(this.scope, this.container, this.initWidth, this.initHeight);
    this.changed = true;
  }
};

Drawer.prototype.reinitLoadingScene = function () {
  if (this.loaded < 1) {
    this.purge();
    this.asyncRenderingTime = Drawer.RenderTimeout;
    this.initPreload(this.scope, this.container, this.loaded, this.initWidth, this.initHeight);
  }
};

Drawer.prototype.reinit = function (force) {
  if (this.loaded >= 1) {
    if (this.currentFrame >= 0 && !this.intro) {
      this.reinitScene();
    } else {
      if (!this.intro || force)
        this.reinitDefaultScene();
    }
  } else {
    this.reinitLoadingScene();
  }
};

Drawer.prototype.animate = function (time) {
  if (this.destroyed) {
    return;
  }

  if (!this.lastRenderTime)
    this.lastRenderTime = time;
  var step = time - this.lastRenderTime;
  if (this.asynchronousStep) {
    step = this.asynchronousStep;
  }
  if (this.onBeforeRender) {
    this.onBeforeRender();
  }

  //this.asyncRenderingTime = Drawer.RenderTimeout;
  if (!this.loading) {
    if (this.loaded < 1) {
      this.changed |= this.renderPreloadScene(this.scope, step);
    } else if (this.changed || (this.asyncRendering && this.asyncRenderingTime > 0)) {
      if (this.currentFrame < 0) {
        this.changed |= this.renderDefaultScene(this.scope, step);
      } else if (this.intro) {
        this.changed = true;
        if (this.endDefaultScene(this.scope, step)) {
          this.intro = false;
          this.reinit(true);
        }
      } else {
        this.changed |= this.renderScene(this.scope, this.question, this.frames, this.currentFrame, this.progress, this.speed, this.reasons[this.currentFrame], step);
      }
    }
    if (this.changed) {
      this.renderer.render(this.container);
      this.changed = false;
    } else {
      //    if (this.renderer.plugins && this.renderer.plugins.interaction)
      //      this.renderer.plugins.interaction.update();
    }
  }
  if (this.onAfterRender) {
    this.onAfterRender();
  }
  var self = this;
  this.lastRenderTime = time;
  if (!this.destroyed)
    requestAnimationFrame(this.animate.bind(this));

  this.asyncRenderingTime -= step;
};

Drawer.prototype.handleInitFrame = function (frame) {
  this.question = frame.question;
  this.currentFrame = frame.frameNumber | 0;
  this.progress = 1;
  this.initView = frame.initView;
  this.initData = this.parseInitData(this.question, this.initView, this.playerCount);
};

Drawer.prototype._initFrames = function (playerCount, frames) {
  if (frames[0][0] == '-1') {
    this.currentFrame = -1;
    return;
  }

  this._frames = frames.map(x => JSON.parse(x.join('\n'))); // compatibility: frames are splitted on \n, so we need to join them to get the string back...
  this.handleInitFrame(this._frames[0]);
  this.playerCount = playerCount;
  this.reasons = [];
  this.frames = [];
  for (var i = 0; i < this._frames.length; ++i) {
    this.frames.push(this.parseFrame(this._frames[i], this._frames[i].key, this.frames, this.initData));
  }
  this.extrapolateStates();
};

Drawer.prototype.isTurnBasedGame = function () {
  return false;
};

Drawer.prototype.initFrames = function (frames, agents) {
  if (this.playerInfo) {
    this.playerInfo.forEach(function (playerInfo) {
      if (playerInfo.avatar) {
        playerInfo.avatar.destroy(true);
      }
    });
  }

  var drawer = this;

  var loader = new PIXI.loaders.Loader(window.location.origin);
  this.playerInfo = agents.map(function (agent, index) {
    var agentData = {
      name: agent.name || 'Anonymous',
      color: drawer.parseColor(agent.color),
      number: index,
      index: agent.index,
      type: agent.type,
      isMe: agent.type === 'CODINGAMER' && agent.typeData.me,
      avatar: null
    };

    loader.add('avatar' + index, agent.avatar, { loadType: 2 }, function (event) {
      agentData.avatar = event.texture;
    });
    return agentData;
  });
  this.loading = true;
  loader.on('complete', function (loader) {
    drawer._initFrames(agents.length, frames);
    drawer.loading = false;
    drawer.reinit(false);
  });
  loader.on('error', function (e) {
    console.warn(e);
  });
  loader.load();
};

Drawer.prototype.update = function (currentFrame, progress, speed) {
  if (this.currentFrame >= 0) {
    this.asyncRenderingTime = Drawer.RenderTimeout;
    this.changed = true;
    this.speed = speed;
    this.currentFrame = currentFrame;
    this.progress = progress;
    if (this.loaded >= 1 && !this.intro) {
      this.updateScene(this.scope, this.question, this.frames, currentFrame, progress, this.speed, this.reasons[this.currentFrame]);
    }
  }
};

Drawer.prototype.parseColor = function (color) {
  if (Array.isArray(color)) {
    var i;
    var parsedColor = [];
    for (i = 0; i < color.length; ++i) {
      parsedColor.push(this.parseColor(color[i]));
    }
    return parsedColor;
  } else {
    if (color.toUpperCase() == '#ffae16'.toUpperCase()) {
      color = '#ff8f16';
    }
    return parseInt(color.substring(1), 16);
  }
};

Drawer.prototype.init = function (canvas, width, height, colors, oversampling, endCallback, location) {
  var key;
  PIXI = Drawer.PIXI || window.PIXI;
  this.oversampling = oversampling || 1;
  this.canvas = $(canvas);
  if (colors) this.colors = this.parseColor(colors);
  if (!this.debugModeSetByUser && location === 'ide') {
    this.debugMode = true;
  }

  this.asyncRendering = true;
  this.asyncRenderingTime = 0;
  this.destroyed = false;
  this.asynchronousStep = null;
  var self = this;
  this.initWidth = width | 0;
  this.initHeight = height | 0;
  this.endCallback = endCallback || this.endCallback;

  if (!this.alreadyLoaded) {
    this.toDestroy = [];
    this.alreadyLoaded = true;
    // Initialisation
    this.question = null;
    this.scope = null;
    this.currentFrame = -1;
    this.loaded = 0;
    // Engine instanciation
    this.container = new PIXI.Container();
    var resources = this.getResources();
    this.renderer = this.createRenderer(this.initWidth, this.initHeight, canvas);
    var loader = new PIXI.loaders.Loader(resources.baseUrl);
    for (key in resources.images) {
      loader.add(key, resources.images[key]);
    }
    var i;
    for (i = 0; i < resources.sprites.length; ++i) {
      loader.add(resources.sprites[i]);
    }
    for (i = 0; i < resources.fonts.length; ++i) {
      loader.add(resources.fonts[i]);
    }
    for (key in resources.spines) {
      loader.add(key, resources.spines[key]);
    }
    for (i = 0; i < resources.others.length; ++i) {
      loader.add(resources.others[i]);
    }

    self.scope = {};
    loader.on('start', function (loader, resource) {
      requestAnimationFrame(self.animate.bind(self));
      self.initPreload(self.scope, self.container, self.loaded = 0, self.initWidth, self.initHeight);
    });
    loader.on('progress', function (loader, resource) {
      if (loader.progress < 100) {
        self.preload(self.scope, self.container, self.loaded = loader.progress / 100, self.initWidth, self.initHeight, resource);
      }
    });

    loader.on('complete', function () {
      var key;
      for (key in resources.images) {
        if (resources.images.hasOwnProperty(key)) {
          PIXI.Texture.addTextureToCache(loader.resources[key].texture, key);
        }
      }
      for (key in resources.spines) {
        if (resources.spines.hasOwnProperty(key)) {
          PIXI.AnimCache[key] = PIXI.AnimCache[resources.baseUrl + resources.spines[key]];
        }
      }
      self.loaded = 1;
      self.reinit(true);
      self.changed = true;
    });
    loader.on('error', function (e) {
      console.warn(e);
    });
    loader.load();
  } else {
    this.changed = true;
    this.renderer.resize(this.initWidth, this.initHeight);
    this.reinit(true);
  }
};
Drawer.prototype.createRenderer = function (width, height, canvas) {
  return PIXI.autoDetectRenderer(width, height, {
    view: canvas,
    clearBeforeRender: true,
    preserveDrawingBuffer: false
  });
};

/**
 * Turns a graphic into a texture, saves the texture for later destruction
 */
Drawer.prototype.generateTexture = function (graphics) {
  var tex = graphics.generateTexture();
  this.toDestroy.push(tex);
  return tex;
};

/**
 * Creates a Text and keeps the texture for later destruction
 */
Drawer.prototype.generateText = function (text, size, color, align) {

  var bitmap = size * this.scope.canvasWidth / this.oversampling >= 30 * 960;
  //  var bitmap = size * this.scope.canvasWidth / 1 >= 30 * 960;
  var textEl;
  if (bitmap) {
    textEl = new PIXI.extras.BitmapText(text, {
      font: size + 'px agency_80',
      tint: color
    });
    textEl.lineHeight = size;
  } else {
    textEl = new PIXI.Text(text, {
      fontSize: Math.round(size / 1.2) + 'px',
      fontFamily: 'Lato',
      fontWeight: 'bold',
      fill: color
    });
    textEl.lineHeight = Math.round(size / 1.2);
  }
  if (align === 'right') {
    textEl.anchor.x = 1;
  } else if (align === 'center') {
    textEl.anchor.x = 0.5;
  }
  return textEl;
};
Drawer.prototype.isReady = function () {
  return this.loaded >= 1;
};


Drawer.prototype.createEmptyEntityStates = function (frameNumber) {
  this.entities.forEach(entity => {
    entity.states[frameNumber] = [];
  });
};

const entitySubFrameStateMap = {};

Drawer.prototype.extrapolateStates = function () {
  let self = this;
  this
    .entities.forEach(entity => {
      let currentFrame = self.frames[0];
      let previousState = null;
      while (currentFrame) {
        if (!entity.states[currentFrame.number]) {
          entity.states[currentFrame.number] = [];
        }
        let subStates = entity.states[currentFrame.number];
        if (!previousState && subStates.length) {
          previousState = Object.assign({}, subStates[subStates.length - 1]);
        } else if (previousState) {
          let subState = subStates[subStates.length - 1];
          if (subState) {
            Object.assign(subState, Object.assign(previousState, subState));
          } else {
            let subState = Object.assign({}, previousState);
            subState.t = 1;
            subStates.push(subState);
          }
        }
        currentFrame = currentFrame.next;
      }
    });
};

Drawer.prototype.extrapolateSubStates = function (frameNumber) {
  this
    .entities.forEach(entity => {
      if (!entity.states[frameNumber]) {
        entity.states[frameNumber] = [];
      }
      entity.states[frameNumber].sort((a, b) => a.t - b.t);


      if (!entitySubFrameStateMap[entity.id]) {
        entitySubFrameStateMap[entity.id] = Object.assign({}, entity.defaultState);
      }
      let currentState = entitySubFrameStateMap[entity.id];
      for (let state of entity.states[frameNumber]) {
        Object.assign(state, Object.assign(currentState, state));
      }

      let subStates = entity.states[frameNumber];
      if (subStates.length && subStates[subStates.length - 1].t !== 1) {
        let subState = Object.assign({}, subStates[0]);
        subState.t = 1;
        subStates.push(subState);
      }
    });
};

export { Drawer };