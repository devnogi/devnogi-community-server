@echo off
setlocal enabledelayedexpansion

REM UTF-8 인코딩 설정
chcp 65001 >nul 2>&1

REM 콘솔 폰트를 유니코드 지원 폰트로 변경 (가능한 경우)
powershell -Command "& {[Console]::OutputEncoding = [System.Text.Encoding]::UTF8}" >nul 2>&1

echo 🚀 프로젝트 설정 파일을 생성합니다...
echo.

REM .env 파일 생성
if exist ".env.sample" (
    if not exist ".env" (
        copy ".env.sample" ".env" >nul 2>&1
        echo ✅ .env 파일이 생성되었습니다.
    ) else (
        echo ⚠️  .env 파일이 이미 존재합니다.
    )
) else (
    echo ❌ .env.sample 파일을 찾을 수 없습니다.
)

REM src/main/resources 디렉토리에서도 확인
if exist "src\main\resources" (
    pushd "src\main\resources" >nul 2>&1

    if exist "application-sample.yml" (
        if not exist "application.yml" (
            copy "application-sample.yml" "application.yml" >nul 2>&1
            echo ✅ src\main\resources\application.yml 파일이 생성되었습니다.
        ) else (
            echo ⚠️  src\main\resources\application.yml 파일이 이미 존재합니다.
        )
    )

    popd >nul 2>&1
)

echo.
echo 🎉 설정 완료!
echo.
echo 📝 다음 단계:
echo    1. .env 파일을 열어서 환경변수 값을 설정하세요.
echo    2. application.yml 파일을 열어서 설정값을 확인하세요.
echo.
echo 계속하려면 아무 키나 누르세요...
pause >nul