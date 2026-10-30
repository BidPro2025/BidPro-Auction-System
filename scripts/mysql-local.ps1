param(
    [ValidateSet('Start', 'Stop', 'Status')][string]$Action = 'Status'
)
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$mysqlRoot = Join-Path $repoRoot '.cache/mysql'
$mysqlBase = Join-Path $mysqlRoot 'mysql-8.4.13-winx64'
$mysqlData = Join-Path $mysqlRoot 'data'
$mysqlDaemon = Join-Path $mysqlBase 'bin/mysqld.exe'
$mysqlClient = Join-Path $mysqlBase 'bin/mysql.exe'
$mysqlAdmin = Join-Path $mysqlBase 'bin/mysqladmin.exe'
$mysqlConfig = Join-Path $mysqlRoot 'client.ini'
$mysqlCredentials = Join-Path $mysqlRoot 'credentials.json'
$mysqlPid = Join-Path $mysqlRoot 'server.pid'
$mysqlPort = 13306

function Get-TaskServer {
    if (-not (Test-Path -LiteralPath $mysqlPid)) { return $null }
    $savedId = [int](Get-Content -LiteralPath $mysqlPid)
    $process = Get-CimInstance Win32_Process -Filter "ProcessId=$savedId" -ErrorAction SilentlyContinue
    if ($process -and $process.ExecutablePath -eq $mysqlDaemon) { return $process }
    return $null
}

if ($Action -eq 'Status') {
    if (Get-TaskServer) { Write-Output "Workspace MySQL is running on 127.0.0.1:$mysqlPort" }
    else { Write-Output 'Workspace MySQL is stopped.' }
    exit 0
}
if ($Action -eq 'Stop') {
    if (-not (Get-TaskServer)) { Write-Output 'Workspace MySQL is already stopped.'; exit 0 }
    & $mysqlAdmin "--defaults-extra-file=$mysqlConfig" shutdown
    if ($LASTEXITCODE -ne 0) { throw 'Unable to stop the workspace MySQL instance.' }
    Write-Output 'Workspace MySQL stopped.'
    exit 0
}
if (Get-TaskServer) { Write-Output "Workspace MySQL is already running on port $mysqlPort"; exit 0 }
if (-not (Test-Path -LiteralPath $mysqlDaemon)) {
    New-Item -ItemType Directory -Path $mysqlRoot -Force | Out-Null
    $archive = Join-Path $mysqlRoot 'mysql-8.4.13-winx64.zip'
    if (-not (Test-Path -LiteralPath $archive)) {
        Invoke-WebRequest -UseBasicParsing -Uri 'https://dev.mysql.com/get/Downloads/MySQL-8.4/mysql-8.4.13-winx64.zip' -OutFile $archive
    }
    if ((Get-FileHash -LiteralPath $archive -Algorithm MD5).Hash.ToLowerInvariant() -ne '7b94489d5524dea9b27097068bea25a8') {
        throw 'MySQL archive does not match the checksum on the official download page.'
    }
    Expand-Archive -LiteralPath $archive -DestinationPath $mysqlRoot
}
if (-not (Test-Path -LiteralPath (Join-Path $mysqlData 'mysql'))) {
    & $mysqlDaemon --no-defaults "--basedir=$mysqlBase" "--datadir=$mysqlData" --initialize-insecure --console
    if ($LASTEXITCODE -ne 0) { throw 'MySQL initialization failed.' }
}
$daemonArguments = @('--no-defaults', ('--basedir="' + $mysqlBase + '"'), ('--datadir="' + $mysqlData + '"'),
    "--port=$mysqlPort", '--bind-address=127.0.0.1', '--mysqlx=OFF', '--console')
$process = Start-Process -FilePath $mysqlDaemon -ArgumentList $daemonArguments -WorkingDirectory $mysqlRoot -WindowStyle Hidden -PassThru `
    -RedirectStandardOutput (Join-Path $mysqlRoot 'stdout.log') -RedirectStandardError (Join-Path $mysqlRoot 'stderr.log')
Set-Content -LiteralPath $mysqlPid -Value $process.Id
$ready = $false
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    $client = New-Object Net.Sockets.TcpClient
    try { $client.Connect('127.0.0.1', $mysqlPort); $ready = $true; break }
    catch { Start-Sleep -Milliseconds 500 }
    finally { $client.Dispose() }
}
if (-not $ready) { throw 'Workspace MySQL did not start; inspect .cache/mysql/stderr.log.' }
if (-not (Test-Path -LiteralPath $mysqlCredentials)) {
    $randomBytes = New-Object byte[] 32
    $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
    $generator.GetBytes($randomBytes)
    $generator.Dispose()
    $localPassword = ([BitConverter]::ToString($randomBytes)).Replace('-', '').ToLowerInvariant()
    "ALTER USER 'root'@'localhost' IDENTIFIED BY '$localPassword';" | & $mysqlClient --no-defaults --protocol=TCP --host=127.0.0.1 "--port=$mysqlPort" --user=root
    if ($LASTEXITCODE -ne 0) { throw 'Unable to secure the workspace MySQL account.' }
    @{ username = 'root'; password = $localPassword; port = $mysqlPort } | ConvertTo-Json | Set-Content -LiteralPath $mysqlCredentials
    @('[client]', 'user=root', "password=$localPassword", 'host=127.0.0.1', "port=$mysqlPort", 'protocol=TCP') | Set-Content -LiteralPath $mysqlConfig
}
Write-Output "Workspace MySQL is running on 127.0.0.1:$mysqlPort. Credentials are stored in ignored .cache/mysql/credentials.json."
exit 0
