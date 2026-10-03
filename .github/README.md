# Android CI and releases

`workflows/ci.yml` builds the debug APK, runs JVM tests and Android lint, and
runs instrumented tests on an Android 36 emulator. It runs on branch pushes,
pull requests, and manual dispatch. APKs and reports are saved as Actions artifacts.
The release workflow calls the same CI workflow against the exact tagged commit.

## Signing configuration

Create these repository secrets under **Settings → Secrets and variables → Actions**:

| Secret | Value |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | Base64 contents of your release `.jks` keystore |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Signing key alias |
| `ANDROID_KEY_PASSWORD` | Signing key password |

Use the same release signing key for every update. Keep the keystore backed up
outside the repository. To copy its Base64 value on Windows:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('C:\path\release.jks')) | Set-Clipboard
```

## Publish a release

Commit the application and workflow changes first, then push a version tag:

```sh
git tag v2.0.0
git push origin v2.0.0
```

Alternatively, run **Android Release** in the Actions tab and enter an existing
tag. The workflow validates the tag, checks its commit, and publishes signed APK
and AAB files plus `SHA256SUMS.txt` to GitHub Releases only after CI passes.
Tags such as `v2.1.0-beta.1` produce prereleases. Rerunning an existing release
replaces its attached files.

The tag supplies `versionName` without the `v` prefix. `versionCode` is the
release workflow run number plus one; a rerun retains the same code. Keep this
workflow's run-number history when distributing updates. Local builds retain
the defaults in `app/build.gradle.kts`, and can override them with
`-PversionName=2.0.0 -PversionCode=2`.

Publishing uses the built-in `GITHUB_TOKEN`; no personal access token is needed.
Missing signing secrets fail the release rather than publishing an unsigned APK.
No Play Store deployment is configured; the AAB is available as a release asset.
