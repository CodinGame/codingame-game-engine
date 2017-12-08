import * as commands from "./Command.js";

export class CommandParser {
  constructor() {
    this.parsers = {
      CREATE: commands.CreateCommand,
      UPDATE: commands.UpdateCommand,
      SET: commands.SetCommand
    };
  }
  parse(line, globalData) {
    let args = line.split(" ");
    let keyword = args[0];
    return new this.parsers[keyword](args.slice(1), globalData);
  }
}