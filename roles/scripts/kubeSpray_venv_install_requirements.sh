#!/usr/bin/env bash

set -e

ANSIBLE_VERSION=2.12

echo -e "\n\n\nStarting python venv kubespray-venv"
python -m venv kubespray-venv ; echo -e "\n\n"

echo "Starting source venv/bin/activate"
source kubespray-venv/bin/activate ; which python ; echo -e "\n\n"

echo -e "Verifying current directory"
pwd ; echo -e "\n\n"

echo -e "Running python install -r requirements.txt\n"
test -f requirements-$ANSIBLE_VERSION.txt && ansible-galaxy role install -r requirements-$ANSIBLE_VERSION.txt && ansible-galaxy collection -r requirements-$ANSIBLE_VERSION.txt

echo -e "Verifying current directory"
pwd ; echo -e "\n"

# echo "Deactivating venv"
# deactivate ; echo -e "\n"                
                