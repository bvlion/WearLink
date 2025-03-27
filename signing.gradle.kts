signingConfigs {
  release {
    storeFile file('release.jks')
    storePassword System.getenv()['KEYSTORE_PASSWORD']
    keyAlias System.getenv()['KEYSTORE_ALIAS']
    keyPassword System.getenv()['KEYSTORE_PASSWORD']
  }
}