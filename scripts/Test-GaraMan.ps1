param([Alias('RuntimeRestart')][switch]$Smoke)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
$env:PYTHONIOENCODING = 'utf-8'
if (-not (Test-Path -LiteralPath '.local/test.properties')) { throw 'Chạy Setup trước để có schema test riêng.' }
& .\mvnw.cmd -B -ntp clean package
if ($LASTEXITCODE -ne 0) { throw 'JUnit/build thất bại.' }
python scripts/prepare_slots.py --test
if ($LASTEXITCODE -ne 0) { throw 'Không chuẩn bị được slot test.' }
try {
    python scripts/run_tomcat.py start --test
    if ($LASTEXITCODE -ne 0) { throw 'Tomcat test chưa chạy được.' }
    python scripts/browser_test.py
    if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử Chrome thất bại.' }
    python scripts/browser_regression_v3.py
    if ($LASTEXITCODE -ne 0) { throw 'Hồi quy V3 thất bại.' }
    python scripts/test_guardrails.py
    if ($LASTEXITCODE -ne 0) { throw 'Guard/cleanup test thất bại.' }
    if ($Smoke) {
        python scripts/runtime_smoke.py
        if ($LASTEXITCODE -ne 0) { throw 'Smoke test thất bại.' }
    }
} finally {
    python scripts/run_tomcat.py stop --test
}

