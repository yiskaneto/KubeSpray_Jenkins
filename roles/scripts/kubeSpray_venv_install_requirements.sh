#!/usr/bin/env bash

ANSIBLE_VERSION=2.12

echo "\n\n\nStarting python venv kubespray-venv"
python -m venv kubespray-venv ; echo -e "\n\n"

echo "Starting source venv/bin/activate"
source kubespray-venv/bin/activate ; ehich python ; echo -e "\n\n"

echo -e "Verifying current directory"
pwd ; echo -e "\n\n"

echo -e "Running /usr/local/bin/pip3 install -r requirements.txt\n"
test -f requirements-$ANSIBLE_VERSION.yml && ansible-galaxy role install -r requirements-$ANSIBLE_VERSION.yml && ansible-galaxy collection -r requirements-$ANSIBLE_VERSION.yml

echo -e "Verifying current directory"
pwd ; echo -e "\n"

# echo "Deactivating venv"
# deactivate ; echo -e "\n"                
                