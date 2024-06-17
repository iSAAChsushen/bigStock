#!/bin/bash
mkdir -p ../../../schedule/chrome-driver/chromedriver-linux64
cd ../../../schedule/chrome-driver/chromedriver-linux64
curl -o chromedriver-linux64.zip https://storage.googleapis.com/chrome-for-testing-public/126.0.6478.61/linux64/chromedriver-linux64.zip
unzip chromedriver-linux64.zip

curl -o chrome-linux64.zip https://storage.googleapis.com/chrome-for-testing-public/126.0.6478.61/linux64/chrome-linux64.zip
unzip chrome-linux64.zip
chmod +x chrome-linux64/
chmod +x chromedriver-linux64/