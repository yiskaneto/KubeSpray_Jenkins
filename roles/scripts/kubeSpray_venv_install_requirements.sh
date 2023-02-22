#!/usr/bin/env bash

set -e

python_venv=${1}

ANSIBLE_VERSION=2.12

# echo -e "\nStarting python venv kubespray-venv\n"
# python -m venv ${python_venv} && echo -e "\n"

# echo "\nStarting source venv/bin/activate\n"
# source ${python_venv}/bin/activate && which python && echo -e "\n\n"

# echo -e "\nVerifying current directory\n" && pwd

# echo -e "\nRunning python -m pip install -r requirements-$ANSIBLE_VERSION.txt\n"
# python -m pip install -r requirements-$ANSIBLE_VERSION.txt

# # echo -e "Running ansible-galazy agains the \n"
# # test -f requirements-$ANSIBLE_VERSION.yml && ansible-galaxy role install -r requirements-$ANSIBLE_VERSION.yml && ansible-galaxy collection -r requirements-$ANSIBLE_VERSION.yml

# echo -e "\nVerifying current directory\n" && pwd && echo -e "\n"

# deactivate ; echo -e "\n"          
