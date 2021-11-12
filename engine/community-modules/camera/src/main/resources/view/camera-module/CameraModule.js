import {WIDTH, HEIGHT} from '../core/constants.js'
import {api as entityModule} from '../entity-module/GraphicEntityModule.js'
import {easeOut} from '../core/transitions.js'
import {lerpPosition} from '../core/utils.js'

export class CameraModule {
    constructor(assets) {
        CameraModule.instance = this
        this.container = {id: -1, sizeX: -1, sizeY: -1}
        this.cameraOffset = 0
        this.previousFrame = {
            registered: new Map()
        }
        this.lastFrame = -1
        this.cameraEndPosition = {x: 0, y: 0}
        this.cameraEndScale = 1
        this.cameraCurve = t => t
        this.oldZoomState = {position: {x: 0, y: 0}, boundSize: {x: 0, y: 0}}
        this.oldCameraState = {scale: {x: -1, y: -1}, position: {x: 0, y: 0}}
        this.currentCameraState = {scale: {x: -1, y: -1}, position: {x: 0, y: 0}}
        this.previousUpdateData = this.currentUpdateFrame = this.currentUpdateProgress = undefined
        this.viewerActive = true
        this.active = true

    }

    static get name() {
        return 'c'
    }

    setActive(active) {
        this.viewerActive = active
        this.lastFrame = -2
        if (this.previousUpdateData !== undefined) {
            this.updateScene(this.previousUpdateData, this.currentUpdateFrame, this.currentUpdateProgress || 1)
        }
    }

    getRelativePosFromContainer(entity, containerId) {
        let x = 0
        let y = 0
        let root = entity
        let debug = 0
        while (root.parent !== null && root.id !== containerId) {
            x += root.currentState.x
            y += root.currentState.y
            root = root.parent
            debug++
            if (debug > 500) {
                throw new Error("this is too long") // break point to be sure the program doesn't
                // loop infinitely, should never be triggered
            }
        }
        return {x, y}
    }

    updateScene(previousData, currentData, progress) {
        this.currentUpdateFrame = currentData
        this.currentUpdateProgress = progress
        this.previousUpdateData = previousData
        const isActive = (currentData.registered.size !== 0) && (currentData.container.entity !== null)
        if (!(currentData.active && this.viewerActive)) {
            if (isActive) {
                currentData.container.entity.graphics.scale = {x: 1, y: 1}
                currentData.container.entity.graphics.position = {x: 0, y: 0}
            }
            return
        }
        if (!isActive) {
            const boundSize = {x: 1920, y: 1080}
            const position = {x: 0, y: 0}
            this.oldCameraState = {boundSize, position}
        }

        if (this.lastFrame !== currentData.number && isActive) {
            this.oldCameraState = {...this.currentCameraState}
            let maxX, minX, minY, maxY;
            let first = true;
            entityModule.entities.forEach(
                entity => {

                    if (currentData.registered.get(entity.id + "")) {
                        const relativePos = this.getRelativePosFromContainer(entity, currentData.container.entity.id)
                        if (first) {
                            minX = maxX = relativePos.x
                            minY = maxY = relativePos.y
                            first = false
                        } else {
                            minX = Math.min(minX, relativePos.x)
                            minY = Math.min(minY, relativePos.y)
                            maxX = Math.max(maxX, relativePos.x)
                            maxY = Math.max(maxY, relativePos.y)
                        }

                    }
                }
            )
            const averagePoint = {x: (maxX + minX) / 2, y: (maxY + minY) / 2}
            const boundSize = {x: maxX - minX, y: maxY - minY}
            const containerState = currentData.container.entity.currentState
            const scale2 = Math.min(HEIGHT / (boundSize.y + currentData.cameraOffset), WIDTH / (boundSize.x + currentData.cameraOffset))
            const scale = {x: scale2 / containerState.scaleX, y: scale2 / containerState.scaleY}
            this.cameraEndScale = scale

            const newX = ((currentData.container.sizeX / 2 - averagePoint.x) * scale2
                - (scale2 - 1) * currentData.container.sizeX / 2
                + (WIDTH / 2 - (containerState.x + currentData.container.sizeX / 2))) / containerState.scaleX

            const newY = ((currentData.container.sizeY / 2 - averagePoint.y) * scale2
                - (scale2 - 1) * currentData.container.sizeY / 2
                + (HEIGHT / 2 - (containerState.y + currentData.container.sizeY / 2))) / containerState.scaleY

            // currentData.container.entity.graphics.scale.x = currentData.container.entity.graphics.scale.y = 0.5
            this.cameraEndPosition = {x: newX, y: newY}
            //console.log(`frame ${currentData.number}, ${Math.round(progress*100)/100}%,container to x : ${newX}, y : ${newY}, scale : ${scale}`)
            const position = averagePoint
            this.cameraCurve = (position.x - this.oldZoomState.position.x) ** 2 +
            (position.y - this.oldZoomState.position.y) ** 2 >= currentData.cameraOffset ** 2
            || Math.max(Math.abs(boundSize.x - this.oldZoomState.boundSize.x),
                Math.abs(boundSize.y - this.oldZoomState.boundSize.y)) > currentData.cameraOffset ? easeOut : t => t
            this.oldZoomState = {boundSize, position}

        }
        const realProgress = Math.abs(currentData.number - this.lastFrame) > 1 ? 1 : progress
        if (isActive) {
            const currentPoint = lerpPosition(this.oldCameraState.position, this.cameraEndPosition, this.cameraCurve(realProgress))
            currentData.container.entity.graphics.position = currentPoint
            const currentScale = lerpPosition(this.oldCameraState.scale, this.cameraEndScale, this.cameraCurve(realProgress))
            currentData.container.entity.graphics.scale = currentScale
            this.currentCameraState = {scale: currentScale, position: currentPoint}
        }
        this.lastFrame = currentData.number


    }

    handleFrameData(frameInfo, data) {
        if (data === undefined) {
            const registered = new Map(this.previousFrame.registered)
            const cameraOffset = this.cameraOffset
            const container = this.container.id !== -1 ? {
                entity: entityModule.entities.get(this.container.id),
                sizeX: this.container.sizeX, sizeY: this.container.sizeY
            } : null
            const active = this.active
            const frame = {registered, number: frameInfo.number, cameraOffset, container, active}
            this.previousFrame = frame
            return frame
        }
        const newRegistration = data[0] || new Map()
        const registered = new Map(this.previousFrame.registered)
        Object.keys(newRegistration).forEach(
            (k) => {
                registered.set(k, newRegistration[k])
            }
        )
        this.cameraOffset = data[1] || this.cameraOffset
        this.container = data[2] ? {id: data[2][0], sizeX: data[2][1], sizeY: data[2][2]} : this.container

        const active = data[3] === null ? this.active : data[3]
        this.active = active
        const cameraOffset = this.cameraOffset
        const container = this.container.id !== -1 ? {
            entity: entityModule.entities.get(this.container.id),
            sizeX: this.container.sizeX, sizeY: this.container.sizeY
        } : null
        const frame = {registered, number: frameInfo.number, cameraOffset, container, active}
        this.previousFrame = frame
        return frame
    }

    reinitScene() {
        if (this.currentUpdateProgress !== undefined) {
            this.lastFrame = -2
            this.updateScene(this.previousUpdateData, this.currentUpdateFrame, 1)
        }

    }

}
