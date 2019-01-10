const OCCURENCE_TOKEN = '@>'

function ParseError (name, params) {
  this.message = name + ': ' + JSON.stringify(params)
  this.name = name
  this.params = params
}

function Node (name, parent) {
  this.name = name
  this.parent = parent

  this.children = []

  if (this.parent) {
    this.parent.children.push(this)
  }
}

function Input () {
  this.root = new Node()
  this.vars = {} // key = var name ; value = node
  this.loops = []
  this.maxNestedLoop = 0
  this.statement = 'Auto-generated code below aims at helping you parse\nthe standard input according to the problem statement.'
  this.containsVarString = false

  this.getVarType = function (name) {
    return this.vars[name].parent.name
  }

  this.getVar = function (name) {
    return this.vars[name].parent
  }

  this.getVarComment = function (name) {
    return this.vars[name].comment
  }

  this.addVar = function (name, node) {
    this.vars[name] = node
    node.original = node.name
  }

  this.addLoop = function (node) {
    this.loops.push(node)
    node.original = node.param
  }

  this.setMaxNestedLoop = function (cntLoop) {
    if (this.maxNestedLoop < cntLoop) {
      this.maxNestedLoop = cntLoop
    }
  }

  this.hasStatement = function () {
    return this.statement != null && this.statement.trim().length > 0
  }

  this.hasString = function () {
    return this.containsVarString
  }
}

const Parser = function () {
  this.cursor = 0
  this.allowType = ['int', 'long', 'float', 'string', 'word']
}

Parser.prototype.getCodeName = function (from, name) {
  let occ = 0
  while (from[name + OCCURENCE_TOKEN + occ]) {
    occ++
  }
  return name + OCCURENCE_TOKEN + occ
}

Parser.prototype.getFirstCodeName = function (from, name) {
  return name + OCCURENCE_TOKEN + '0'
}

Parser.prototype.parse = function (cginput, turnBased) {
  try {
    this.write = []
    this.vars = {}
    this.code = cginput
    const input = new Input()
    input.turnBased = turnBased
    while (this.hasNext()) {
      this.parseTree(input.root, input, 0)
    }
    return input
  } finally {
    this.write = null
    this.code = null
    this.cursor = 0
  }
}

Parser.prototype.read = function (input, n) {
  const line = this.nextLine()
  const wholeRead = line.split(' ')

  for (let i = 0; i < wholeRead.length; i++) {
    const typeValue = wholeRead[i].split(':')
    const name = typeValue[0]
    const type = typeValue[1]
    if (!/^[a-zA-Z0-9]+$/.test(name)) {
      throw new ParseError('InvalidVariable', {
        variable: name,
        line: this.getLineNumber()
      })
    }
    if (!type) {
      throw new ParseError('MissingType', {
        variable: name,
        line: this.getLineNumber()
      })
    }
    const nType = new Node(type, n)
    if (/^string\([a-zA-Z0-9]+\)$/.test(type)) {
      nType.param = this.extractParam(nType.name)
      nType.name = 'string'
      input.containsVarString = true
    } else if (/^word\([a-zA-Z0-9]+\)$/.test(type)) {
      nType.param = this.extractParam(nType.name)
      nType.name = 'word'
    } else if (type === 'word' || type === 'string') {
      throw new ParseError('MissingLength', {
        variable: name,
        type: type,
        line: this.getLineNumber()
      })
    } else if (this.allowType.indexOf(type) < 0) {
      throw new ParseError('InvalidType', {
        variable: name,
        type: type,
        line: this.getLineNumber()
      })
    }

    const codeName = this.getCodeName(input.vars, name)
    input.addVar(codeName, new Node(name, nType))

    if (nType.name === this.STRING && (i > 0)) {
      throw new Error('InvalidStringRead')
    }
  }
}

Parser.prototype.parseTree = function (node, input, loopCnt) {
  if (!this.hasNext()) {
    return
  }

  const next = this.next().split(':')
  const cmd = next[0]
  const param = next[1]
  const parser = this

  if (typeof param !== 'undefined') {
    throw new ParseError('InvalidKeyword', {
      param: cmd + ':' + param,
      line: this.getLineNumber()
    })
  }

  switch (cmd) {
    case 'loop':
    {
      const n = new Node(cmd, node)
      n.param = this.next()
      input.addLoop(n)
      input.setMaxNestedLoop(++loopCnt)
      this.parseTree(n, input, loopCnt)
      break
    }
    case 'gameloop':
    {
      const n = new Node(cmd, node)
      while (this.hasNext()) {
        this.parseTree(n, input, loopCnt)
      }
      break
    }
    case 'read':
    {
      const n = new Node(cmd, node)
      this.read(input, n)
      break
    }
    case 'loopline':
    {
      const n = new Node(cmd, node)
      n.param = this.next()
      input.addLoop(n)
      input.setMaxNestedLoop(++loopCnt)
      const nRead = new Node('readline', n)
      this.read(input, nRead)

      break
    }
    case 'write':
      const write = new Node(cmd, node)
      this.write.push(write)

      let out = this.nextLine()

      let match = out.match(/join\(([^,)]+(?:,[^,)]+)*)\)\s*(?:\/\/\s*(.*))?/)
      if (match) {
        let params = match[1].split(',')
        const comment = match[2] || null
        let separator = ' '
        const separatorMatch = params[params.length - 1].match(/\s*sep\s*=\s*"(.*)"\s*/)
        if (separatorMatch) {
          params = params.slice(0, -1)
          separator = separatorMatch[1]
        }
        write.param = params.map(function (vari) {
          const name = vari.trim()
          match = name.match(/"(.*)"/)
          if (match) {
            return { type: 'CONST', value: match[1] }
          } else {
            const codeName = parser.getFirstCodeName(input.vars, name)
            return { type: 'VAR', node: input.getVar(codeName) }
          }
        })
        write.param.separator = separator
        write.comment = comment
      } else {
        const buff = [out]
        while (this.hasNext()) {
          out = this.nextLine()
          if (out.length === 0) {
            break
          }
          buff.push(out)
        }

        write.param = buff.join('\n')
      }
      break
    case 'INPUT':
      if (this.hasNext()) {
        this.nextLine()
      }
      while (this.hasNext()) {
        const line = this.nextLine()
        if (line.length === 0) {
          break
        } else {
          const splitPos = line.indexOf(':')
          const varName = line.substring(0, splitPos).trim()
          const comment = line.substring(splitPos + 1).trim()

          const codeName = this.getCodeName(this.vars, varName)
          this.vars[codeName] = true
          const varNode = input.vars[codeName]
          if (varNode) {
            varNode.comment = comment
          }
        }
      }
      break
    case 'OUTPUT':
      const buff = []
      if (this.hasNext()) {
        this.nextLine()
      }
      while (this.hasNext()) {
        const line = this.nextLine()
        if (line.length === 0) {
          break
        } else {
          buff.push(line)
        }
      }
      if (buff.length > 0) {
        this.write.forEach(function (write) {
          if (!write.comment) {
            write.comment = buff.join('\n')
          }
        })
      }
      break
    case 'STATEMENT':
    {
      const buff = []
      if (this.hasNext()) {
        this.nextLine()
      }
      while (this.hasNext()) {
        const line = this.nextLine()
        if (line.length === 0) {
          break
        } else {
          buff.push(line, '\n')
        }
      }
      if (buff.length > 0) {
        buff.pop() // remove the last \n
        input.statement = buff.join('')
      }
      break
    }
    default:
      throw new ParseError('InvalidKeyword', {
        param: cmd,
        line: this.getLineNumber()
      })
  }
}

Parser.prototype.next = function () {
  while (this.hasNext() && (this.code.charAt(this.cursor) === ' ' || this.code.charAt(this.cursor) === '\n')) {
    this.cursor++
  }

  const last = this.cursor
  while (this.hasNext() && this.code.charAt(this.cursor) !== ' ' && this.code.charAt(this.cursor) !== '\n') {
    this.cursor++
  }

  return this.code.substring(last, this.cursor).trim()
}

Parser.prototype.nextLine = function () {
  const last = this.cursor
  while (this.hasNext() && this.code.charAt(this.cursor) !== '\n') {
    this.cursor++
  }

  return this.code.substring(last, this.cursor++).trim()
}

Parser.prototype.hasNext = function () {
  return this.cursor < this.code.length
}

Parser.prototype.extractParam = function (type) {
  const iopen = type.indexOf('(')
  const iclose = type.indexOf(')')
  return type.substring(iopen + 1, iclose)
}
Parser.prototype.getLineNumber = function () {
  return this.code.substring(0, this.cursor - 1).split('\n').length
}

export default Parser
