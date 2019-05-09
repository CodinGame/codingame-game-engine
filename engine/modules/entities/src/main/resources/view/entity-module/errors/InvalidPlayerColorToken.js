export class InvalidPlayerColorToken extends Error {
  constructor (playerIdx, cause) {
    super(`Invalid color token: there is no player at index ${playerIdx}. Make sure to use getColorToken() if you want the color of a specific player.`)
    this.playerIdx = playerIdx
    this.cause = cause
    this.name = 'InvalidPlayerColorTokenError'
  }
}
