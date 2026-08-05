# Repository Guidelines

## プロジェクト概要

WearLink は Kotlin で実装された Android マルチモジュールプロジェクトです。

- `mobile/`: Androidスマートフォン向けアプリ
- `wear/`: Wear OS向けアプリ
- `shared/`: mobileとwearで共有する通信処理、データモデル、DataStore、HTTP関連処理
- `fastlane/metadata/android/`: Google Play掲載情報

Java 17と、リポジトリ同梱のGradle Wrapperを使用します。

## Worktree

- Codexは、現在のworktreeと対応ブランチ内だけを変更してください。
- 他のworktreeや、`scripts/create-codex-worktree.sh`を実行したコピー元worktreeを変更しないでください。
- Issue用worktreeは、コピー元worktreeで`scripts/create-codex-worktree.sh <Issue番号> <接尾辞>`を実行して作成してください。

## 変更方針

- 依頼された問題の解決に必要な最小限の差分にしてください。
- 無関係なリファクタリング、命名変更、整形、依存更新を混ぜないでください。
- 既存の設計、パッケージ構成、命名、記述形式を優先してください。
- 挙動を推測だけで変更せず、呼び出し元・呼び出し先・既存テストを確認してください。
- 要件が不明確で複数の妥当な実装がある場合は、実装前に確認してください。
- 生成されたキャッシュやIDE固有ファイルをコミットしないでください。

## コーディング規約

`.editorconfig`に従い、KotlinとGradle Kotlin DSLのインデントはスペース2個とします。

- クラス、型、Compose関数: `UpperCamelCase`
- 関数、プロパティ: `lowerCamelCase`
- 定数: `UPPER_SNAKE_CASE`
- パッケージ: `info.bvlion...`

依存関係のバージョンは原則として `gradle/libs.versions.toml` で管理します。既存ファイルのスタイルを維持し、変更対象外のコードを一括整形しないでください。

## ビルドとテスト

変更範囲に対応する、最小限かつ十分な検証を実行してください。

代表的なコマンド:

- `./gradlew :mobile:assembleDebug`
- `./gradlew :wear:assembleDebug`
- `./gradlew :shared:testDebugUnitTest`
- `./gradlew compileDebugUnitTestJavaWithJavac`
- `./gradlew testDebugUnitTest`
- `git diff --check`

HTTP関連テストではhttpbinが必要です。ローカルでCIと同じ構成を使う場合は、httpbinを起動して`HOST`を指定してください。

- `docker run -p 80:80 -d --name httpbin kennethreitz/httpbin`
- `HOST=http://localhost ./gradlew testDebugUnitTest`

計装テストは、変更内容に必要であり、端末またはエミュレーターを利用できる場合に実行してください。

検証を実行できなかった場合や失敗した場合は、成功したものとして扱わず、実行コマンドと理由を報告してください。

## 秘密情報とローカル設定

- 秘密情報やローカル専用設定を新規作成、編集、コミットしないでください。
- `google-services.json`、`release.jks`、署名情報、APIキー、アクセストークンを差分へ含めないでください。
- 必要な秘密ファイルが環境に存在しない場合、ダミーファイルの作成やビルド設定の迂回を行わず、実行できなかった検証として報告してください。
- `.gitignore`を変更して秘密ファイルを追跡対象にしないでください。

## GitとPull Request

- `main`へ直接コミットまたはpushしないでください。
- force pushや`main`ブランチの削除を行わないでください。
- 作業ごとに専用ブランチを使用してください。
- ブランチ名は原則として `agent/<short-task-name>` としてください。
- コミット件名は英語の命令形で簡潔に記述してください。
- Issue対応を依頼された場合は、調査、実装、検証、コミット、pushを行い、Pull RequestをDraftではなくReady for reviewとして作成してください。
- Pull Request本文には対応Issueを記載し、原因または目的、変更内容、最終的な検証結果を記載してください。
- 影響のある未実施または失敗した検証が残る場合はPull Request本文に記載し、解消済みの途中経過や試行錯誤は記載しないでください。
- UI変更には可能であればスクリーンショットを添付してください。
- ユーザーの承認なしにPull Requestをマージしないでください。
- Issueを手動でcloseせず、タグやReleaseを作成しないでください。
- 破壊的操作、追加の認証、依頼範囲外の変更が必要な場合は、実行前にユーザーへ確認してください。
- ユーザーが明示的に依頼した場合は、必須CIとブランチ保護を満たすGitHub auto-mergeを設定してかまいません。
- Dependabot PRは、リポジトリ所有者がApproveした後、必須CIとブランチ保護を満たした時点で自動マージしてかまいません。
- Pull Requestを自動Approveしないでください。
