export class ModuleError extends Error {
  constructor (moduleName, cause) {
    super('<Error in module "' + moduleName + '">')
    this.cause = cause
    this.name = 'ModuleError'
  }
}
