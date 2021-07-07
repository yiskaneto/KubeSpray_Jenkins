#! /usr/bin/env bash

echo "Starting python3 -m venv venv"
python3 -m venv venv
source venv/bin/activate

echo "Running /usr/local/bin/pip3 install -r requirements.txt"
/usr/local/bin/pip3 install -r requirements.txt