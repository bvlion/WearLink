const fs = require('fs')
const path = require('path')
const { marked } = require('marked')

const DOCS_DIR = __dirname
const OUTPUT_DIR = path.join(DOCS_DIR, 'public')

const template = (title, body) => `<!DOCTYPE HTML>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${title}</title>
  <link href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/4.0.0/github-markdown.min.css" rel="stylesheet" type="text/css" media="all"/>
  <style>
    html,
    body {
      height: 100%;
      width: 100%;
      margin: 0;
      padding: 0;
      left: 0;
      top: 0;
      font-size: 100%;
    }

    .main {
      padding: 32px;
    }
  </style>
</head>
<body>
<div class="container">
<div class="markdown-body main">
${body}</div>
</div>
</body>
</html>
`

function buildPage({ markdownPath, outputName, title }) {
  const markdown = fs.readFileSync(path.join(DOCS_DIR, markdownPath), 'utf-8')
  const body = marked.parse(markdown)
  fs.mkdirSync(OUTPUT_DIR, { recursive: true })
  fs.writeFileSync(path.join(OUTPUT_DIR, outputName), template(title, body))
  console.log(`Generated ${outputName} from ${markdownPath}`)
}

// 将来 docs/en/privacy_policy.md 等を追加する場合は、ここに buildPage() 呼び出しを追加する。
buildPage({
  markdownPath: 'ja/privacy_policy.md',
  outputName: 'privacy_policy.html',
  title: 'プライバシー・ポリシー',
})
