# docs

Google Play 掲載用のプライバシーポリシー等、GitHub Pages（`gh-pages` branch）で公開する文書の正本を管理します。

## 正本

- `ja/privacy_policy.md` が日本語版プライバシーポリシーの正本です。

`gh-pages` branch の `privacy_policy.html` は本ディレクトリの Markdown から生成される成果物です。**`gh-pages` 上の HTML を直接編集しないでください。** 内容を変更する場合は、必ずこのディレクトリの Markdown を修正してください。

## デプロイ方法

`main` へのマージで `docs/**` に変更があると、`.github/workflows/privacy-policy.yaml` が Markdown から HTML を生成し、`gh-pages` branch へ自動デプロイします。手動で実行したい場合は同 workflow を `workflow_dispatch` で起動してください。

ローカルで生成結果を確認する場合は以下を実行します。

```sh
cd docs
npm ci
npm run build
```

`docs/public/` に生成された HTML が出力されます（この出力先はコミット対象外です）。
