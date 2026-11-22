param([Parameter(Mandatory)][string]$EvidenceDir)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$save = Join-Path $root 'run/smoke/saves/Renjian-Silk-268-275'
$report = Join-Path $root 'build/smoke/report.txt'
$verify = Join-Path $PSScriptRoot 'check.py'
$options = Join-Path $root 'run/smoke/options.txt'
$checkpoint = Join-Path $EvidenceDir 'checkpoint.json'
if (Test-Path -LiteralPath $EvidenceDir) { throw '证据目录已存在，请保留原结果。' }
$evidence = (New-Item -ItemType Directory -Path $EvidenceDir).FullName
$checkpoint = Join-Path $evidence 'checkpoint.json'

function Verify-Save([string]$Stage) {
    $argsForStage = @($verify, $Stage, $save)
    if ($Stage -ne 'ready') { $argsForStage += $checkpoint }
    & python -B -X utf8 @argsForStage 2>&1 | Tee-Object -FilePath (Join-Path $evidence "$Stage.txt")
    if ($LASTEXITCODE -ne 0) { throw "存档核验未通过：$Stage" }
}

function Invoke-Scenario([string]$Selector) {
    if (-not (Select-String -LiteralPath $options -Pattern '^soundCategory_master:0\.0$' -Quiet)) { throw '测试客户端未静音。' }
    $dir = (New-Item -ItemType Directory -Path (Join-Path $evidence $Selector)).FullName
    $start = [DateTime]::UtcNow
    & (Join-Path $root 'gradlew.bat') --offline --no-daemon --max-workers=1 "-PsmokeOnly=$Selector" runSmokeClient *> (Join-Path $dir 'gradle.log')
    $exitCode = $LASTEXITCODE
    $log = Join-Path $root 'run/smoke/logs/latest.log'
    if ((Test-Path -LiteralPath $log) -and (Get-Item -LiteralPath $log).LastWriteTimeUtc -ge $start) {
        Copy-Item -LiteralPath $log -Destination $dir
    }
    if (-not (Test-Path -LiteralPath $report) -or (Get-Item -LiteralPath $report).LastWriteTimeUtc -lt $start) {
        throw "$Selector 未产生本轮报告，Gradle=$exitCode"
    }
    Copy-Item -LiteralPath $report -Destination $dir
    $lines = Get-Content -LiteralPath $report -Encoding utf8
    if ($exitCode -ne 0 -or ($lines -match '^FAIL') -or $lines[-1] -ne 'DONE') { throw "$Selector 未通过，见 $dir" }
}

Push-Location $root
try {
    Verify-Save ready
    Invoke-Scenario silksurvival
    Verify-Save checkpoint
    Invoke-Scenario silksurvivalreentry
    Verify-Save reentry
} finally {
    Pop-Location
}
