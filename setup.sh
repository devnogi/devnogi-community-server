#!/bin/bash

# UTF-8 인코딩 설정 (대부분의 Unix 시스템은 기본이 UTF-8)
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

echo "🚀 프로젝트 설정 파일을 생성합니다..."
echo

# .env 파일 생성
if [ -f ".env.sample" ]; then
    if [ ! -f ".env" ]; then
        cp ".env.sample" ".env"
        echo "✅ .env 파일이 생성되었습니다."
    else
        echo "⚠️ .env 파일이 이미 존재합니다."
    fi
else
    echo "❌ .env.sample 파일을 찾을 수 없습니다."
fi

# src/main/resources 디렉토리에서 확인
if [ -d "src/main/resources" ]; then
    cd "src/main/resources"

    if [ -f "application-sample.yml" ]; then
        if [ ! -f "application.yml" ]; then
            cp "application-sample.yml" "application.yml"
            echo "✅ src/main/resources/application.yml 파일이 생성되었습니다."
        else
            echo "⚠️  src/main/resources/application.yml 파일이 이미 존재합니다."
        fi
    fi

    cd - > /dev/null
fi

echo
echo "🎉 설정 완료!"
echo
echo "📝 다음 단계:"
echo "   1. .env 파일을 열어서 환경변수 값을 설정하세요."
echo "   2. application.yml 파일을 열어서 설정값을 확인하세요."
echo

read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
echo
