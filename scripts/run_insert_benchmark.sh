#!/bin/bash

# --- 配置区 ---
# 设置你的 JAR 文件路径
CLI_JAR_PATH="/home/lauv/oursql/our-sql-1.0-SNAPSHOT.jar"

# 设置要插入的记录数量
NUMBER_OF_INSERTS=1000

# 设置用于本次测试的指令文件名
COMMANDS_FILE="insert_load_test.txt"

# --- 配置区结束 ---


# --- 脚本主逻辑 ---

# 检查 JAR 文件是否存在
if [ ! -f "$CLI_JAR_PATH" ]; then
    echo "错误: JAR 文件未找到: $CLI_JAR_PATH"
    exit 1
fi

echo "--- 步骤 1: 生成插入指令 ---"
echo "正在生成 ${NUMBER_OF_INSERTS} 条 INSERT 指令到文件: ${COMMANDS_FILE}"

# 清空或创建指令文件
> "${COMMANDS_FILE}"

# 使用循环生成INSERT语句
# 为了演示，我们假设表结构是 student(id INT, name VARCHAR, age INT)
# 您可以根据您的实际表结构修改这一行
for (( i=1; i<=${NUMBER_OF_INSERTS}; i++ ))
do
  # 生成随机年龄 (18-40)
  RANDOM_AGE=$((18 + RANDOM % 23))
  # 将指令追加到文件中
  echo "INSERT INTO student(id,name,age) VALUES (${i},'User${i}',${RANDOM_AGE});" >> "${COMMANDS_FILE}"
done

echo "指令生成完毕。"
echo "----------------------------------------------------"


echo "--- 步骤 2: 执行基准测试 ---"
# 设置日志文件名，包含时间戳
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)
OUTPUT_LOG_FILE="benchmark_output_${TIMESTAMP}.log"

echo "正在执行 ${NUMBER_OF_INSERTS} 条插入操作..."
echo "所有输出将被记录在: ${OUTPUT_LOG_FILE}"

# 记录开始时间（使用纳秒级精度以获得更准确的计时）
START_TIME=$(date +%s.%N)

# 核心执行命令
java -jar "$CLI_JAR_PATH" < "$COMMANDS_FILE" > "$OUTPUT_LOG_FILE" 2>&1

# 记录结束时间
END_TIME=$(date +%s.%N)

echo "测试执行完毕。"
echo "----------------------------------------------------"


echo "--- 步骤 3: 计算并报告结果 ---"
# 使用 bc 计算浮点数时间差
# bc 是一个高精度计算器，需要确保系统已安装 (通常 Ubuntu 自带)
DURATION=$(echo "$END_TIME - $START_TIME" | bc)

echo "✅ 基准测试报告 ✅"
echo "总共插入记录数: ${NUMBER_OF_INSERTS}"
echo "总耗时: ${DURATION} 秒"
echo "详细日志请查看文件: ${OUTPUT_LOG_FILE}"