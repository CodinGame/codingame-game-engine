#!/usr/bin/env node

const fs = require('fs');
const filename = process.argv[2]
const version = process.argv[3]
const file = fs.readFileSync(filename, "utf8");
const escaped = version.replace(/\./g, '\\.')
const regexp = new RegExp(`## ${escaped}\\n*(?<notes>(?:.|\\n)*?)## \\d+\\.\\d+\\.\\d+`, 'gm')
const match = regexp.exec(file)

if (match) {
  console.log(match[1])
} else {
  throw new Error(`Couldn't find version ${version} in release notes. Don't forget to update '## Next release' in misc-3-release-notes.md`)
}
