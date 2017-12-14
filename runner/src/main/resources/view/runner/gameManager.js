var GameManager = function(drawer, gameId, agents, frames, tooltips, refereeInput) {
  this.drawer = drawer;
  this.gameId = gameId;
  this.agents = agents;
  this.tooltips = tooltips;
  this.frames = frames;
  this.refereeInput = refereeInput;
  this.speed = 1;
  this.playing = false;
  this.progress = 100;
  this.listeners = [];
  this.eventListeners = {
    pause: [],
    play: []
  };
  this.currentFrame = 0;
  this.views = frames.map(function(frame) {
    var prefix = frame.keyframe?'KEY_FRAME':'INTERMEDIATE_FRAME';
    return prefix+frame.view;
  });

  this.enableInteractions(true);
};

GameManager.prototype.getFrameCount = function() {
  return this.frames.length;
};

GameManager.prototype.updateListeners = function(isSubFrame) {
  var self = this;
  this.listeners.forEach(function(listener) {
    listener(self.currentFrame, self.progress / 100, self.playing, isSubFrame, self.isTurnBasedGame(), self.atEnd());
  });
};

GameManager.prototype.startAnimation = function() {
  this.lastTime = null;
  this.loop();
};

GameManager.prototype.loop = function() {
  var self = this;
  this.timer = window.requestAnimationFrame(function(timestamp) {
    self.animate(timestamp);
    if (self.playing) {
      self.loop();
    }
  });
};

GameManager.prototype.animate = function(timestamp) {
  if (this.playing) {
    if(this.lastTime === null) {
      this.lastTime = timestamp;
    }
    var elapsed = timestamp - this.lastTime;
    this.lastTime = timestamp;

    var currentFrameIdx = (this.progress === 100) ? this.getNextKeyFrameIndex() : this.currentFrame;
    var currentframeSpeed = this.drawer.getFrameSpeed(currentFrameIdx, this.speed);
    var baseFrameDuration = this.drawer.getFrameDuration(currentFrameIdx, this.speed);
    var delta = this.speed * 100 * elapsed / baseFrameDuration * currentframeSpeed;
    var timeCrop = ((this.progress + delta) % (this.speed * 10));
    var isSubFrame = true;
    if (timeCrop < delta) {
      isSubFrame = false;
    }

    if (this.progress < 100 && this.progress + delta > 100) {
      this.progress = 100;
      isSubFrame = false;
    } else {
      this.progress += delta;
    }

    if ((delta > 100) || this.progress > 100) {
      this.nextKeyFrame();

      if (delta > 100) {
        this.progress = 100;
      } else {
        this.progress = this.progress - 100;
      }
    }

    if (this.currentFrame <= this.getFrameCount() + 1 && !this.atEnd()) {
      this.updateListeners(isSubFrame);
    } else {
      this.playing = false;
      this.updateListeners();
    }
  }
};

GameManager.prototype.getGameId = function () {
  return this.gameId || null;
};

GameManager.prototype.interrupt = function() {
  if (this.timer) {
    window.cancelAnimationFrame(this.timer);
    this.timer = null;
  }
  this.playing = false;
};

GameManager.prototype.destroy = function() {
  this.interrupt();
};

GameManager.prototype.getNextKeyFrameIndex = function() {
  var currentFrame = this.currentFrame;
  do {
    currentFrame++;
  } while (currentFrame < this.getFrameCount() && !this.isKeyFrame(currentFrame));

  if (currentFrame >= this.getFrameCount()) {
    currentFrame = this.getFrameCount() - 1;
  }

  return currentFrame;
};

GameManager.prototype.nextKeyFrame = function() {
  if (this.currentFrame >= this.getFrameCount() - 1) {
    this.progress = 100;
  } else {
    this.currentFrame = this.getNextKeyFrameIndex();
  }
};

GameManager.prototype.getPreviousKeyFrameIndex = function() {
  var currentFrame = this.currentFrame;
  do {
    currentFrame--;
  } while (currentFrame >= 0 && !this.isKeyFrame(currentFrame));

  if (currentFrame < 0) {
    currentFrame = 0;
  }

  return currentFrame;
};

GameManager.prototype.previousKeyFrame = function() {
  this.currentFrame = this.getPreviousKeyFrameIndex();
  if (this.currentFrame === 0) {
    this.progress = 100;
  }
};


// ------------------------ API ------------------------


GameManager.prototype.enableInteractions = function(enabled) {
  this.interactionsEnabled = enabled;
};

GameManager.prototype.areInteractionsEnabled = function() {
  return this.interactionsEnabled;
};


GameManager.prototype.atEnd = function() {
  return this.currentFrame >= this.getFrameCount() - 1 && this.progress >= 100;
};

GameManager.prototype.subscribe = function(callback) {
  this.listeners.push(callback);
};


GameManager.prototype.on = function(event, callback) {
  this.eventListeners[event].push(callback);
};

GameManager.prototype.off = function(event, callback) {
  var index = this.eventListeners[event].indexOf(callback);
  if(index >= 0) {
    this.eventListeners[event].splice(index, 1);
  }
};

GameManager.prototype.triggerEvent = function(eventName, event) {
  this.eventListeners[eventName].forEach(function(fn) {
    fn(event);
  });
};

GameManager.prototype.unsubscribe = function(callback) {
  var index = this.listeners.indexOf(callback);
  if(index >= 0) {
    this.listeners.splice(index, 1);
  }
};

GameManager.prototype.clear = function() {
  this.listeners = [];
};

GameManager.prototype.togglePlay = function(userInteraction) {
  if(!this.interactionsEnabled) {
    return;
  }
  if (!this.playing) {
    this.play(userInteraction);
  } else {
    this.pause(userInteraction);
  }
};

GameManager.prototype.play = function(userInteraction) {
  if(!this.interactionsEnabled) {
    return;
  }
  if (!this.playing) {
    if (!this.atEnd()) {
      if (this.currentFrame === 0) {
        this.progress = 100;
      }
      this.playing = true;
      this.updateListeners();
      this.startAnimation();
    } else if (this.currentFrame > 0) {
      this.currentFrame = 0;
      this.play();
    }

    this.triggerEvent('play', {userInteraction: userInteraction || false, frame: this.currentFrame});
  }
};

GameManager.prototype.isPlaying = function() {
  return this.playing;
};

GameManager.prototype.pause = function(userInteraction) {
  if(!this.interactionsEnabled) {
    return;
  }
  if (this.playing) {
    this.interrupt();
    this.updateListeners();
    this.triggerEvent('pause', {userInteraction: userInteraction || false, frame: this.currentFrame});
  }
};

GameManager.prototype.isTurnBasedGame = function() {
  return this.drawer && this.drawer.isTurnBasedGame();
};

GameManager.prototype.next = function() {
  if(!this.interactionsEnabled) {
    return;
  }
  if(!this.isTurnBasedGame()) {
    this.interrupt();
    this.nextKeyFrame();
    this.progress = 100;
    this.updateListeners();
  } else {
    this.setFrame(this.currentFrame + 1);
  }
};

GameManager.prototype.isKeyFrame = function(frameIndex) {
  return !this.frames[frameIndex] || this.frames[frameIndex].keyframe;
};


GameManager.prototype.prior = function() {
  if(!this.interactionsEnabled) {
    return;
  }
  if(!this.isTurnBasedGame()) {
    this.interrupt();
    this.previousKeyFrame();
    this.progress = 100;
    this.updateListeners();
  } else {
    this.setFrame(this.currentFrame - 1);
  }
};

GameManager.prototype.first = function() {
  if(!this.interactionsEnabled) {
    return;
  }
  this.setFrame(0);
};

GameManager.prototype.last = function() {
  if(!this.interactionsEnabled) {
    return;
  }
  this.setFrame(this.getFrameCount() - 1);
};

GameManager.prototype.setSpeed = function(speed) {
  if (speed > 0 && speed <= 100) {
    this.speed = speed;
  }
};

GameManager.prototype.getSpeed = function() {
  return this.speed;
};

GameManager.prototype.setFrame = function(frame, progress) {
  if(!this.interactionsEnabled) {
    return;
  }
  this.interrupt();
  if (frame >= 0 && frame < this.getFrameCount()) {
    this.currentFrame = frame;
  }
  if (progress === undefined || !this.isKeyFrame(frame)) {
    progress = 100;
  }

  this.progress = progress;
  this.updateListeners();
};


function createGameManagerFromGameInfo(drawer, gameInfo) {
  let playerColors = ['#ff8f16', '#ff1d5c', '#22a1e4', '#de6ddf', '#9975e2', '#ff0000', '#6ac371', '#3ac5ca'];  

  function getIDECodinGamer() {
    return codingamer = {
      pseudo: 'IDE CODE',
      avatar: null,
      userId: null
    };
  }

  var agents = null;
  if (gameInfo.agents) {
    agents = gameInfo.agents.map(function(agent) {
      var codingamer = agent.codingamer;
      return {
        arenaboss: null,
        index: agent.index,
        position: agent.position || (gameInfo.ranks ? gameInfo.ranks.indexOf(agent.index) : agent.index),
        userId: (codingamer && codingamer.userId) || null,
        name: (codingamer && codingamer.pseudo) || null,
        score: agent.score,
        agentId: agent.agentId,
        gameScore: (gameInfo.scores) ? gameInfo.scores[agent.index] : null,
        avatar: (codingamer && codingamer.avatar) || null,
        color: playerColors[agent.index],
        rank: agent.rank
      };
    });
  } else {
    var codingamer = getIDECodinGamer();

    agents = [{
      index: 0,
      agentId: -1,
      score: gameInfo.scores[0],
      name: (codingamer && codingamer.pseudo) || null,
      userId: (codingamer && codingamer.userId) || null,
      avatar: (codingamer && codingamer.avatar) || null
    }];
  }
  var frames = gameInfo.frames.map(function(frame) {
    return Object.assign({}, frame);
  });

  var _views = frames.map(frame => frame.view);

  while(frames.length < _views.length) {
    frames.push({
      agent: null,
      gameInformation: null,
      keyframe: true,
      view: null
    });
  }
  frames.forEach(function(frame, index) {
    frame.agent = agents[frame.agentId] || null;
    frame.view = _views[index];
  });

  return new GameManager(drawer, gameInfo.gameId, agents, frames, gameInfo.tooltips || [], gameInfo.refereeInput);
}

export { createGameManagerFromGameInfo }