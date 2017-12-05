import { Drawer } from './js/viewer.js';

var progress;

window.DEFAULT_WIDTH = 960;
window.DEFAULT_HEIGHT = 540;





function go() {
  document.getElementById("canvasWidth").value = DEFAULT_WIDTH;
  document.getElementById("canvasHeight").value = DEFAULT_HEIGHT;
  document.getElementById("positionRange").max = frames.length - 1;
  document.getElementById("framelabel").max = frames.length - 1;


  canvas = document.getElementsByTagName('canvas')[0];

  Drawer.PIXI = PIXI;
  d = new Drawer(canvas, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  window.d = d;
  resize();

  var stats = new Stats();
  stats.setMode(2);
  stats.domElement.style.position = 'absolute';
  stats.domElement.style.right = '0px';
  stats.domElement.style.top = '0px';
  document.getElementById("container").appendChild(stats.domElement);
  d.onBeforeRender = stats.begin;
  d.onAfterRender = function () {
    stats.end();
  };
  frame = 0;
  progress = 1;

  updateButtons();
  resize(1920, 1080);

}

function updateButtons() {
  var viewerButtons = $('#viewerButtons');
  var options = d.getOptions();
  for (var i = 0; i < options.length; ++i) {
    var option = options[i];
    var div = $('<div></div>');
    div.append($('<span>' + option.title + ' :</span>'));
    var name = 'viewerOption' + i;
    for (var value in option.values) {

      var radio = $('<input type="radio" name="' + name + '" value="' + value + '" id="' + (name + value) + '"><label for="' + (name + value) + '">' + value + '</label>');
      if (option.values[value] == option.get()) {
        radio.attr('checked', true);
      }
      radio.data("option", option);
      radio.data("name", option);
      div.append(radio);
      radio.on("change", function (test) {
        var option = $(test.currentTarget).data("option");
        var value = option.values[$('input[name=' + test.currentTarget.name + ']:checked').val()];
        option.set(value);
      });

    }
    viewerButtons.append(div);
  }
}


var d;
var canvas;

var speed = 1;
var frame = -1;
var animationId;
var animationId = -1;
var lastRender = -1;
var init = 0;
var data = window.data;
var frames = data.views;
var agents = [{
  index: 0,
  name: 'Antoine[Amadeus]',
  avatar: 'https://static.codingame.com/servlet/fileservlet?id='+1715936252943+'&format=viewer_avatar',
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
  avatar: 'https://static.codingame.com/servlet/fileservlet?id='+1717001354716+'&format=viewer_avatar',
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
    if (text != null)
      document.getElementById("errors").value += text;
  }

  if (data.errors.referee[id] != null) {
    document.getElementById("refereeerrors").value = data.errors.referee[id];
  } else {
    document.getElementById("refereeerrors").value = "";
  }
}

function updateToFrame(_frame, _progress, _speed) {
  initFrames();
  frame = Math.max(0, Math.min(_frame, frames.length - 1));
  progress = Math.max(0, Math.min(_progress, 1));
  document.getElementById("positionRange").value = frame + _progress;
  document.getElementById("framelabel").value = frame;
  updateText(frame);
  d.update(frame, progress, _speed);
}

function enabledAsyncRendering(enabled) {
  d.enableAsyncRendering(enabled);
}

function destroyViewer() {
  d.destroy();
}

function initFrames() {
  if (init == 0) {
    var _frames = []
    for (var i = 0; i < frames.length; ++i) {
      _frames.push(frames[i].split("\n"));
    }
    d.initFrames(_frames, agents.slice(0, window.playerCount));
    init = 1;
  }
}

function startAnimation() {
  animationId = requestAnimationFrame(function (time) {
    lastRender = time;
    _animate(time);
  });
}


function _animate(time) {
  if (progress == 1) {
    progress = 0.00000000000000001;
    do {
      frame++;
    } while (frame < frames.length - 1 && (frames[frame].indexOf("KEY_FRAME") != 0 && frames[frame].indexOf("\"key\":true") === -1));
  }
  if (frame >= frames.length) {
    animationId = -1;
    updateToFrame(frames.length - 1, 1, 0);
    return;
  }
  var step = Math.min(32, (time - lastRender)) / 1000 * 2 * speed;
  
  progress += step * (d.getFrameSpeed ? d.getFrameSpeed(frame, speed) : 1);
  if (progress > 1)
    progress = 1;
  updateToFrame(frame, progress, speed);
  lastRender = time;
  animationId = requestAnimationFrame(_animate);
}

function stop() {
  if (animationId >= 0) {
    cancelAnimationFrame(animationId);
    animationId = -1;
    updateToFrame(frame, progress, 0);
  }

}

function frame0() {
  stop();
  updateToFrame(0, progress = 1, 0);

}

function nextFrame() {
  stop();
  updateToFrame(progress >= 1 ? frame + 1 : frame, 1, 0);
}

function previousFrame() {
  stop();
  updateToFrame(frame - 1, 1, 0);
}

function resize(width, height) {
  if (!width || !height) {
    width = document.getElementById("canvasWidth").value;
    height = document.getElementById("canvasHeight").value;
  }
  document.getElementById("canvasWidth").value = width;
  document.getElementById("canvasHeight").value = height;
  var ratio = Math.min(DEFAULT_WIDTH / width, DEFAULT_HEIGHT / height);
  canvas.width = width;
  canvas.height = height;
  canvas.style.width = width + "px";
  canvas.style.height = height + "px";
  d.init(canvas, width, height, agents.map(function (agent) {
    return agent.color;
  }), 1 / ratio);
  canvas.style.width = (ratio * width) + "px";
  canvas.style.height = (ratio * height) + "px";

  if (frame >= 0 && animationId >= 0) {
    updateToFrame(frame, 1, 0);
  }
}

window.resize = resize;
window.nextFrame = nextFrame;
window.frame0 = frame0;
window.stop = stop;
window.previousFrame = previousFrame;
window.startAnimation = startAnimation;
window.updateToFrame = updateToFrame;
window.enabledAsyncRendering = enabledAsyncRendering;
window.destroyViewer = destroyViewer;
window.initFrames = initFrames;


go();