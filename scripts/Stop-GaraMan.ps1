$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
$env:PYTHONIOENCODING = 'utf-8'
python scripts/run_tomcat.py stop
if ($LASTEXITCODE -ne 0) { throw 'Không dừng được Tomcat project.' }
python scripts/local_runtime.py stop
if ($LASTEXITCODE -ne 0) { throw 'Không dừng được MySQL project.' }
