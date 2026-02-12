#!/bin/bash

# 定义变量
APP_NAME="ai-code-helper"
JAR_NAME="$APP_NAME-0.0.1-SNAPSHOT.jar"
LOG_FILE="app.log"
PID_FILE="app.pid"

# 停止旧进程
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null; then
        echo "Stopping existing process with PID: $PID"
        kill $PID
        sleep 5
    fi
    rm "$PID_FILE"
fi

# 确保 target 目录存在
if [ ! -d "target" ]; then
    echo "Building project..."
    mvn clean package -DskipTests
fi

# 启动新进程
echo "Starting application..."
nohup java -jar target/$JAR_NAME > $LOG_FILE 2>&1 &
echo $! > $PID_FILE

echo "Application started with PID: $(cat $PID_FILE)"
echo "Logs are being written to $LOG_FILE"
