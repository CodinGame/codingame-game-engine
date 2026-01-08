export class PIXILoader {
  constructor() {
    this.toLoad = []
    this.loadedResources = {}
    this.handleComplete = () => {}
    this.handleError = () => {}
    this.handleStart = () => {}
    this.handleProgress = () => {}
  }

  add(key, url, callback) {
    if (url == null) {
      url = key
    }
    this.toLoad.push({ key, url, callback})
  }

  onComplete(callback) {
    this.handleComplete = callback
  }

  onError(callback) {
    this.handleError = callback
  }

  onStart(callback) {
    this.handleStart = callback
  }

  onProgress(callback) {
    this.handleProgress = callback
  }

  load() {
    void this.loadAsync()
  }

  async loadAsync() {
    this.handleStart()
    const total = this.toLoad.length
    let loaded = 0

    try {
      for (const item of this.toLoad) {
        const resource = await PIXI.Assets.load(
          {
            alias: item.key,
            src: item.url
          }
        )
        this.loadedResources[item.key] = resource
        loaded++

        this.handleProgress(100 * (loaded / total), resource)
        if (item.callback) {
          item.callback({texture: resource})
        }
      }
      this.handleComplete(this.loadedResources)
    } catch (err) {
      this.handleError(err)
    }
  }
}
