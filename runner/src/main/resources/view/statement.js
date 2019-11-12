function getStatementInput () {
  return document.getElementById('statementInput').value
}

function refreshStatement () {
  const resultNode = document.getElementById('result')
  const errorNode = document.getElementById('error')

  try {
    resultNode.innerText = getStatementInput()

    resultNode.innerHTML = resultNode.innerText.replace('\'', '&#39').replace('≤', '&le;')

    errorNode.innerText = ''
    resultNode.style.opacity = 1
  } catch (error) {
    errorNode.innerText = error.message
    resultNode.style.opacity = 0.2
  }
}

async function load () {
  const response = await fetch('/services/statement', {
    method: 'GET'
  })
  statement = await response.text()
  document.getElementById('statementInput').value = statement

  refreshStatement()
}

async function save () {
  document.getElementById('save').disabled = true
  await fetch('/services/statement', {
    method: 'PUT',
    body: getStatementInput()
  })
  document.getElementById('save').innerText = 'Saved'
}

function handleChangeStatementInput () {
  document.getElementById('save').disabled = false
  document.getElementById('save').innerText = 'Save'
  refreshStatement()
}
