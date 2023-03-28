# KubeSpray Jenkins Automation Pipeline

Jenkins pipeline to automate the installation of Kubernetes on RHEL distribution systems by using the Kubespray project

## Requirements

1. Python 3.9
1. [Jenkins Ansible Plugin](https://plugins.jenkins.io/ansible/)

1. Make sure you have a sudo user on your target nodes and that is able to write to `/opt`, to ensure this you can run:
      `sudo setfacl -R -m u:<sudo username>:rwx /opt/`

1. On the Ansible control node(s) create a new Python virtual environment to download and install the specific version of Ansible and its dependencies for Kubespray to work, this will also be the ansible installation that the pipeline will use, this is helpful as it won't interfere with other ansible installation the system may have.
   1. Clone the Kubespray repo:

      ```bash
      git clone -b tags/v2.21.0 https://github.com/kubernetes-sigs/kubespray.git
      cd kubespray
      ```

   1. Create a Python virtual environment,  for this example we'll be creating the venv under /opt/

      ```bash
      python -m venv /opt/python-venvs/ansible-2.12/
      ```

   1. Activate the virtual environment:

      ```bash
      source  /opt/python-venvs/ansible-2.12/bin/activate
      ```

   1. Install the specific Ansible version along with its dependencies:

      ```bash
      python -m pip install -r requirements-2.12.txt
      ```

   1. On the Jenkins UI:
      1. Go to "Manage Jenkins > Global Tool Configuration"
      1. Scroll down to Ansible, expand the "Ansible Installation" option.
      1. Add a `Name` for the Anible installation we just did above, this is the name that will be used on the `ansible_installation` parameter.
      1. Add to the path to the Ansible executable, in our case that would be  `/opt/python-venvs/ansible-2.12/bin/`

1. You must copy the public ssh key (You can user the default one or create one specific for these installations) to the target nodes, it should be the key of the user that is running the Jenkins job, for example:

   ```bash
   ssh-copy-id -i ~/.ssh/<pub key name>.pub <user>@${node}
   or
   cat /var/lib/<user>/.ssh/id_rsa.pub | ssh <node> "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
   ```

   Or you can use this helper command to copy the key to multiple nodes:

   ```bash
   NODOS="<nodeIP> <nodeIP> <nodeIP>" && for node in ${NODOS} ; do ssh-copy-id -i ~/.ssh/<pub key name>.pub <user>@${node} -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ; done
   ```

1. The no_proxy parameter at bear minimum include:
   `127.0.0.1,localhost,10.233.0.1,10.233.0.10,169.254.25.10`

This Jenkins pipeline has 5 Stages

1. Clones the Kubespray repo
1. Running requirements:
   - Set SElinux to permissive mode.
1. Running KubeSpray:
   - This section runs the installation of K8s using KubeSpray, it could take from 7 to 30 mins, this will depend on the connection speed and the number of control plane and worker nodes.

If metrics-server is enabled, once installed you can run the following command to get all the metrics-server options:

```bash
docker run --rm k8s.gcr.io/metrics-server/metrics-server:<tag> --help
```

## Ansible Commands

In general we're invoking the ansible plugin to run the equivalent of the commands below:

- Base command, no external load balancer:

   ```bash
   time ansible-playbook -i inventory/mycluster/inventory.ini --become --become-user=root cluster.yml -K --extra-vars="http_proxy=<proxy node> https_proxy=<proxy node> no_proxy=127.0.0.1,localhost,10.233.0.1,169.254.25.10,<add the rest of nodes you need exclude> kube_proxy_mode=iptables dashboard_enabled=<default value> metrics_server_enabled=<default value> helm_enabled=<default value>" -v
   ```

- With xternal load balancer:

   ```bash
   time ansible-playbook -i inventory/mycluster/inventory.ini --become --become-user=root cluster.yml -K --extra-vars="http_proxy=<proxy node> https_proxy=<proxy node> no_proxy=127.0.0.1,localhost,10.233.0.1,169.254.25.10,<add the rest of nodes you need exclude> kube_proxy_mode=iptables dashboard_enabled=<default value> metrics_server_enabled=<default value> helm_enabled=<default value>" -v
   ```
