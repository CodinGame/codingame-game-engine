export class MissingImageError extends Error {
  constructor (image, cause) {
    super('Could not find image: "' + image + '". Make sure it is in your assets folder and is spelled correctly.')
    this.image = image
    this.cause = cause
    this.name = 'MissingImageError'
  }
}
