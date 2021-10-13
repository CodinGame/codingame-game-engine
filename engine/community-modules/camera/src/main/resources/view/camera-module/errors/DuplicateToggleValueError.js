export class DuplicateToggleValueError extends Error {
  constructor (toggle, dup, cause) {
    super('(' + dup.keys.toString() + ') have the same value \'' + dup.value + '\' in toggle "' + toggle + '". Avoid those duplicates in your config.js.')
    this.toggle = toggle
    this.cause = cause
    this.name = 'DuplicateToggleValueError'
  }
}
