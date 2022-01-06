import * as config from '../config.js'
import './lib/cginput.js'
import './player.js'

const createCGPlayer = (opts) => {
  return window['cg-player'].default({
    ...opts,
    localStorageKey: 'ngStorage-gameParams',
    src: './player.html',
    libraries: {
      PIXI6: './lib/pixi6.js'
    }
  })
}

/* global fetch, angular, $ */

function PlayerCtrl ($scope, $timeout, $interval, $element) {
  'ngInject'

  const ctrl = this

  let cgPlayer = null
  let player = null
  let lastWidth
  let currentFrame = null

  $scope.lessOrEqualThanTwoPlayers = lessOrEqualThanTwoPlayers

  $interval(checkSize, 1000)

  $scope.isEmptyObject = function (obj) {
    return angular.equals(obj, {})
  }

  $scope.errors = {}

  $scope.isNonemptyObject = function(obj) {
    return Object.keys(obj).length > 0
  }
  $scope.hasNonemptyStderr = function(referee) {
    return referee !== undefined && referee.stderr !== undefined
  }

  function addError (error) {
    let errorText = error.message + '\n'
    if (error.cause) {
      if (typeof error.cause === 'string') {
        errorText += error.cause + '\n'
      } else {
        errorText += error.cause.message + '\n'
      }
    }
    if (!$scope.errors[errorText]) {
      $scope.errors[errorText] = { quantity: 1 }
    } else {
      $scope.errors[errorText].quantity += 1
    }
  }

  init()

  /// //////////////

  async function init () {
    cgPlayer = createCGPlayer({
      viewerUrl: '/core/Drawer.js'
    })
    cgPlayer.on('parsedGameInfo', onParsedGameInfo)
    cgPlayer.createIframe($element.find('.cg-player-sandbox')[0])
    cgPlayer.setOptions({
      showConsole: false,
      showRankings: false,
      showSmallRankings: false,
      asyncRendering: true,
      shareable: false,
      showReplayPrompt: false
    })

    cgPlayer.on('error', addError)
    loadGame()
  }

  function onParsedGameInfo (parsedGameInfo) {
    $scope.playerColors = {}
    ctrl.parsedGameInfo = parsedGameInfo
    parsedGameInfo.agents.forEach(function (agent) {
      $scope.playerColors[agent.index] = agent.color
    })
    cgPlayer.off('parsedGameInfo', onParsedGameInfo)
    const frameData = parsedGameInfo.frames[0]
    $scope.referee = { ...frameData.referee }
    $scope.summary = frameData.gameSummary
  }

  async function loadGame () {
    const response = await fetch('game.json')
    ctrl.data = await response.json()
    if (!ctrl.data) {
      return
    }
    if (ctrl.data.failCause) {
      addError({
        message: ctrl.data.failCause
      })
      return
    }

    ctrl.gameInfo = convertFrameFormat(ctrl.data)
    $scope.agents = { ...ctrl.data.agents }

    cgPlayer.sendFrames(ctrl.gameInfo)
    cgPlayer.subscribe(onUpdate)
  }

  function onUpdate (frame, progress, playing, isSubFrame, isTurnBased, atEnd) {
    if (frame !== currentFrame) {
      $timeout(() => {
        currentFrame = frame
        onFrameChange(frame)
      })
    }
  }

  function onFrameChange (frame) {
    // one frame in this method is one game turn, and contains subframes for each agent's actions
    for (const i in ctrl.data.ids) {
      $scope.agents[i].stdout = null
      $scope.agents[i].stderr = null
    }

    $scope.referee = {}
    const frameData = ctrl.parsedGameInfo.frames[frame]
    for (const i in ctrl.data.ids) {
      const subframe = frameData.subframes.find(subframe => subframe.agentId === i)
      if (subframe) {
        if (subframe.stdout) {
          $scope.agents[i].stdout = subframe.stdout
        }

        if (subframe.stderr) {
          $scope.agents[i].stderr = subframe.stderr
        }
      }
    }
    $scope.referee.stdout = frameData.referee.stdout
    $scope.referee.stderr = frameData.referee.stderr
    $scope.summary = frameData.gameSummary
  }

  function convertFrameFormat (data) {
    // one frame in this method means one output, if in a single game turn two agents act, the two actions are put in separate frames
    const frames = data.views.map(v => {
      const f = v.split('\n')
      const header = f[0].split(' ')

      return { view: v.replace(/^(KEY_FRAME)|(INTERMEDIATE_FRAME)/, ''), keyframe: header[0] === 'KEY_FRAME' }
    })
    const refereeKeysMap = { errors: 'stderr', outputs: 'stdout' }
    for (let i = 0; i < frames.length; i++) {
      frames[i].gameSummary = data.summaries[i]
      frames[i].referee = {}

      for (const key in refereeKeysMap) {
        const newKey = refereeKeysMap[key]
        if (data[key].referee[i] && data[key].referee[i].length) {
          frames[i].referee[newKey] = data[key].referee[i]
        }
      }
      for (const pi in data.ids) {
        const stdout = data.outputs[pi][i]
        const stderr = data.errors[pi][i]

        if (pi !== 'referee') {
          // If this agent has output, it is the frame's only active agent
          if (stdout || stderr) {
            frames[i].agentId = pi
          }
          if (data.errors[pi][i]) {
            // This frame's active agent has output this on stderr
            frames[i].stderr = stderr
          }
          if (data.outputs[pi][i]) {
            // This frame's active agent has output this on stdout
            frames[i].stdout = stdout
          }
        }
      }
    }
    const agents = data.agents.map(a => Object.assign(a, { avatarUrl: a.avatar }))
    const tooltips = data.tooltips.map(JSON.stringify)
    return { agents: agents, frames: frames, tooltips: tooltips }
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

  function lessOrEqualThanTwoPlayers () {
    return $scope.agents && Object.keys($scope.agents).length <= 2
  }
}

angular.module('player', ['ngStorage'])
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
        .replace(/\n/g, '<br />')

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
