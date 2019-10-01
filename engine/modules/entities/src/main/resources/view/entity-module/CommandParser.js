import { CreateCommand, PropertiesCommand, LoadCommand } from './Command.js'

const COMMAND_KEY_MAP = {
  C: CreateCommand,
  U: PropertiesCommand,
  L: LoadCommand
}

function splitOnCharOutsideQuotes (text, charParam) {
  const res = []
  let current = ''
  let idx = 0
  let lastChar = ''
  let inQuotes = false

  while (idx < text.length) {
    const char = text[idx++]
    if (char === charParam) {
      if (!inQuotes) {
        res.push(current)
        current = ''
      } else {
        current += char
      }
    } else if (char === "'" && lastChar !== '\\') {
      inQuotes = !inQuotes
      current += char
    } else if (lastChar === '\\') {
      if (char === "'") {
        current += "\\'"
      } else {
        current += '\\' + char
      }
    } else if (char !== '\\') {
      current += char
    }
    lastChar = char
  }
  res.push(current)
  return res
};

function getCommands (Type, text, globalData, frameInfo) {
  const args = splitOnCharOutsideQuotes(text, ';')
  const commands = []
  args.forEach(command => commands.push(new Type(splitOnCharOutsideQuotes(command, ' '), globalData, frameInfo)))
  return commands
}
export class CommandParser {
  static parse (line, globalData, frameInfo) {
    let commands = []

    const commandChunks = splitOnCharOutsideQuotes(line, '\n')

    for (const i in commandChunks) {
      const type = COMMAND_KEY_MAP[commandChunks[i][0]]
      if (type) {
        commands = [...commands, ...getCommands(type, commandChunks[i].slice(1), globalData, frameInfo)]
      } else {
        throw new Error('Unrecognised command : ' + commandChunks[i][0])
      }
    }
    return commands
  }
}
