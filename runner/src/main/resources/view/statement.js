var language = 'EN'

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

async function load () {
  const response = await fetch('/services/statement', {
    method: 'POST',
    body: language
  })
  const statement = await response.text()
  document.getElementById('statementInput').value = statement
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
  document.getElementById('save').innerText = 'Saved'
}

function handleChangeStatementInput () {
  document.getElementById('save').disabled = false
  document.getElementById('save').innerText = 'Save'
  refreshStatement()
}

function changeLanguage (event, tabLanguage) {
  for (const child of document.getElementById('tab-buttons').childNodes) {
    child.className = ''
  }
  event.currentTarget.className += ' active'
  language = tabLanguage
  load()
}
