import { CommandParser } from './CommandParser.js';
import { fitAspectRatio } from './utils.js';
import { Drawer } from './Drawer.js';
import { Group } from './Group.js'

export class EntityManager {
  constructor(name, assets) {
    this.name = name;
    this.commandParser = new CommandParser();
    this.entities = new Map();
    this.frames = [];

    this.extrapolationMap = {};

    this.globalData = {
      coeff: 1,
      mustResetTree: true,
      mustResort: true,
      players: []
    };

  }

  handleFrameData(number, frameData) {
    for (let line of frameData.split('\n')) {
      if (line) {
        let command = this.commandParser.parse(line, this.globalData);
        command.apply(this.entities, number);
      }
    }
    this.extrapolate(number);

    var parsedFrame = {
      number: number
    };
    parsedFrame.previous = this.frames[this.frames.length - 1] || parsedFrame;
    if (parsedFrame !== parsedFrame.previous) {
      parsedFrame.previous.next = parsedFrame;
    }

    this.frames.push(parsedFrame);

    return parsedFrame;
  }

  lastElementOf(arr) {
    return arr[arr.length - 1]
  }

  extrapolate(frameNumber) {
    this
      .entities.forEach(entity => {
        // Create empty substate array if none
        if (!entity.states[frameNumber]) {
          entity.states[frameNumber] = [];
        }
        // Copy default state if extrapolation just started
        if (!this.extrapolationMap[entity.id]) {
          this.extrapolationMap[entity.id] = { ...entity.defaultState };
        }

        const currentState = this.extrapolationMap[entity.id];
        const subStates = entity.states[frameNumber];

        // Sort on t to begin extrapolation
        subStates.sort((a, b) => a.t - b.t);

        for (const state of subStates) {
          // Extrapolate through existing substates, updating the extrapolationMap in the process (currentState)
          Object.assign(currentState, state)
          Object.assign(state, currentState);
        }

        if (!subStates.length || this.lastElementOf(subStates).t !== 1) {
          // Create a subState at t=1
          subStates.push({ ...currentState, t: 1 });
        }
      });
  }

  reinitScene(container) {
    this.container = container;
    this.entities.forEach((e) => {
      e.init();
    });
  }

  sortChildren(container) {
    container.children.sort((a, b) => {
      if (a.zIndex === b.zIndex) {
        return a.id - b.id;
      } else {
        return a.zIndex - b.zIndex;
      }
    });
  }

  updateScene(previousData, currentData, progress) {
    this.entities.forEach(e => e.render(progress, currentData, this.globalData));

    // Flags are set by Entity when a zIndex changes, or a group has different children
    if (this.globalData.mustResetTree) {
      this.reconstructTree();
      this.globalData.mustResetTree = false;
      this.globalData.mustResort = true;
    }
    if (this.globalData.mustResort) {
      this.resortTree();
      this.globalData.mustResort = false;
    }
  }

  resortTree() {
    // Groups
    this.entities.forEach(e => {
      if (e instanceof Group) {
        this.sortChildren(e.graphics);
      }
    });
    // Parent
    this.sortChildren(this.container);
  }

  reconstructTree() {
    this.container.removeChildren();

    // Groups
    this.entities.forEach(e => {
      if (e instanceof Group) {
        e.graphics.removeChildren();
        e.currentState.children.split(',').forEach(id => {
          e.graphics.addChild(this.entities.get(+id).container);
        });
      }
    });

    // Parent
    this.entities.forEach(e => {
      if (!e.container.parent) {
        this.container.addChild(e.container);
      }
    });
  }

  handleGlobalData(players, globalData) {
    this.globalData.players = players;
    const width = globalData.width;
    const height = globalData.height;
    this.globalData.coeff = fitAspectRatio(width, height, Drawer.WIDTH, Drawer.HEIGHT);
  }

}