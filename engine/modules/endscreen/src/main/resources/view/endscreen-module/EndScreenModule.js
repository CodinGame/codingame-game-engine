import {WIDTH, HEIGHT} from '../core/constants.js'
import {lerp, unlerp} from '../core/utils.js'
import {ErrorLog} from '../core/ErrorLog.js'
import {MissingImageError} from './errors/MissingImageError.js'

/* global PIXI */

export class EndScreenModule {
  constructor (assets) {
    this.states = []
    this.scores = []
    this.globalData = {}
    this.atEnd = false
  }

  static get name () {
    return 'endScreen'
  }

  updateScene (previousData, currentData, progress) {
    if (currentData.scores && progress === 1) {
      this.atEnd = true
    } else {
      this.atEnd = false
    }
  }

  handleFrameData (frameInfo, data) {
    let scores = null
    let spriteName = null
    if (data) {
      scores = data[0]
      spriteName = data[1]
    }
    const state = {
      number: frameInfo.number,
      scores,
      spriteName
    }
    if (scores) {
      this.scores = scores
    }
    if (spriteName) {
      this.spriteName = spriteName
    }
    this.states.push(state)
    return state
  }

  reinitScene (container, canvasData) {
    this.container = container
    this.endLayer = this.createEndScene(this)
    if (this.atEnd) {
      this.initEndScene()
    }
    this.container.addChild(this.endLayer)
  }

  animateScene (delta) {
    let step = Math.min(32, delta)

    if (this.atEnd) {
      if (!this.animationEnded) {
        this.renderEndScene(step)
      }
    } else {
      if (this.endTime > 0) {
        this.destroyEndScene()
      }
      this.endTime = 0
    }
  }

  destroyEndScene () {
    this.animationEnded = false
    this.endLayer.visible = false
  }

  initEndScene () {
    this.animationEnded = false
    this.endLayer.visible = true
  }

  renderEndScene (step) {
    var endOfEnd = 10000
    if (this.endTime === 0) {
      this.initEndScene()
    }

    var backS = 0
    var backD = 400
    var backP = unlerp(backS, backS + backD, this.endTime)
    this.endLayer.backgroundRanking.alpha = backP * 0.9

    var logoS = 400
    var logoD = 600
    var logoP = unlerp(logoS, logoS + logoD, this.endTime)
    this.endLayer.titleRanking.scale.x = this.endLayer.titleRanking.scale.y = 0.001 + lerp(10, 0.8, logoP)
    this.endLayer.titleRanking.visible = !!logoP

    var rankS = 1000
    var rankD = 300
    for (let i = 0; i < this.finishers.length; ++i) {
      var p = unlerp(rankS + rankD * i, rankS + rankD * i + rankD, this.endTime)
      this.finishers[i].alpha = p
    }

    this.endTime += step

    if (this.endTime >= endOfEnd) {
      this.animationEnded = true
    }
  }

  handleGlobalData (players, globalData) {
    this.globalData = {
      players: players,
      playerCount: players.length
    }
  }

  generateText (text, size, align, color) {
    var textEl

    textEl = new PIXI.Text(text, {
      fontSize: Math.round(size / 1.2) + 'px',
      fontFamily: 'Lato',
      fontWeight: 'bold',
      fill: color
    })
    textEl.lineHeight = Math.round(size / 1.2)
    if (align === 'right') {
      textEl.anchor.x = 1
    } else if (align === 'center') {
      textEl.anchor.x = 0.5
    }
    return textEl
  }

  createFinisher (finisher) {
    var layer = new PIXI.Container()

    var avatarContainer = new PIXI.Container()
    avatarContainer.y = 0
    avatarContainer.x = 0

    var backgroundAvatar = new PIXI.Graphics()
    backgroundAvatar.beginFill(0xffffff)
    backgroundAvatar.alpha = 0.1
    backgroundAvatar.drawRect(0, 0, 240, 120)
    avatarContainer.addChild(backgroundAvatar)

    var avatarBorder = new PIXI.Graphics()
    avatarBorder.lineStyle(1, 0xffffff)
    avatarBorder.alpha = 0.5
    avatarBorder.drawRect(0, 0, 120, 120)
    avatarContainer.addChild(avatarBorder)

    var avatar = new PIXI.Sprite(finisher.player.avatar)
    avatar.width = avatar.height = 120

    var rank = this.generateText(finisher.rank.toString(), 76, 'center', finisher.player.color)
    rank.anchor.y = 0.5
    rank.position.x = 160
    rank.position.y = 56
    avatarContainer.addChild(rank)

    var rankLetter = this.generateText(finisher.rank === 1 ? 'ST' : 'ND'.toString(), 34, 'left', finisher.player.color)
    rankLetter.position.x = 184
    rankLetter.position.y = 32
    avatarContainer.addChild(rankLetter)

    var hudAvatar = new PIXI.Container()
    hudAvatar.addChild(avatar)

    avatarContainer.addChild(hudAvatar)

    var name = this.generateText(finisher.player.name.toUpperCase(), 50, 'left', finisher.player.color)
    var scoreLabel = this.generateText(((finisher.score >= 0) ? finisher.score.toString() + ' points' : '-'), 64, 'left', finisher.player.color)

    name.x = 330
    name.y = -4
    scoreLabel.x = 330
    scoreLabel.y = 50

    layer.addChild(avatarContainer)
    layer.addChild(name)
    layer.addChild(scoreLabel)

    return layer
  }

  createEndScene () {
    var layer = new PIXI.Container()

    var background = new PIXI.Graphics()
    background.beginFill(0, 0.85)
    background.drawRect(0, 0, WIDTH, HEIGHT)
    background.endFill()

    layer.backgroundRanking = background

    let sprite = this.spriteName
    var titleRanking
    if (PIXI.utils.TextureCache.hasOwnProperty(sprite)) {
      titleRanking = new PIXI.Sprite.fromFrame(sprite)
    } else {
      ErrorLog.push(new MissingImageError(sprite))
      titleRanking = new PIXI.Sprite(PIXI.Texture.EMPTY)
    }
    titleRanking.anchor.x = titleRanking.anchor.y = 0.5
    layer.titleRanking = titleRanking

    titleRanking.position.x = WIDTH / 2
    titleRanking.position.y = 230

    var podium = []
    for (var i = 0; i < this.globalData.playerCount; ++i) {
      podium.push({
        score: this.scores[i],
        player: this.globalData.players[i],
        rank: 0
      })
    }
    podium.sort(function (a, b) {
      return b.score - a.score
    })

    this.finishers = []
    var finishers = new PIXI.Container()
    var curRank = 1
    var elem
    for (i = 0; i < podium.length; ++i) {
      if (i > 0 && podium[i - 1].score !== podium[i].score) {
        curRank++
      }

      podium[i].rank = curRank
      elem = this.createFinisher(podium[i])
      finishers.addChild(elem)
      this.finishers.push(elem)
    }

    for (i = 0; i < this.finishers.length; ++i) {
      this.finishers[i].position.x = (WIDTH - this.finishers[0].width) / 2
      this.finishers[i].position.y = i * 150
    }
    finishers.y = 400

    layer.addChild(background)
    layer.addChild(titleRanking)
    layer.addChild(finishers)

    layer.visible = false
    return layer
  }
}
