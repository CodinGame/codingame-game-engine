import {CreateCommand, PropertiesCommand, LoadCommand} from "./Command.js";


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
  static parse(line, globalData, frameInfo) {
    const args = splitOnSpaceOutsideQuotes(line);
    const keyword = args[0];
    if (keyword === 'C') {
      return new CreateCommand(args.slice(1), globalData);
    } else if (keyword === 'U') {
      return new PropertiesCommand(args.slice(1), globalData, frameInfo);
    } else if (keyword === 'L') {
      return new LoadCommand(args.slice(1), globalData);
    } else {
      throw new Error("Unrecognised command : " + keyword);
    }
  }
}