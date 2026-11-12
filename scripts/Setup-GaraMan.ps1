param([string]$MySqlBin = 'C:\Program Files\MySQL\MySQL Server 9.4\bin')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $projectRoot
if (-not $IsWindows -and $PSVersionTable.PSEdition -eq 'Core') { throw 'Script này dành cho Windows.' }
$env:PYTHONIOENCODING = 'utf-8'
$env:GARAMAN_MYSQL_BIN = $MySqlBin
python -m pip install -r scripts/requirements.txt
if ($LASTEXITCODE -ne 0) { throw 'Không cài được phụ thuộc kiểm thử.' }
python scripts/download_tools.py
if ($LASTEXITCODE -ne 0) { throw 'Không chuẩn bị được Tomcat.' }
python scripts/local_runtime.py setup
if ($LASTEXITCODE -ne 0) { throw 'Không khởi tạo được MySQL project.' }
& .\mvnw.cmd -B -ntp package
if ($LASTEXITCODE -ne 0) { throw 'Build thất bại.' }
python scripts/prepare_slots.py
if ($LASTEXITCODE -ne 0) { throw 'Không chuẩn bị được lịch làm việc.' }
python scripts/run_tomcat.py start
if ($LASTEXITCODE -ne 0) { throw 'Tomcat chưa chạy được.' }
