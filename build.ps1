# MoBoxBot 构建脚本
# 使用 Java 8 + javac + jar，产出 out\MoBoxBot.jar
param()

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$dependDir = Join-Path $root "depend"
$moBoxLib = Join-Path $dependDir "MoBoxLib.jar"
$javaWebSocket = Join-Path $dependDir "Java-WebSocket-1.6.0.jar"
$srcJava = Join-Path $root "src\main\java"
$srcResources = Join-Path $root "src\main\resources"
$outDir = Join-Path $root "out"
$buildDir = Join-Path $outDir "build"
$classesDir = Join-Path $buildDir "classes"
$jarPath = Join-Path $outDir "MoBoxBot.jar"

Write-Host "==== MoBoxBot 构建 ===="

foreach ($jar in @($moBoxLib,$javaWebSocket)) {
    if (-not (Test-Path -LiteralPath $jar)) {
        Write-Host "找不到依赖：$jar"
        exit 1
    }
}

$resolvedRoot = [System.IO.Path]::GetFullPath($root)
$resolvedBuild = [System.IO.Path]::GetFullPath($buildDir)
if (-not $resolvedBuild.StartsWith($resolvedRoot,[System.StringComparison]::OrdinalIgnoreCase)) {
    Write-Host "构建目录不在项目内，已中止：$resolvedBuild"
    exit 1
}

if (Test-Path -LiteralPath $buildDir) { Remove-Item -LiteralPath $buildDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$javaFiles = Get-ChildItem -LiteralPath $srcJava -Recurse -File -Filter *.java | Select-Object -ExpandProperty FullName
if (-not $javaFiles -or $javaFiles.Count -eq 0) {
    Write-Host "没有找到 Java 源码：$srcJava"
    exit 1
}
Write-Host ("编译源码：" + $javaFiles.Count + " 个文件")
$classpath = $moBoxLib + ";" + $javaWebSocket
& javac -encoding UTF-8 -cp $classpath -d $classesDir $javaFiles
if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败，已中止。"
    exit 1
}

Copy-Item -Path (Join-Path $srcResources "*") -Destination $classesDir -Recurse -Force

Copy-Item -Path (Join-Path $classesDir "*") -Destination $buildDir -Recurse -Force
if (Test-Path -LiteralPath $classesDir) { Remove-Item -LiteralPath $classesDir -Recurse -Force }

Push-Location $buildDir
& jar xf $moBoxLib
& jar xf $javaWebSocket
Pop-Location
if ($LASTEXITCODE -ne 0) {
    Write-Host "解压依赖失败，已中止。"
    exit 1
}

$metaInf = Join-Path $buildDir "META-INF"
if (Test-Path -LiteralPath $metaInf) {
    if (Test-Path -LiteralPath (Join-Path $metaInf "MANIFEST.MF")) {
        Remove-Item -LiteralPath (Join-Path $metaInf "MANIFEST.MF") -Force
    }
    Get-ChildItem -LiteralPath $metaInf -File | Where-Object { $_.Extension -in @(".SF",".RSA",".DSA") } | Remove-Item -Force
}

if (Test-Path -LiteralPath $jarPath) { Remove-Item -LiteralPath $jarPath -Force }
& jar cfe $jarPath org.moboxlab.moboxbot.Main -C $buildDir .
if ($LASTEXITCODE -ne 0) {
    Write-Host "打包失败，已中止。"
    exit 1
}

$size = [math]::Round((Get-Item -LiteralPath $jarPath).Length / 1MB,2)
Write-Host ("构建完成：" + $jarPath + "（" + $size + " MB）")
