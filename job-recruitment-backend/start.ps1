# 设置 JAVA_HOME (Java 17)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# 检查 Java 是否存在
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    Write-Host "Error: Java 17 not found at $env:JAVA_HOME" -ForegroundColor Red
    Write-Host "Please install Java 17 from: https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.exe" -ForegroundColor Yellow
    exit 1
}

Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
Write-Host "Starting Spring Boot application..." -ForegroundColor Green

# 设置 Maven Wrapper 相关路径
$MAVEN_JAVA_EXE = "$env:JAVA_HOME\bin\java.exe"
$WRAPPER_JAR = ".mvn\wrapper\maven-wrapper.jar"
$WRAPPER_LAUNCHER = "org.apache.maven.wrapper.MavenWrapperMain"

# 运行 Maven Wrapper
& $MAVEN_JAVA_EXE -classpath $WRAPPER_JAR "-Dmaven.multiModuleProjectDirectory=$PWD" $WRAPPER_LAUNCHER spring-boot:run
