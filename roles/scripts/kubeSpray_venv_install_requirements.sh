#!/usr/bin/env bash

set -e

venv=${1}

ANSIBLE_VERSION=2.12

echo -e "\nStarting python venv kubespray-venv\n"
python -m venv ${venv} && echo -e "\n"

echo "\nStarting source venv/bin/activate\n"
source ${venv}/bin/activate && which python && echo -e "\n\n"

echo -e "\nVerifying current directory\n" && pwd

echo -e "\nRunning python -m pip install -r requirements-$ANSIBLE_VERSION.txt\n"
python -m pip install -r requirements-$ANSIBLE_VERSION.txt

# echo -e "Running ansible-galazy agains the \n"
# test -f requirements-$ANSIBLE_VERSION.yml && ansible-galaxy role install -r requirements-$ANSIBLE_VERSION.yml && ansible-galaxy collection -r requirements-$ANSIBLE_VERSION.yml

echo -e "\nVerifying current directory\n" && pwd && echo -e "\n"

# echo "Deactivating venv"
# deactivate ; echo -e "\n"                
