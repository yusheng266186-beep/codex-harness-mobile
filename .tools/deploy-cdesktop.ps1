# Deploy cdesktop ARM64 binaries to the phone and stage them for offline launch.
#
# Run this once with the phone connected:
#     .\.tools\deploy-cdesktop.ps1
#
# It is idempotent and hash-verified. Nothing here needs the app to be running.
#
# Why this exists: cdesktop's npx wrapper downloads ~49 MB from
# binaries.cdesktop.ai on first run. Through this phone's VPN that transfer
# stalls (observed stuck at 2% for minutes). We instead push the verified
# archives and place them exactly where the wrapper's ensureBinary() looks
# first, so its first launch makes no network call at all.

[CmdletBinding()]
param(
    [switch] $SkipAppInstall
)

$ErrorActionPreference = 'Stop'

$root    = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$adbCandidates = @()
if ($env:ANDROID_ADB) { $adbCandidates += $env:ANDROID_ADB }
if ($env:ANDROID_HOME) { $adbCandidates += (Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe') }
if ($env:ANDROID_SDK_ROOT) { $adbCandidates += (Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe') }
$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCommand) { $adbCandidates += $adbCommand.Source }
$adb = $adbCandidates | Where-Object { $_ -and (Test-Path -LiteralPath $_) } | Select-Object -First 1
if (-not $adb) {
    throw '找不到 adb。请设置 ANDROID_HOME/ANDROID_SDK_ROOT，或把 platform-tools 加入 PATH。'
}
$stage   = Join-Path $root '.stage'
$tag     = 'v0.2.3-20260519022845'
$work    = "$env:TEMP\cdesktop-probe"
$remote  = '/sdcard/Download/cdesktop'

$expected = @{
    'cdesktop'        = 'b1f05e6ff53ce9ac0c32f266cea6dfb05889672c9738a16cffa1eb9b361679d8'
    'cdesktop-mcp'    = 'b4d6f982beab5993aec373a17331770b60a984cd1730e33ec656e8dd3992a5e9'
    'cdesktop-review' = '8a8996737d52fc01ff8c130e0c5128ddf87409039ceccda903e6cf222704b492'
}

function Say($m) { Write-Host $m }

Say "=== 0. phone present? ==="
$devices = & $adb devices | Select-String 'device$'
if (-not $devices) {
    throw "no adb device. Unlock the phone, accept the USB-debugging prompt, then re-run."
}
Say ($devices -join "`n")

Say "`n=== 1. locate the built zips ==="
$zips = @{}
foreach ($name in $expected.Keys) {
    $candidate = Join-Path $work "$name.zip"
    if (-not (Test-Path $candidate)) { throw "missing $candidate - re-download the cdesktop $name archive first" }
    $zips[$name] = $candidate
    Say ("  {0,-17} {1,7:N2} MB" -f $name, ((Get-Item $candidate).Length / 1MB))
}

Say "`n=== 2. verify sha256 before pushing (a bad archive would be cached and trusted) ==="
foreach ($name in $expected.Keys) {
    $h = (Get-FileHash $zips[$name] -Algorithm SHA256).Hash.ToLower()
    if ($h -ne $expected[$name]) { throw "$name sha256 mismatch`n  got      $h`n  expected $($expected[$name])" }
    Say "  $name OK"
}

Say "`n=== 3. push to the phone ==="
& $adb shell "mkdir -p $remote"
foreach ($name in $expected.Keys) {
    & $adb push $zips[$name] "$remote/$name.zip" | Out-Null
    Say "  pushed $name.zip"
}
$stageScript = Join-Path $root '.tools\stage-cdesktop.sh'
& $adb push $stageScript "$remote/stage-cdesktop.sh" | Out-Null
Say "  pushed stage-cdesktop.sh"
Say "`n  on-device listing:"
& $adb shell "ls -la $remote"

$apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
if (-not $SkipAppInstall) {
    Say "`n=== 4. install the app (overwrite, keeps all data) ==="
    if (-not (Test-Path $apk)) { throw "no APK at $apk - build it first" }
    & $adb install -r $apk
}

Say "`n=== 5. stage the binaries inside Debian so first launch is offline ==="
Say "  Run this from the Termux app, or say the word and it can be driven over adb:"
Say "      bash /sdcard/Download/cdesktop/stage-cdesktop.sh"

Say "`n=== done ==="
Say "APK      : $apk"
Say "archives : $remote (3 files)"
Say ""
Say "Next: open the app -> Codex tab -> '启动并打开'."
Say "The wrapper finds ~/.cdesktop/bin/$tag/linux-arm64/*.zip and skips the download entirely."
