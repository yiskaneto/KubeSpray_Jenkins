#! /usr/bin/env bash

echo "Starting python3 -m venv venv \n\n\n\n"
python3 -m venv venv

echo "Starting source venv/bin/activate \n\n\n\n"
source venv/bin/activate

echo -e "cd pwd \n\n\n\n"
pwd

echo "Running /usr/local/bin/pip3 install -r requirements.txt \n\n\n\n"
/usr/local/bin/pip3 install -r requirements.txt