export class MissingToggleError extends Error {
  constructor (toggle, cause) {
    super('Could not find toggle: "' + toggle + '". Make sure it is set up correctly in your config.js.')
    this.toggle = toggle
    this.cause = cause
    this.name = 'MissingToggleError'
  }
}
