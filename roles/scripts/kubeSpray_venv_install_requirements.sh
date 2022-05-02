#!/usr/bin/env bash

echo "\n\n\nStarting python3 -m venv venv"
python -m venv venv ; echo -e "\n\n"

echo "Starting source venv/bin/activate"
source venv/bin/activate ; echo -e "\n\n"

echo -e "Verifying current directory"
pwd ; echo -e "\n"

echo -e "Running /usr/local/bin/pip3 install -r requirements.txt\n"
# /usr/local/bin/pip3 install -r requirements.txt ; echo -e "\n\n"
python -m pip install -r requirements.txt ; echo -e "\n\n"

echo -e "Verifying current directory"
pwd ; echo -e "\n"

# echo "Deactivating venv"
# deactivate ; echo -e "\n"