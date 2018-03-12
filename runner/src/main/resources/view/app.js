import * as config from '../config.js';
import {Drawer} from '../core/Drawer.js';
import {ErrorLog} from '../core/ErrorLog.js';

function PlayerCtrl($scope, $timeout, $interval, $translate, drawerFactory, gameManagerFactory, $localStorage) {
  'ngInject';
  const ctrl = this;
  let player = null;
  let lastWidth;
  let currentFrame = null;

  let playerLoadedPromise = new Promise((resolve) => {
    $scope.playerLoaded = function(playerApi) {
      ctrl.playerApi = playerApi;
      resolve(playerApi);
    };
  });

  $scope.gameParams = $localStorage.$default({
    gameParams: {}
  }).gameParams;
  $scope.loadGame = loadGame;
  $scope.selectReplay = selectReplay;

  $interval(checkSize, 1000);

  $scope.errors = "";
  ErrorLog.listen(function(error) {
    $scope.errors += error.message + '\n';
  });

  init();

  /////////////////

  async function init() {
    const demoData = await fetchGame({demo: true});

    if (demoData) {
      const frames = demoData.views;
      const agents = demoData.agents;
      const logo = 'logo.png';
      Drawer.demo = {
        playerCount: agents.length,
        logo,
        overlayAlpha: 0.2,
        agents,
        frames
      };
    }



    drawerFactory.createDrawer(Drawer).then(drawer => {
      $scope.drawer = drawer;
      let data = fetchGame().then(data => {
        ctrl.data = data;
        if (!Drawer.demo) {
          loadGame();
        }
      });
    });
  }

  function loadGame() {
    if ($scope.gameLoaded || !ctrl.data) {
      return;
    }
    $scope.gameLoaded = true;
    ctrl.gameInfo = convertFrameFormat(ctrl.data);
    $scope.agents = {...ctrl.data.agents};

    ctrl.gameManager = gameManagerFactory.createGameManagerFromGameInfo($scope.drawer, ctrl.gameInfo, true);
    ctrl.gameManager.subscribe(onUpdate);

    return playerLoadedPromise.then(playerApi => {
      playerApi.initReplay(ctrl.gameManager);
      playerApi.initReplay(ctrl.gameManager);
    });
  }

  function onUpdate(frame, progress, playing, isSubFrame, isTurnBased, atEnd) {
    if (frame !== currentFrame) {
      $timeout(() => {
        currentFrame = frame;
        onFrameChange(frame);
      });
    }
  }

  function onFrameChange(frame) {
    let startFrame = frame;
    while (startFrame > 0 && !ctrl.gameInfo.frames[startFrame - 1].keyframe) {
      startFrame--;
    }

    for (var i in ctrl.data.ids) {
      $scope.agents[i].stdout = null;
      $scope.referee = {};
    }

    while (startFrame <= frame) {
      for (var i in ctrl.data.ids) {
        const stdout = ctrl.data.outputs[i][startFrame];
        if (stdout) {
          $scope.agents[i].stdout = stdout;
        }
        const stderr = ctrl.data.errors[i][startFrame];
        if (stderr) {
          $scope.agents[i].stderr = stderr;
        }
      }
      $scope.referee.stdout = $scope.referee.stdout || ctrl.data.outputs.referee[startFrame];
      $scope.summary = ctrl.data.summaries[startFrame];
      startFrame++;
    }
  }

  function convertFrameFormat(data) {
    const frames = data.views.map(v => {
      let f = v.split('\n');
      let header = f[0].split(' ');

      return {view: v.replace(/^(KEY_FRAME)|(INTERMEDIATE_FRAME)/, ''), keyframe: header[0] === 'KEY_FRAME'};
    });
    for (let i = 0; i < frames.length; i++) {
      frames[i].gameSummary = data.summaries[i];
      for (var pi in data.ids) {
        frames[i].stderr = frames[i].stderr || data.errors[pi][i];
        frames[i].stdout = frames[i].stdout || data.outputs[pi][i];
      }
      frames[i].agentId = -1;
    }
    const agents = data.agents.map(a => Object.assign(a, {avatarUrl: a.avatar}));
    const tooltips = data.tooltips.map(JSON.stringify);
    return {agents: agents, frames: frames, tooltips: tooltips};
  }

  function checkSize() {
    if (!player) {
      player = $('#cg-player').find('.player');
    }
    const newWidth = player.width();
    if (newWidth !== lastWidth) {
      lastWidth = newWidth;
      if (ctrl.playerApi) {
        ctrl.playerApi.resize();
      }
    }
  }

  function fetchGame({demo} = {demo: false}) {
    return new Promise((resolve, reject) => {
      let xhr = new XMLHttpRequest();
      xhr.onload = function() {
        let result = null;
        if (demo && this.status === 404) {
          result = null;
        } else {
          try {
            const json = JSON.parse(this.responseText);
            json.agents.forEach(agent => agent.color = Drawer.playerColors[agent.index]);
            result = json;
          } catch (e) {
            console.error(e);
            result = null;
          }
        }
        resolve(result);
      };
      xhr.open('GET', (demo ? 'demo.json' : 'game.json'), true);
      xhr.send();
    });
  }

  $scope.selectProgress = 'inactive';
  async function selectReplay() {
    $scope.selectProgress = 'saving';
    const response = await fetch('/services/save-replay');
    $scope.selectProgress = 'complete';
  }
}

angular.module('player').controller('PlayerCtrl', PlayerCtrl);