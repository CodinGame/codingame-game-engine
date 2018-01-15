import * as commands from "./Command.js";

function splitOnSpaceOutsideQuotes(text) {
  const res = [];
  let current = ''
  let idx = 0;
  let lastChar = '';
  let inQuotes = false;

  while (idx < text.length) {
    const char = text[idx++];
    if (char == ' ') {
      if (!inQuotes) {
        res.push(current);
        current = '';
      } else {
        current += char;
      }
    } else if (char == "'" && lastChar != '\\') {
      inQuotes = !inQuotes;
    } else if (lastChar == '\\') {
      if (char == "'") {
        current += "'";
      } else {
        current += '\\' + char;
      }
    } else if (char != '\\') {
      current += char;
    }
    lastChar = char;
  }
  res.push(current);
  return res;
};

export class CommandParser {
  constructor() {
    this.parsers = {
      C: commands.CreateCommand,
      U: commands.UpdateCommand,
      SET: commands.SetCommand
    };
  }
  parse(line, globalData, frameInfo) {
    let args = splitOnSpaceOutsideQuotes(line);
    let keyword = args[0];
    return new this.parsers[keyword](args.slice(1), globalData, frameInfo);
  }
}