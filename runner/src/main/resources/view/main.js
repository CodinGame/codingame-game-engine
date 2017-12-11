import * as config from './js/config.js';
import { Drawer } from './js/Drawer.js';
import { createGameManagerFromGameInfo } from './gameManager.js'

// TODO: add tooltips

var progress;

window.DEFAULT_WIDTH = 960;
window.DEFAULT_HEIGHT = 540;
window.overSampling = 2;

function go() {
  document.getElementById("positionRange").max = frames.length;
  document.getElementById("framelabel").max = frames.length - 1;

  canvas = document.getElementsByTagName('canvas')[0];

  Drawer.PIXI = PIXI;
  d = new Drawer(canvas, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  window.d = d;

  window.gameManager = createGameManagerFromGameInfo(d, {agents: window.agents, frames: window.frames})
  window.gameManager.subscribe(updateToFrame);

  var stats = new Stats();
  stats.setMode(2);
  stats.domElement.style.position = 'absolute';
  stats.domElement.style.right = '0px';
  stats.domElement.style.top = '0px';
  $("body").append(stats.domElement);
  d.onBeforeRender = stats.begin;
  d.onAfterRender = function () {
    stats.end();
  };

  resize(1920, 1080);

  let container = $('#container');
  setInterval(function() {
    if (canvas.width !== container.width() * overSampling) {
      resize(container.width() * overSampling, container.width() * 0.5625 * overSampling);
    }
  }, 200);
}

var d;
var canvas;

window.speed = 1;
window.animationId = -1;
window.lastRender = -1;
window.init = 0;
window.data = window.data;
window.frames = data.views.map(v => JSON.parse(v));
window.agents = [{
  index: 0,
  name: '[CG]Nonofr',
  avatar: 'https://static.codingame.com/servlet/fileservlet?id=' + 1715936252943 + '&format=viewer_avatar',
  // avatar: '/servlet/A.png',
  type: 'CODINGAMER',
  color: '#ffae16',
  typeData: {
    me: true,
    nickname: '[CG]Nonofr'
  }
}, {
  index: 1,
  name: 'Index_1',
  avatar: 'https://static.codingame.com/servlet/fileservlet?id=' + 1717001354716 + '&format=viewer_avatar',
  // avatar: '/servlet/B.png',
  type: 'CODINGAMER',
  color: '#ff1d5c',
  typeData: {
    me: true,
    nickname: '[CG]Maxime'
  }
}, {
  index: 2,
  name: 'Player 3',
  avatar: '/servlet/fileservlet?id=' + 1719001703601 + '&format=viewer_avatar',
  type: 'CODINGAMER',
  color: '#22a1e4',
  typeData: {
    me: false,
    nickname: null
  }
}, {
  index: 3,
  name: 'Player 4',
  avatar: '/servlet/fileservlet?id=' + 1719285195844 + '&format=viewer_avatar',
  type: 'CODINGAMER',
  color: '#de6ddf',
  typeData: {
    me: false,
    nickname: null
  }
}, {
  index: 4,
  name: 'Player 5',
  avatar: '/servlet/fileservlet?id=' + 1719285195844 + '&format=viewer_avatar',
  type: 'CODINGAMER',
  color: '#9975e2',
  typeData: {
    me: false,
    nickname: null
  }
}, {
  index: 5,
  name: 'Player 6',
  avatar: '/servlet/fileservlet?id=' + 1719285195844 + '&format=viewer_avatar',
  type: 'CODINGAMER',
  color: '#ff0000',
  typeData: {
    me: false,
    nickname: null
  }
}, {
  index: 6,
  name: 'Player 7',
  avatar: '/servlet/fileservlet?id=' + 1719285195844 + '&format=viewer_avatar',
  type: 'CODINGAMER',
  color: '#6ac371',
  typeData: {
    me: false,
    nickname: null
  }
}, {
  index: 7,
  name: 'Player 8',
  avatar: '/servlet/fileservlet?id=' + 1719285195844 + '&format=viewer_avatar',
  type: 'CODINGAMER',
  color: '#3ac5ca',
  typeData: {
    me: false,
    nickname: null
  }
}];

let idx = 0;
for (const player of config.players) {
  agents[idx].name = player.name;
  agents[idx].avatar = player.avatar;
  if (++idx >= agents.length) {
    break;
  }

};

console.log(data.tooltips ? JSON.stringify(data.tooltips) : 'no tooltips');
var uinput = {};
if (data.uinput) {
  var tab = data.uinput[0].trim().split('\n');
  for (var k = 0; k < tab.length; ++k) {
    var elem = tab[k].split('=');
    uinput[elem[0]] = elem[1];
  }
}
console.log(uinput);

function updateText(id) {
  document.getElementById("stdout").value = '';
  for (var i in data.ids) {
    var text = data.outputs[i][id];
    if (text != null)
      document.getElementById("stdout").value += text;
  }

  document.getElementById("console").value = "Frame " + id + "/" + (frames.length - 1) + "\n" + data.outputs.referee[id] + '\n' + (data.summaries && data.summaries[id]);

  document.getElementById("errors").value = '';
  for (var i in data.ids) {
    var text = data.errors[i][id];
    if (text != null) {
      document.getElementById("errors").value += text;
    }
  }
}

function updateToFrame(_frame, _progress, _playing, isSubFrame, isTurnBased, atEnd) {
  initFrames();
  let frame = Math.max(0, Math.min(_frame, frames.length - 1));
  let progress = Math.max(0, Math.min(_progress, 1));
  document.getElementById("positionRange").value = frame;
  document.getElementById("framelabel").value = frame;
  updateText(frame);
  d.update(frame, progress, 1);
}

function initFrames() {
  if (init == 0) {
    d.initFrames(frames, agents.slice(0, window.playerCount));
    init = 1;
  }
}

function startAnimation() {
  gameManager.play();
}

function stop() {
  gameManager.pause();  
}

function togglePlay() {
  gameManager.togglePlay();
}

function nextFrame() {
  gameManager.pause();
  gameManager.next();
}

function previousFrame() {
  gameManager.pause();
  gameManager.prior();
}

function resize(width, height) {
  console.log(width, height)
  canvas.width = width;
  canvas.height = height;
  d.init(canvas, canvas.width, canvas.height, agents.map(function (agent) {
    return agent.color;
  }), overSampling);
}

function setSpeed(speed) {
  gameManager.setSpeed(speed);
  document.getElementById('speedlabel').value = speed;
}

function goTo(frame, progress) {
  let f = frame, p = progress;
  if (frame === -1) {
    f = frames.length - 1;
  }
  if (progress === 0) {
    f = frame - 1;
    p = 1;
  }
  gameManager.pause();
  gameManager.setFrame(f, p * 100);
}

function fullScreen() {  
  let fs = canvas.webkitRequestFullScreen || canvas.mozRequestFullScreen || canvas.requestFullscreen;
  if (fs) {
    fs.apply(canvas);
  }
}

window.setSpeed = setSpeed;
window.resize = resize;
window.nextFrame = nextFrame;
window.previousFrame = previousFrame;
window.togglePlay = togglePlay;
window.initFrames = initFrames;
window.goTo = goTo;
window.fullScreen = fullScreen;

go();