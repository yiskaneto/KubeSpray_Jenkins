# KubeSpray Jenkins Automation Pipeline

For now this project only works on CentOS/RHEL 7/8

Jenkins pipeline to automate the installation of Kubernetes on CentOS by using the Kubespray project, the pipeline has 5 Stages

1. Create the inventory file, takes the passed hostname on the host parameters and fills a pre-built inventory file
2. Create the SSH key pairs, only of they don't exist.
3. Running requirements:
   3.1 SElinux_disable
   3.2 disable_firewalld
   3.3 docker_uninstall
   3.4 k8s_uninstall
   3.5 uninstall_calico
4. Setting KubeSpray Env:
   4.1 Clones the Kubespray project in the Jenking the ${env.WORKSPACE}/roles/tmp/
   4.2 Creates a venv on the ${env.WORKSPACE}/roles/tmp/kubespray/ directory and installs the kubespray requirements
   4.3 Populates the inventory/mycluster/group_vars/all/all.yml with the pass values from the Jenkins user input section
   4.4 Populates the inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml with the pass values from the Jenkins user input section
5. Running KubeSpray: This section runs the installation of K8s using KubeSpray, it could take from 7 to 30 mins, this will depend on the connection speed and the number of control plane and worker nodes.
