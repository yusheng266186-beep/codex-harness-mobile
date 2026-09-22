# Phone control helpers for CodexHarness / Termux recovery.
# Usage:  . .\.tools\phone.ps1   then call the functions below.

$script:ProjectRoot = Split-Path -Parent $PSScriptRoot
$script:ShotDir = $script:ProjectRoot

$adbCandidates = @()
if ($env:ANDROID_ADB) { $adbCandidates += $env:ANDROID_ADB }
if ($env:ANDROID_HOME) { $adbCandidates += (Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe') }
if ($env:ANDROID_SDK_ROOT) { $adbCandidates += (Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe') }
$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if ($adbCommand) { $adbCandidates += $adbCommand.Source }
$script:Adb = $adbCandidates | Where-Object { $_ -and (Test-Path -LiteralPath $_) } | Select-Object -First 1
if (-not $script:Adb) {
    throw '找不到 adb。请设置 ANDROID_HOME/ANDROID_SDK_ROOT，或把 platform-tools 加入 PATH。'
}

function Adb {
    param([Parameter(ValueFromRemainingArguments = $true)] $Args)
    & $script:Adb @Args
}

# Binary-safe screenshot: screencap to device file, then pull.
function Shot {
    param([string] $Name = 'shot')
    $out = Join-Path $script:ShotDir "$Name.png"
    & $script:Adb shell screencap -p /sdcard/_shot.png | Out-Null
    & $script:Adb pull /sdcard/_shot.png $out 2>$null | Out-Null
    return $out
}

function Tap {
    param([int] $X, [int] $Y)
    & $script:Adb shell input tap $X $Y | Out-Null
}

function Swipe {
    param([int] $X1, [int] $Y1, [int] $X2, [int] $Y2, [int] $Ms = 300)
    & $script:Adb shell input swipe $X1 $Y1 $X2 $Y2 $Ms | Out-Null
}

# Type text into the focused field. Input strings must have spaces as %s.
function TypeText {
    param([string] $Text)
    & $script:Adb shell input text $Text | Out-Null
}

function Enter {
    & $script:Adb shell input keyevent 66 | Out-Null
}

# Type a shell command and press enter. Escapes spaces for `input text`.
function Run {
    param([string] $Command)
    $escaped = $Command -replace ' ', '%s'
    & $script:Adb shell input text $escaped | Out-Null
    Start-Sleep -Milliseconds 250
    & $script:Adb shell input keyevent 66 | Out-Null
}

function Focus {
    (& $script:Adb shell dumpsys window 2>&1 | Select-String 'mCurrentFocus' | Select-Object -First 1) -replace '.*mCurrentFocus=', ''
}

function TermuxPid {
    (& $script:Adb shell pidof com.termux 2>&1) -join ''
}

# Ports listening on loopback, as decimal numbers.
function LoopbackPorts {
    $raw = & $script:Adb shell cat /proc/net/tcp 2>&1
    $ports = @()
    foreach ($line in $raw) {
        if ($line -match '^\s*\d+:\s+([0-9A-F]{8}):([0-9A-F]{4})\s+[0-9A-F]{8}:[0-9A-F]{4}\s+0A') {
            $ports += [Convert]::ToInt32($matches[2], 16)
        }
    }
    return $ports
}

# --- Remote shell over SSH (adb forward -> Termux sshd on 8022) ---
$script:SshArgs = @(
    '-p', '8022',
    '-o', 'StrictHostKeyChecking=no',
    '-o', 'UserKnownHostsFile=NUL',
    '-o', 'BatchMode=yes',
    '-o', 'ConnectTimeout=15',
    '-o', 'LogLevel=ERROR',
    '127.0.0.1'
)

# Run a command in Termux (Android side) and return its output as a string array.
function Sh {
    param([Parameter(ValueFromRemainingArguments = $true)] $Args)
    & ssh @script:SshArgs @Args 2>&1
}

# Ensure sshd is listening and the adb forward tunnel is up; restart if needed.
function EnsureShell {
    $ports = LoopbackPorts
    if ($ports -notcontains 8022) {
        Write-Host "sshd not listening; restarting via UI..."
        Tap 600 1000
        Start-Sleep -Milliseconds 500
        Run 'sshd'
        Start-Sleep -Seconds 3
    }
    & $script:Adb forward tcp:8022 tcp:8022 | Out-Null
    $probe = Sh 'echo TUNNEL_OK'
    if ($probe -notmatch 'TUNNEL_OK') { throw "SSH tunnel is not working: $probe" }
    return $true
}

# Copy a local script to the phone and run it, optionally inside Debian.
# Avoids all nested quoting problems by keeping the command text in a file.
function RunScript {
    param(
        [Parameter(Mandatory = $true)][string] $Path,
        [switch] $InDebian
    )
    if (-not (Test-Path $Path)) { throw "no such script: $Path" }
    $name = Split-Path $Path -Leaf
    $remote = "/data/data/com.termux/files/home/.tools/$name"
    Sh "mkdir -p ~/.tools" | Out-Null
    & scp -P 8022 -o StrictHostKeyChecking=no -o UserKnownHostsFile=NUL -o LogLevel=ERROR `
        $Path "127.0.0.1:$remote" 2>&1 | Out-Null
    Sh "chmod +x $remote" | Out-Null
    if ($InDebian) {
        Sh "proot-distro login debian --bind /data/data/com.termux/files/home/.tools:/mnt/tools -- bash /mnt/tools/$name"
    } else {
        Sh "bash $remote"
    }
}

Write-Host "phone.ps1 loaded. Functions: Adb, Shot, Tap, Swipe, TypeText, Enter, Run, Focus, TermuxPid, LoopbackPorts, Sh, EnsureShell, RunScript"
