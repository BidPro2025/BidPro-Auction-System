$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Set-Location -LiteralPath $repoRoot
& (Join-Path $PSScriptRoot 'mysql-local.ps1') -Action Start
if ($LASTEXITCODE -ne 0) { throw 'Unable to start workspace MySQL.' }
$credentials = Get-Content -LiteralPath '.cache/mysql/credentials.json' | ConvertFrom-Json
$schema = 'bidpro_test_' + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$clientPath = Join-Path $repoRoot '.cache/mysql/mysql-8.4.13-winx64/bin/mysql.exe'
$clientConfig = Join-Path $repoRoot '.cache/mysql/client.ini'
"CREATE DATABASE $schema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" | & $clientPath "--defaults-extra-file=$clientConfig"
if ($LASTEXITCODE -ne 0) { throw 'Unable to create the isolated MySQL test schema.' }
$changes = @{
    SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:$($credentials.port)/$schema"
    SPRING_DATASOURCE_USERNAME = $credentials.username
    SPRING_DATASOURCE_PASSWORD = $credentials.password
    SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'com.mysql.cj.jdbc.Driver'
    SPRING_JPA_HIBERNATE_DDL_AUTO = 'create-drop'
}
$saved = @{}
foreach ($key in $changes.Keys) {
    $saved[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
    [Environment]::SetEnvironmentVariable($key, $changes[$key], 'Process')
}
try {
    Write-Output "Running the complete backend integration suite against isolated MySQL schema $schema"
    & node scripts/api.mjs test
    $testExit = $LASTEXITCODE
}
finally {
    foreach ($key in $saved.Keys) { [Environment]::SetEnvironmentVariable($key, $saved[$key], 'Process') }
}
exit $testExit
