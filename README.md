# WearLink
http request from Wear OS

## 開発環境

* Java 1７
* Android Studio

## 配信環境

* `v*` タグをpushすると、mobile / Wear OSのRelease AABをビルドし、Google Playの内部テストへ配布する
* `release*` タグをpushすると、直近の内部テストArtifactを製品版へ昇格する

## キー管理
各ファイル Notion にて管理中

## VERSION_CODE

mobile と wear を分けるために 1 億の位を分けている

- mobile: `100000000 + VERSION_CODE`
- wear: `200000000 + VERSION_CODE`

## release workflowに必要なGitHub Actions Secrets

値そのものはリポジトリへ含めず、GitHub Actions Secretsで管理する。

- `RELEASE_JKS`
- `KEYSTORE_ALIAS`
- `KEYSTORE_PASSWORD`
- `RELEASE_GOOGLE_SERVICES_JSON`
- `GOOGLE_PLAY_SERVICE_JSON`
- `SLACK_WEBHOOK_URL`
