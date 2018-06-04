import * as config from '../config.js'
import {Drawer} from '../core/Drawer.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {demo} from '../demo.js'
import Parser from './lib/Parser.js'

/* global fetch, angular, Blob, $, XMLHttpRequest */

function PlayerCtrl ($scope, $timeout, $interval, $translate, drawerFactory, gameManagerFactory, $localStorage) {
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
  $scope.exportZip = exportZip
  $scope.reportItems = {}
  $scope.closeReportPopup = closeReportPopup
  $scope.submitConfig = submitConfig

  $interval(checkSize, 1000)

  $scope.errors = ''
  ErrorLog.listen(function (error) {
    $scope.errors += error.message + '\n'
  })

  init()

  /// //////////////

  function init () {
    drawerFactory.createDrawer(Drawer).then(drawer => {
      $scope.drawer = drawer
      fetchGame().then(data => {
        ctrl.data = data
        if (!demo && !config.demo) {
          loadGame()
        }
      })
    })
  }

  function loadGame () {
    if ($scope.gameLoaded || !ctrl.data) {
      return
    }
    $scope.gameLoaded = true
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
      $scope.summary = convertNameTokens(ctrl.data.summaries[startFrame])
      startFrame++
    }
  }

  function convertNameTokens (value) {
    return value && value.replace(/\$(\d)/g, 'Player $1')
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

  function closeReportPopup () {
    $scope.showExportPopup = false
  }

  function closeConfigForm () {
    $scope.showConfigForm = false
  }

  $scope.showExportPopup = false
  $scope.showConfigForm = false
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
        let url = window.URL.createObjectURL(base64ToBlob(exportResponse.data))
        let a = document.createElement('a')
        a.href = url
        a.download = 'export.zip'
        a.click()
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

  function base64ToBlob (base64) {
    let binaryString = window.atob(base64)
    let len = binaryString.length
    let bytes = new Uint8Array(len)
    for (let i = 0; i < len; i++) {
      bytes[i] = binaryString.charCodeAt(i)
    }
    return new Blob([bytes])
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
}

angular.module('player').controller('PlayerCtrl', PlayerCtrl)
