let language = 'EN'
let statements = {
  EN: null,
  FR: null
}
let saved = {
  EN: false,
  FR: false
}
let isDirty = {
  EN: false,
  FR: false
}
let editor
const saveButton = document.getElementById('StatementMaker-save')

init()

async function init () {
  editor = await loadMonacoEditor()

  const response = await fetch('/services/statement')
  statements = JSON.parse(await response.text())
  setStatementInput(statements[language])
  refreshPreview()
  saved.EN = statements.EN != null
  saved.FR = statements.FR != null
  updateSaveButton()

  editor.onDidChangeModelContent(event => {
    handleChangeStatementInput()
  })

  window.onbeforeunload = function (e) {
    if (!isDirty.EN && !isDirty.FR) {
      return null
    } else {
      return 'If you leave before saving, your changes will be lost.'
    }
  }
}

function loadMonacoEditor () {
  return new Promise(resolve => {
    require.config({ paths: { vs: 'https://unpkg.com/monaco-editor@0.20.0/min/vs' }})
    window.MonacoEnvironment = { getWorkerUrl: () => proxy }

    let proxy = URL.createObjectURL(new Blob([`
      self.MonacoEnvironment = {
        baseUrl: 'https://unpkg.com/monaco-editor@0.20.0/min/'
      };
      importScripts('https://unpkg.com/monaco-editor@0.20.0/min/vs/base/worker/workerMain.js');
    `], { type: 'text/javascript' }))

    require(['vs/editor/editor.main'], function () {
      const editor = monaco.editor.create(document.getElementById('StatementMaker-input'), {
        value: 'Loading statement...',
        language: 'html',
        theme: 'vs-dark',
        minimap: {
          enabled: false
        },
        wordWrap: 'on'
      })

      window.addEventListener('resize', () => {
        editor.layout()
      })

      resolve(editor)
    })
  })
}

function getStatementInput () {
  return editor.getValue()
}

function setStatementInput (statement) {
  editor.setValue(statement)

  const defaultInsertSpaces = true
  const defaultTabSize = 2
  editor.getModel().detectIndentation(defaultInsertSpaces, defaultTabSize)
}

async function refreshPreview () {
  const response = await fetch('/services/preview-levels', {
    method: 'POST',
    body: JSON.stringify({
      language: language,
      statement: getStatementInput()
    })
  })
  const preview = JSON.parse(await response.text())

  const resultNode = document.getElementById('StatementMaker-preview-result')
  const errorNode = document.getElementById('StatementMaker-preview-error')

  try {
    errorNode.innerText = ''
    resultNode.style.opacity = 1
  } catch (error) {
    errorNode.innerText = error.message
    resultNode.style.opacity = 0.2
  }

  for (let [level, statement] of Object.entries(preview)) {
    const elementId = `StatementMaker-preview-result-${level}`
    let result = document.getElementById(elementId)
    if (!result) {
      result = document.createElement("div")
      result.classList.add('cg-statement')
      result.id = elementId
      if (level !== "level1") {
        result.style.marginTop = '50px'
      }
      resultNode.parentElement.appendChild(result)
    }
    result.innerHTML = statement
  }
}

async function save () {
  saveButton.disabled = true
  await fetch('/services/statement', {
    method: 'PUT',
    body: JSON.stringify({
      language: language,
      statement: getStatementInput()
    })
  })
  saveButton.innerText = 'Saved ' + language
  saved[language] = true
  isDirty[language] = false
}

function handleChangeStatementInput () {
  saveButton.disabled = false
  saveButton.innerText = 'Save'
  refreshPreview()
  saved[language] = false
  isDirty[language] = true
}

function updateSaveButton () {
  if (saved[language]) {
    saveButton.disabled = true
    saveButton.innerText = 'Saved ' + language
  } else {
    saveButton.disabled = false
    saveButton.innerText = 'Save'
  }
}

function changeLanguage (event, tabLanguage) {
  document.querySelectorAll('.StatementMaker-languages button')
    .forEach(button => {
      button.classList.remove('active')
    })

  const clickedElement = event.currentTarget
  clickedElement.classList.add('active')

  statements[language] = getStatementInput()
  language = tabLanguage
  setStatementInput(statements[language])
  updateSaveButton()
}
