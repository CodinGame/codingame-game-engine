async function main() {
  const body = document.getElementById('messages');

  const data = await fetch('/services/export');
  if (data.status >= 400 && data.status < 500) {
    const text = await data.text();
    body.append(text);
    document.getElementById('form').style.display = 'block';
  } else {
    const blob = await data.blob();
    var url = window.URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = "export.zip";
    a.click();
  }
}
