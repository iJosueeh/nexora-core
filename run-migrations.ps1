# Script para ejecutar migraciones de Base de Datos en Nexora Core
# Versión ULTRA-ROBUSTA: Usa archivo de configuración temporal para evitar errores de escape

Write-Host "🚀 Iniciando proceso de migración..." -ForegroundColor Cyan

# 1. Cargar variables de .env
if (Test-Path ".env") {
    Write-Host "📄 Cargando variables desde .env..." -ForegroundColor Gray
    Get-Content .env | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
        $name, $value = $_.Split('=', 2)
        $cleanValue = $value.Trim().Trim('"').Trim("'")
        [System.Environment]::SetEnvironmentVariable($name.Trim(), $cleanValue, "Process")
    }
}

# 2. Validar variables
if (-not $env:DB_URL) {
    Write-Host "❌ Error: DB_URL no definida." -ForegroundColor Red
    exit 1
}

# 3. Crear archivo de configuración temporal para Flyway
# Esto evita que los caracteres especiales de la contraseña pasen por la línea de comandos
$flywayConf = "flyway.url=$($env:DB_URL)`n"
if ($env:DB_USERNAME) { $flywayConf += "flyway.user=$($env:DB_USERNAME)`n" }
if ($env:DB_PASSWORD) { $flywayConf += "flyway.password=$($env:DB_PASSWORD)`n" }

# Escribir el archivo con codificación UTF8 (sin BOM para compatibilidad)
$confPath = Join-Path $PSScriptRoot "flyway.tmp.conf"
$flywayConf | Out-File -FilePath $confPath -Encoding utf8

try {
    Write-Host "🛠️ Ejecutando Flyway Migrate mediante archivo de configuración..." -ForegroundColor Magenta
    
    # Ejecutar Maven apuntando al archivo temporal
    & .\mvnw.cmd flyway:migrate "-Dflyway.configFiles=$confPath" --batch-mode

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Migración completada con éxito." -ForegroundColor Green
    } else {
        Write-Host "❌ Error durante la migración." -ForegroundColor Red
        exit $LASTEXITCODE
    }
}
finally {
    # 4. Limpieza: Borrar el archivo temporal siempre, incluso si falla
    if (Test-Path $confPath) {
        Remove-Item $confPath -Force
        Write-Host "🧹 Archivo temporal de configuración eliminado." -ForegroundColor Gray
    }
}
