# Repository Guidelines

## プロジェクト概要

WearLink は Kotlin で実装された Android マルチモジュールプロジェクトです。

- `mobile/`: Androidスマートフォン向けアプリ
- `wear/`: Wear OS向けアプリ
- `shared/`: mobileとwearで共有する通信処理、データモデル、DataStore、HTTP関連処理
- `AppInfoManager/`: アプリ情報、更新確認、変更履歴を扱うGitサブモジュール
- `fastlane/metadata/android/`: Google Play掲載情報

Java 17と、リポジトリ同梱のGradle Wrapperを使用します。

## AppInfoManagerサブモジュール

`AppInfoManager/`は独立したGitリポジトリを参照するサブモジュールです。

- 明示的に依頼されない限り、`AppInfoManager/`内を変更しないでください。
- WearLink側の作業として、サブモジュールの参照コミットを不用意に変更しないでください。
- AppInfoManager自体の修正が必要な場合は、WearLinkの変更と分離して報告してください。
- 初期化が必要な場合は `git submodule update --init --recursive` を使用してください。

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

HTTP関連テストでは、`http://localhost`でhttpbinが必要です。CIと同じ環境が必要な場合は `docker run -p 80:80 -d --name httpbin kennethreitz/httpbin` を実行してください。

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
- 明示的に依頼された場合のみ、コミット、push、Pull Request作成を行ってください。
- Pull Requestには、原因または目的、変更内容、実行した検証、未実施または失敗した検証を記載してください。
- UI変更には可能であればスクリーンショットを添付してください。
- Pull Requestを自動でマージしないでください。最終的なマージ判断はユーザーが行います。
