const errors = []
const listeners = []

export class ErrorLog {
  static listen (callback) {
    if (typeof callback === 'function') {
      listeners.push(callback)
    } else {
      throw new TypeError(callback + ' is not a function')
    }
  }

  static push (error) {
    errors.push(error)
    listeners.forEach(callback => callback(error))
  }
}

export { errors }
