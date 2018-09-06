import * as config from '../config.js'
import {Drawer} from '../core/Drawer.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {demo as defaultDemo} from '../demo.js'
import Parser from './lib/Parser.js'

/* global fetch, angular, $, XMLHttpRequest */

function PlayerCtrl ($scope, $timeout, $interval, $filter, drawerFactory, gameManagerFactory, $localStorage) {
  'ngInject'
  const ctrl = this
  let player = null
  let lastWidth
  let currentFrame = null

  let playerLoadedPromise = new Promise((resolve) => {
    $scope.playerLoaded = function (playerApi) {
      ctrl.playerApi = playerApi
      resolve(playerApi)
    }
  })

  $scope.gameParams = $localStorage.$default({
    gameParams: {}
  }).gameParams
  $scope.loadGame = loadGame
  $scope.selectReplay = selectReplay
  $scope.viewReplay = viewReplay
  $scope.exportZip = exportZip
  $scope.reportItems = {}
  $scope.closeReportPopup = closeReportPopup
  $scope.closeViewReplayPopup = closeViewReplayPopup
  $scope.submitConfig = submitConfig
  $scope.lessOrEqualThanTwoPlayers = lessOrEqualThanTwoPlayers
  $scope.isReplayAvailable = isReplayAvailable

  $interval(checkSize, 1000)

  $scope.errors = ''
  ErrorLog.listen(function (error) {
    $scope.errors += error.message + '\n'
    if (error.cause) {
      $scope.errors += error.cause + '\n'
    }
  })

  init()

  /// //////////////

  function init () {
    drawerFactory.createDrawer(Drawer).then(drawer => {
      $scope.drawer = drawer
      fetchGame().then(data => {
        ctrl.data = data
        loadGame()
      })
    })
  }

  function loadGame () {
    if ($scope.gameLoaded || !ctrl.data) {
      return
    }
    $scope.gameLoaded = true
    $scope.uinput = ctrl.data.uinput
    ctrl.gameInfo = convertFrameFormat(ctrl.data)
    $scope.agents = {...ctrl.data.agents}

    ctrl.gameManager = gameManagerFactory.createGameManagerFromGameInfo($scope.drawer, ctrl.gameInfo, true)
    ctrl.gameManager.subscribe(onUpdate)

    return playerLoadedPromise.then(playerApi => {
      playerApi.initReplay(ctrl.gameManager)
    })
  }

  function onUpdate (frame, progress, playing, isSubFrame, isTurnBased, atEnd) {
    if (ctrl.gameInfo.frames[frame].keyframe && frame !== currentFrame) {
      $timeout(() => {
        currentFrame = frame
        onFrameChange(frame)
      })
    }
  }

  function onFrameChange (frame) {
    let startFrame = frame
    while (startFrame > 0 && !ctrl.gameInfo.frames[startFrame - 1].keyframe) {
      startFrame--
    }

    for (let i in ctrl.data.ids) {
      $scope.agents[i].stdout = null
      $scope.agents[i].stderr = null
      $scope.referee = {}
    }

    while (startFrame <= frame) {
      for (let i in ctrl.data.ids) {
        const stdout = ctrl.data.outputs[i][startFrame]
        if (stdout) {
          $scope.agents[i].stdout = stdout
        }
        const stderr = ctrl.data.errors[i][startFrame]
        if (stderr) {
          $scope.agents[i].stderr = stderr
        }
      }
      $scope.referee.stdout = $scope.referee.stdout || ctrl.data.outputs.referee[startFrame]
      $scope.referee.stderr = $scope.referee.stderr || ctrl.data.errors.referee[startFrame]
      $scope.summary = ctrl.data.summaries[startFrame]
      startFrame++
    }
  }

  function convertFrameFormat (data) {
    const frames = data.views.map(v => {
      let f = v.split('\n')
      let header = f[0].split(' ')

      return {view: v.replace(/^(KEY_FRAME)|(INTERMEDIATE_FRAME)/, ''), keyframe: header[0] === 'KEY_FRAME'}
    })
    for (let i = 0; i < frames.length; i++) {
      frames[i].gameSummary = data.summaries[i]
      for (let pi in data.ids) {
        frames[i].stderr = frames[i].stderr || data.errors[pi][i]
        frames[i].stdout = frames[i].stdout || data.outputs[pi][i]
      }
      frames[i].agentId = -1
    }
    const agents = data.agents.map(a => Object.assign(a, {avatarUrl: a.avatar}))
    const tooltips = data.tooltips.map(JSON.stringify)
    return {agents: agents, frames: frames, tooltips: tooltips}
  }

  function checkSize () {
    if (!player) {
      player = $('#cg-player').find('.player')
    }
    const newWidth = player.width()
    if (newWidth !== lastWidth) {
      lastWidth = newWidth
      if (ctrl.playerApi) {
        ctrl.playerApi.resize()
      }
    }
  }

  function fetchGame () {
    return new Promise((resolve, reject) => {
      let xhr = new XMLHttpRequest()
      xhr.onload = function () {
        let result = null
        try {
          const json = JSON.parse(this.responseText)
          json.agents.forEach(agent => { agent.color = Drawer.playerColors[agent.index] })
          result = json
        } catch (e) {
          console.error(e)
          reject(e)
        }
        resolve(result)
      }
      xhr.open('GET', 'game.json', true)
      xhr.send()
    })
  }
  $scope.selectProgress = 'inactive'
  async function selectReplay () {
    $scope.selectProgress = 'saving'
    await fetch('/services/save-replay')
      .then(function (response) {
        if (response.ok) {
          setIntroReplay()
          $scope.selectProgress = 'complete'
        } else {
          throw new Error(response.statusText)
        }
      })
      .catch(function (error) {
        $scope.selectProgress = 'inactive'
        $scope.reportItems = [
          {
            'type': 'ERROR',
            'message': error
          }
        ]
        $scope.showExportPopup = true
      })
  }

  function setIntroReplay () {
    ctrl.introReplayData = ctrl.data
  }

  function viewReplay () {
    drawerFactory.createDrawer(Drawer, ctrl.introReplayData).then(drawer => {
      $scope.replayDrawer = drawer
    })
    $scope.showViewReplayPopup = true
  }

  function closeViewReplayPopup () {
    $scope.showViewReplayPopup = false
  }

  function isReplayAvailable () {
    return ctrl.introReplayData || defaultDemo || config.demo
  }

  function closeReportPopup () {
    $scope.showExportPopup = false
  }

  function closeConfigForm () {
    $scope.showConfigForm = false
  }

  $scope.showExportPopup = false
  $scope.showConfigForm = false
  $scope.showViewReplayPopup = false
  async function exportZip () {
    const data = await fetch('/services/export')
      .then(function (response) {
        if (response.ok || response.status === 422) {
          return response
        } else {
          throw new Error(response.statusText)
        }
      })
      .catch(function (error) {
        $scope.reportItems = [
          {
            'type': 'ERROR',
            'message': error
          }
        ]
        $scope.showExportPopup = true
      })

    if (!data) {
      return
    }

    if (data.status === 422) {
      const text = await data.text()
      $scope.formStatement = text
      $scope.showConfigForm = true
    } else {
      const exportResponseString = await data.text()
      let exportResponse = JSON.parse(exportResponseString)

      let stubParser = new Parser()
      for (let stub in exportResponse.stubs) {
        try {
          stubParser.parse(exportResponse.stubs[stub], 0)
        } catch (e) {
          exportResponse.reportItems.push({
            'type': 'WARNING',
            'message': stub + ' Error in stub.txt',
            'details': { 'name': e.name, 'params': e.params }
          })
        }
      }

      if (exportResponse.exportStatus === 'SUCCESS') {
        exportResponse.reportItems.push({
          'type': 'SUCCESS',
          'message': 'Export success.'
        })
        let url = exportResponse.dataUrl
        let a = document.createElement('a')
        a.href = url
        a.download = 'export.zip'
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
      } else {
        exportResponse.reportItems.push({
          'type': 'FAIL',
          'message': 'Export fail.'
        })
      }
      $scope.reportItems = exportResponse.reportItems
      $scope.showExportPopup = true
      $scope.$apply()
    }
  }

  async function submitConfig (valid, config) {
    if (!valid) {
      return
    }
    if (config.type !== 'multi') {
      config.minPlayers = 1
      config.maxPlayers = 1
    }
    await fetch('/services/init-config',
      {
        body: JSON.stringify(config),
        method: 'POST'
      })
    closeConfigForm()
    exportZip()
  }

  function lessOrEqualThanTwoPlayers () {
    return $scope.agents && Object.keys($scope.agents).length <= 2
  }
}

angular.module('player')
  .controller('PlayerCtrl', PlayerCtrl)
  .directive('resizeHandle', function ($localStorage) {
    'ngInject'

    return {
      restrict: 'A',
      link: function (scope, el, attrs) {
        var rightBloc = el.parent().find('.right-bloc')
        var leftBloc = el.parent().find('.left-bloc')
        var minLeft = 510
        var config = $localStorage.$default({
          ideSplitPosition: '50%'
        })
        var position = config.ideSplitPosition

        var getPosition = function () {
          return position
        }

        var getRightCss = function () {
        // Same formula in test.css (.right-bloc)
          return 'calc(100% - ' + getPosition() + ')'
        }

        var getLeftCss = function () {
        // Same formula in test.css (.left-bloc)
          return getPosition()
        }

        var updatePosition = function () {
          leftBloc.css('right', getRightCss())
          rightBloc.css('left', getLeftCss())
          updateHandle()
        }

        var updateHandle = function () {
          el.css('left', getLeftCss())
        }

        function mouseMoveHandler (event) {
          position = Math.max(minLeft, event.clientX) + 'px'
          config.ideSplitPosition = position
          updatePosition()
        }

        el.on('mousedown', (event) => {
          event.preventDefault()
          scope.userSelect = 'none'
          el.parent().on('mousemove', mouseMoveHandler)
        })

        el.parent().on('mouseup', (event) => {
          el.parent().off('mousemove', mouseMoveHandler)
          scope.userSelect = 'auto'
        })

        angular.element(window).resize(updatePosition)
        scope.$on('$destroy', function () {
          angular.element(window).off('resize', updateHandle)
        })

        updatePosition()
      }
    }
  })
  .filter('formatConsole', function ($sce) {
    'ngInject'

    return function (input, agents) {
      if (!input) {
        return null
      }
      input = angular.element('<div/>').text(input).html()

      input = input.replace(/\xa4RED\xa4/g, '<span class="consoleError">')
        .replace(/\xa4GREEN\xa4/g, '<span class="consoleSuccess">')
        .replace(/\xa7RED\xa7/g, '</span>')
        .replace(/\xa7GREEN\xa7/g, '</span>')

      input = input.replace(/[\u0000-\u0009\u000b-\u000c\u000e-\u001F]/g, function (c) {
        return '\\' + c.charCodeAt(0)
      })

      if (agents) {
        input = input.replace(/\$([0-7])/mig, function (match, p1) {
          const agenti = parseInt(p1)
          if (agents[agenti]) {
            return $('<span>')
              .addClass('nickname')
              .css('background-color', agents[agenti].color)
              .text(agents[agenti].name)
              .prop('outerHTML')
          } else {
            return match
          }
        })
      }
      return $sce.trustAsHtml(input)
    }
  })
