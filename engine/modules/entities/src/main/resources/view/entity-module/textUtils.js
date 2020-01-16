export function ellipsis (text, maxWidth) {
  if (text.width > maxWidth) {
    while (text.text.length > 3 && text.width > maxWidth) {
      text.text = text.text.slice(0, -4) + '...'
    }
  }
}
