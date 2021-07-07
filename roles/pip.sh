#! /usr/bin/env bash

echo "Starting python3 -m venv venv"
python3 -m venv venv ; echo -e "\n\n\n\n"

echo "Starting source venv/bin/activate"
source venv/bin/activate ; echo -e "\n\n\n\n"

echo -e "cd pwd"
pwd ; echo -e "\n\n\n\n"

echo "Running /usr/local/bin/pip3 install -r requirements.txt"
/usr/local/bin/pip3 install -r requirements.txt ; echo -e "\n\n\n\n"