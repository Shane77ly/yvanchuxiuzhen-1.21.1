param([Parameter(Mandatory)][string]$SourceSave)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$source = (Resolve-Path -LiteralPath $SourceSave).Path
$saves = Join-Path $root 'run/smoke/saves'
$destination = Join-Path $saves 'Renjian-Jadebamboo-268-275'
$levelHash = '9E035D96AF6F3BB00F802892B540EFD378FF1F05955AE3F642D8C8774411BF40'
$regionHash = 'A06070EF170C240D6305BBB8DAFEF84472927D099F665851E0DA091FA60F7E47'
$region = 'dimensions/yvanchuxiuzhen/dimensionrenjian/region/r.8.8.mca'
if (-not (Test-Path -LiteralPath $saves -PathType Container)) { throw '开发客户端存档目录不存在。' }
if (Test-Path -LiteralPath $destination) { throw '目标副本已存在，请保留既有结果。' }
$lockPath = Join-Path $source 'session.lock'
if (-not (Test-Path -LiteralPath $lockPath -PathType Leaf)) { throw '来源存档缺少 session.lock，无法核对关闭状态。' }
$lock = $null
$locked = $false
try {
    $lock = [System.IO.File]::Open($lockPath, [System.IO.FileMode]::Open,
        [System.IO.FileAccess]::Read, [System.IO.FileShare]::None)
    $lock.Lock(0, 1)
    $locked = $true
    if ((Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $source 'level.dat')).Hash -ne $levelHash -or
            (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $source $region)).Hash -ne $regionHash) {
        throw '来源存档指纹不符；请勿复制或运行。'
    }
    New-Item -ItemType Directory -Path $destination | Out-Null
    Get-ChildItem -LiteralPath $source -Force | Where-Object { $_.Name -ne 'session.lock' } |
        Copy-Item -Destination $destination -Recurse
    if ((Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $destination 'level.dat')).Hash -ne $levelHash -or
            (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $destination $region)).Hash -ne $regionHash -or
            (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $source 'level.dat')).Hash -ne $levelHash -or
            (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $source $region)).Hash -ne $regionHash) {
        throw '来源或副本指纹不符，保留副本供检查，不启动游戏。'
    }
    Write-Output "已准备独立副本：$destination"
} finally {
    if ($locked) { $lock.Unlock(0, 1) }
    if ($null -ne $lock) { $lock.Dispose() }
}
