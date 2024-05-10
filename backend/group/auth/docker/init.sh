#!/bin/bash
#${JAR_NAME} ${JAVA_COMMAND_STR} 為deployment傳進來的環境變數
java -server ${JAVA_COMMAND_STR} -jar  ${JAR_NAME}
while sleep 60; do
  ps aux |grep ${JAR_NAME} |grep -q -v grep
  PROCESS_1_STATUS=$?

  # If the greps above find anything, they exit with 0 status
  # If they are not both 0, then something is wrong
  if [ $PROCESS_1_STATUS -ne 0 ]; then
    echo "One of the processes has already exited."
    exit 1
  fi
done