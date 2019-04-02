export class DuplicateToggleValueError extends Error {
  constructor (toggle, cause) {
    super('Duplicate values in toggle "' + toggle + '". Make sure it is set up correctly in your config.js.')
    this.toggle = toggle
    this.cause = cause
    this.name = 'DuplicateToggleValueError'
  }
}
