#!/bin/bash
mkdir -p ../../../schedule/chrome-driver/chromedriver-linux64
cd ../../../schedule/chrome-driver/chromedriver-linux64
curl -o chromedriver-linux64.zip https://storage.googleapis.com/chrome-for-testing-public/125.0.6422.141/linux64/chromedriver-linux64.zip
unzip chromedriver-linux64.zip

