import * as config from './js/config.js';
import { Drawer } from './js/Drawer.js';
import { createGameManagerFromGameInfo } from './gameManager.js'

// TODO: add tooltips

window.DEFAULT_WIDTH = 960;
window.DEFAULT_HEIGHT = 540;
window.overSampling = 2;

function fetchGame(callback) {
  let xhr = new XMLHttpRequest();
  xhr.onload = function () { 
    callback(JSON.parse(this.responseText));
  };
  xhr.open('GET', 'game.json', true);
  xhr.send();
}

function go(data) {
  window.data = data;
  window.agents = data.agents;
  window.frames = data.views.map(v => JSON.parse(v));
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

  document.getElementById("positionRange").max = frames.length;
  document.getElementById("framelabel").max = frames.length - 1;

  canvas = document.getElementsByTagName('canvas')[0];

  Drawer.PIXI = PIXI;
  d = new Drawer(canvas, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  window.d = d;

  window.gameManager = createGameManagerFromGameInfo(d, {agents: window.agents, frames: window.frames})
  window.gameManager.subscribe(updateToFrame);

  addStats(d);

  let container = $('#container');
  setInterval(function() {
    if (canvas.width !== container.width() * overSampling) {
      resize(container.width() * overSampling, container.width() * 0.5625 * overSampling);
    }
  }, 200);
  
  if (!config.demo) {
    goTo(0,1);
  }
}

var d;
var canvas;

window.init = 0;

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
    d.initFrames(frames, agents);
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

function addStats(d) {
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
}

window.setSpeed = setSpeed;
window.resize = resize;
window.nextFrame = nextFrame;
window.previousFrame = previousFrame;
window.togglePlay = togglePlay;
window.initFrames = initFrames;
window.goTo = goTo;
window.fullScreen = fullScreen;

fetchGame(go);