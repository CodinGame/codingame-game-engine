function getPrismJsLanguage (language) {
  switch (language) {
    case 'C#':
      return 'csharp'
    case 'C++':
      return 'cpp'
    case 'F#':
      return 'fsharp'
    case 'VB.NET':
      return 'vbnet'
    case 'Python':
    case 'Python3':
      return 'python'
    default:
      return language.toLowerCase()
  }
}

function getStubInput () {
  return document.getElementById('stubInput').value
}

function refreshStub () {
  const resultNode = document.getElementById('result')
  const errorNode = document.getElementById('error')
  const languageId = document.getElementById('language').value
  const prismLanguage = getPrismJsLanguage(languageId)

  try {
    resultNode.innerText = cginput.generateStub({ stubInput: getStubInput(), languageId: languageId })

    resultNode.innerHTML = resultNode.innerHTML.replace(/[<]br[/]?[>]/gi, '\n')

    resultNode.className = 'language-' + prismLanguage
    Prism.highlightElement(resultNode)

    errorNode.innerText = ''
    resultNode.style.opacity = 1
  } catch (error) {
    errorNode.innerText = error.message
    resultNode.style.opacity = 0.2
  }
}

async function load () {
  const response = await fetch('/services/stub')
  stub = await response.text()
  document.getElementById('stubInput').value = stub

  refreshStub()
}

async function save () {
  document.getElementById('save').disabled = true
  await fetch('/services/stub', {
    method: 'PUT',
    body: getStubInput()
  })
  document.getElementById('save').innerText = 'Saved'
}

function handleChangeStubInput () {
  document.getElementById('save').disabled = false
  document.getElementById('save').innerText = 'Save'
  refreshStub()
}
