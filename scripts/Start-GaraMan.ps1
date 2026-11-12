param([switch]$Build)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
$env:PYTHONIOENCODING = 'utf-8'
python scripts/local_runtime.py start
if ($LASTEXITCODE -ne 0) { throw 'MySQL chưa chạy được.' }
if ($Build) { & .\mvnw.cmd -B -ntp package; if ($LASTEXITCODE -ne 0) { throw 'Build thất bại.' } }
python scripts/prepare_slots.py
if ($LASTEXITCODE -ne 0) { throw 'Không chuẩn bị được slot.' }
python scripts/run_tomcat.py start
if ($LASTEXITCODE -ne 0) { throw 'Tomcat chưa chạy được.' }
