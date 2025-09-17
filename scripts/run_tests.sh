#!/bin/bash

rm -r ~/.oursql

# --- 配置区 ---
# 设置你的 JAR 文件路径
CLI_JAR_PATH="/home/lauv/oursql/our-sql-1.0-SNAPSHOT.jar"

# 设置包含SQL命令的输入文件路径
INPUT_COMMANDS_FILE="commands.txt"

TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)

# 设置保存CLI程序输出结果的日志文件路径
OUTPUT_LOG_FILE="test_run_output_${TIMESTAMP}.log"
# --- 配置区结束 ---


# --- 脚本主逻辑 ---
# 检查 JAR 文件是否存在
if [ ! -f "$CLI_JAR_PATH" ]; then
    echo "错误: JAR 文件未找到: $CLI_JAR_PATH"
    exit 1
fi

# 检查输入命令文件是否存在
if [ ! -f "$INPUT_COMMANDS_FILE" ]; then
    echo "错误: 输入命令文件未找到: $INPUT_COMMANDS_FILE"
    exit 1
fi

# 打印开始信息
echo "自动化测试开始..."
echo "CLI 程序: $CLI_JAR_PATH"
echo "输入文件: $INPUT_COMMANDS_FILE"
echo "输出日志: $OUTPUT_LOG_FILE"
echo "----------------------------------------------------"

# 核心执行命令
# 使用输入重定向 (<) 将整个命令文件的内容作为 CLI 程序的标准输入
# 使用输出重定向 (>) 将标准输出保存到日志文件
# 使用 2>&1 将标准错误也重定向到与标准输出相同的地方（即同一个日志文件）
java -jar "$CLI_JAR_PATH" < "$INPUT_COMMANDS_FILE" > "$OUTPUT_LOG_FILE" 2>&1

# 打印结束信息
echo "----------------------------------------------------"
echo "测试执行完毕。"
echo "所有输出（包括成功和失败）已保存在文件: $OUTPUT_LOG_FILE"