#! /usr/bin/env bash

echo "Starting pipenv shell"
/usr/local/bin/pipenv shell

echo "Running /usr/local/bin/pip3 install -r requirements.txt"
/usr/local/bin/pip3 install -r requirements.txt