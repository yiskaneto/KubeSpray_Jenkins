#! /usr/bin/env bash

echo "\n\n\nStarting python3 -m venv venv"
python3 -m venv venv ; echo -e "\n\n"

echo "Starting source venv/bin/activate"
source venv/bin/activate ; echo -e "\n\n"

echo -e "cd pwd"
pwd ; echo -e "\n\n"

echo "Running /usr/local/bin/pip3 install -r requirements.txt"
/usr/local/bin/pip3 install -r requirements.txt ; echo -e "\n\n"

echo "Deactive venv"
deactivate ; echo -e "\n\n"