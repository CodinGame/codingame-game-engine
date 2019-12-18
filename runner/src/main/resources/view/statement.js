var language = 'EN'
var statements
var saved = {
  EN: false,
  FR: false
}
var isDirty = {
  EN: false,
  FR: false
}

function getStatementInput () {
  return document.getElementById('statementInput').value
}

function refreshStatement () {
  const resultNode = document.getElementById('result')
  const errorNode = document.getElementById('error')

  try {
    resultNode.innerHTML = getStatementInput()

    errorNode.innerText = ''
    resultNode.style.opacity = 1
  } catch (error) {
    errorNode.innerText = error.message
    resultNode.style.opacity = 0.2
  }
}

async function init () {
  const response = await fetch('/services/statement', {
    method: 'GET'
  })
  statements = JSON.parse(await response.text())
  loadStatement()

  window.onbeforeunload = function (e) {
    if (!isDirty.EN && !isDirty.FR) {
      return null
    } else {
      return 'If you leave before saving, your changes will be lost.'
    }
  }
}

function loadStatement () {
  document.getElementById('statementInput').value = statements[language]
  refreshStatement()
}

async function save () {
  document.getElementById('save').disabled = true
  await fetch('/services/statement', {
    method: 'PUT',
    body: JSON.stringify({
      language: language,
      statement: getStatementInput()
    })
  })
  document.getElementById('save').innerText = 'Saved ' + language
  saved[language] = true
  isDirty[language] = false
}

function handleChangeStatementInput () {
  document.getElementById('save').disabled = false
  document.getElementById('save').innerText = 'Save'
  refreshStatement()
  saved[language] = false
  isDirty[language] = true
}

function updateSaveButton () {
  if (saved[language]) {
    document.getElementById('save').disabled = true
    document.getElementById('save').innerText = 'Saved ' + language
  } else {
    document.getElementById('save').disabled = false
    document.getElementById('save').innerText = 'Save'
  }
}

function changeLanguage (event, tabLanguage) {
  for (const child of document.getElementById('tab-buttons').childNodes) {
    child.className = ''
  }
  event.currentTarget.className += ' active'
  statements[language] = document.getElementById('statementInput').value
  language = tabLanguage
  loadStatement()
  updateSaveButton()
}

function changeMode (event, mode) {
  for (const child of document.getElementById('mode-buttons').childNodes) {
    child.className = ''
  }
  event.currentTarget.className += ' active'

  if (mode === 'tpl') {
    debugger
  }
}
