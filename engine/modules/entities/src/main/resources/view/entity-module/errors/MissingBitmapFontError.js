export class MissingBitmapFontError extends Error {
  constructor (font, cause) {
    super('Could not find font: "' + font + '". Make sure it is in your assets folder and is spelled correctly.')
    this.font = font
    this.cause = cause
    this.name = 'MissingBitmapFontError'
  }
}
