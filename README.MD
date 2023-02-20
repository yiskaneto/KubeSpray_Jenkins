# KubeSpray Jenkins Automation Pipeline
Jenkins pipeline to automate the installation of Kubernetes on RHEL distribution systems by using the Kubespray project

## Requirements
1. Python 3.9

1. Create a Python virtual environment,   job will create a new python virtual environment, hence the jenkins user must have writing permission to it, you can specify where the python 

1. You must copy the public ssh key to the target nodes, it should be the key of the user that is running the Jenkins job, for example:

   ```bash
   cat /var/lib/jenkins/.ssh/id_rsa.pub | ssh <node> "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
   ```

This Jenkins pipeline has 5 Stages

1. Create the inventory file:
   - Takes the passed hostname on the host parameters and fills a pre-built inventory file
2. Running requirements:
   - SElinux_disable
   - disable_firewalld
   - docker_uninstall
   - k8s_uninstall
   - uninstall_calico
4. Setting KubeSpray Env:
   - Clones the Kubespray project in the Jenking the ${env.WORKSPACE}/roles/tmp/
   - Creates a venv on the ${env.WORKSPACE}/roles/tmp/kubespray/ directory and installs the kubespray requirements
   - Populates the inventory/mycluster/group_vars/all/all.yml with the pass values from the Jenkins user input section
   - Populates the inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml with the pass values from the Jenkins user input section
5. Running KubeSpray:
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
