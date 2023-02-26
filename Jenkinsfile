def setDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription("<h5><span style=\"color:#138D75\">KubeSpray Automation v2.21.0 <a href='https://github.com/kubernetes-sigs/kubespray/releases/tag/v2.21.0'>https://github.com/kubernetes-sigs/kubespray/releases/tag/v2.21.0'</a>. Before running this pipeline, make sure to read the README.MD from <a href='https://github.com/escanoru/KubeSpray_Jenkins'>https://github.com/escanoru/KubeSpray_Jenkins</a></span></h5>")
    item.save()
}

setDescription()

def inventorySample = '''# ## Configure 'ip' variable to bind kubernetes services on a
# ## different ip than the default iface
# ## We should set etcd_member_name for etcd cluster. The node that is not a etcd member do not need to set the value, or can set the empty string value.

[all:vars]
## You can create a vault containing user_sudo_pass with the password of your nodes or just created a new key and replace user_sudo_pass with such key withing the curly braces
ansible_become_pass='{{ user_sudo_pass }}' 

[all]
node1 ansible_host=95.54.0.12  # ip=10.3.0.1 etcd_member_name=etcd1
node2 ansible_host=95.54.0.13  # ip=10.3.0.2 etcd_member_name=etcd2
node3 ansible_host=95.54.0.14  # ip=10.3.0.3 etcd_member_name=etcd3


# ## configure a bastion host if your nodes are not directly reachable
# [bastion]
# bastion ansible_host=x.x.x.x installation_user=some_user

[kube_control_plane]
node1
node2
node3

[etcd]
node1
node2
node3

[kube_node]
node4
node5
node6

[calico_rr]
# Calico advanced options: https://github.com/kubernetes-sigs/kubespray/blob/master/docs/calico.md

[k8s_cluster:children]
kube_control_plane
kube_node
calico_rr'''

def externalLB = '''---
## Only fill this if you will use a external load balancer
apiserver_loadbalancer_domain_name: "elb.some.domain"
loadbalancer_apiserver:
  address: 1.2.3.4
  port: 8383
'''
def addons = '''---
## More configurations can be found at inventory/mycluster/group_vars/k8s_cluster/addons.yml
dashboard_enabled: false
helm_enabled: false
registry_enabled: false
metrics_server_enabled: false
ingress_nginx_enabled: false
cert_manager_enabled: false
'''

pipeline {
	agent { label 'ansible' }
	options {
		ansiColor('gnome-terminal')		
		buildDiscarder(logRotator(daysToKeepStr: '90'))
		}
    
  
    parameters {
        string(
            name: 'ansible_installation',
            description: '<h5>Name of the Ansible installation, this is set on the "Global Tool Configuration" on Jenkins</h5>'
        )
        string(
            name: 'installation_user',
            defaultValue: 'REPLACE_THIS',
            description: '<h5>Username that will run the installation</h5>'
        )
        string(
            name: 'private_key_credential',
            defaultValue: 'REPLACE_THIS',
            description: '<h5>Jenkins credential holding the private key to connect to the target nodes</h5>'
        )
        string(
            name: 'ansible_vault_credential',
            defaultValue: 'REPLACE_THIS',
            description: '<h5>Jenkins credential holding the vault</h5>'
        )
        string(
            name: 'decrypt_vault_key_credential',
            defaultValue: 'REPLACE_THIS',
            description: '<h5>Jenkins credential holding the key to decrypt the Ansible vault</h5>'
        )
        booleanParam(
            name: 'reset_k8s_cluster',
            defaultValue: true,
            description: 'Uninstall previous installations K8s and set OS requirements, this will force a reboot whether K8s is installed or not'
        )
        booleanParam(
            name: 'only_reset_k8s_cluster',
            defaultValue: false,
            description: 'Only uninstall, if this is set to true then the other stages will be skiped'
        )
        booleanParam(
            name: 'restart_node',
            defaultValue: false,
            description: 'Recommended after resseting k8s'
        )
        booleanParam(
            name: 'run_requirements',
            defaultValue: true,
            description: 'Set OS requirements'
        )
        booleanParam(
            name: 'install_kubespray',
            defaultValue: true,
            description: 'Set OS requirements'
        )
        text(
            name: 'inventory',
            defaultValue: "${inventorySample}",
            description: ''
        )
        string(
            name: 'kube_version',
            defaultValue: 'v1.25.6',
            description: '<h5>Kubernetes version, found on inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml</h5>'
        )
        string(
            name: 'cluster_name',
            defaultValue: 'cluster.local',
            description: 'Found on inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml'
        )
        string(
            name: 'http_proxy',
            defaultValue: '',
            description: '<h5>e.g http://my_proxy.com:8080, found on inventory/mycluster/group_vars/all/all.yml</h5>'
        )
        string(
            name: 'https_proxy',
            defaultValue: '',
            description: '<h5>e.g http://my_proxy.com:8080, found on inventory/mycluster/group_vars/all/all.yml</h5>'
        )
        string(
            name: 'no_proxy',
            defaultValue: '127.0.0.1,localhost,10.233.0.1,169.254.25.10',
            description: 'list of to exclude from the proxy, found on inventory/mycluster/group_vars/all/all.yml'
        )
        choice(
            name: 'kube_network_plugin',
            choices: ['calico','flannel','cilium','weave','cloud','canal'],
            description: 'Found on inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml'
		)
        choice(
            name: 'container_manager',
            choices: ['containerd','crio'],
            description: 'Found on inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml'
		)
        choice(
            name: 'resolvconf_mode',
            choices: ['host_resolvconf','none'],
            description: 'Found on inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml'
		)
        choice(
            name: 'kube_proxy_mode',
            choices: ['iptables','ipvs'],
            description: 'Found on inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml'
		)
        booleanParam(
            name: 'loadbalancer_apiserver_localhost',
            defaultValue: true,
            description: 'Found on inventory/mycluster/group_vars/all/all.yml'
        )
        choice(
            name: 'loadbalancer_apiserver_type',
            choices: ['nginx','haproxy'],
            description: 'Found on inventory/mycluster/group_vars/all/all.yml'
		)
        booleanParam(
            name: 'use_localhost_as_kubeapi_loadbalancer',
            defaultValue: true,
            description: 'Found on inventory/mycluster/group_vars/all/all.yml'
        )
        booleanParam(
            name: 'use_external_load_balancer',
            defaultValue: false,
            description: 'Custom parameter to determine if LB will be used'
        )
        text(
            name: 'externalLB',
            defaultValue: "${externalLB}",
            description: ''
        )
        // string(
        //     name: 'apiserver_loadbalancer_domain_name',
        //     defaultValue: '',
        //     description: 'Found on inventory/mycluster/group_vars/all/all.yml'
        // )
        // string(
        //     name: 'apiserver_loadbalancer_address',
        //     defaultValue: '',
        //     description: 'Found on inventory/mycluster/group_vars/all/all.yml'
        // )
        // string(
        //     name: 'apiserver_loadbalancer_port',
        //     defaultValue: '8383',
        //     description: 'VIP port for external Load Balancer. Leave empty if not needed'
        // )
        text(
            name: 'K8sAddons',
            defaultValue: "${addons}",
            description: ''
        )
        // choice(
        //     name: 'dashboard_enabled',
        //     choices: ['False','True'],
        //     description: 'RBAC required. Found on inventory/mycluster/group_vars/k8s_cluster/addons.yml'
        // )
        // choice(
        //     name: 'helm_enabled',
        //     choices: ['False','True'],
        //     description: 'Found on inventory/mycluster/group_vars/k8s_cluster/addons.yml'
        // )
        // choice(
        //     name: 'registry_enabled',
        //     choices: ['False','True'],
        //     description: 'Found on inventory/mycluster/group_vars/k8s_cluster/addons.yml'
        // )
        // choice(
        //     name: 'metrics_server_enabled',
        //     choices: ['False','True'],
        //     description: 'Found on inventory/mycluster/group_vars/k8s_cluster/addons.yml'
        // )
        // choice(
        //     name: 'ingress_nginx_enabled',
        //     choices: ['False','True'],
        //     description: 'Found on inventory/mycluster/group_vars/k8s_cluster/addons.yml'
        // )
        // choice(
        //     name: 'cert_manager_enabled',
        //     choices: ['False','True'],
        //     description: 'Found on inventory/mycluster/group_vars/k8s_cluster/addons.yml'
        // )
        string(
            name: 'local_release_dir',
            defaultValue: '/tmp/local_release_dir',
            description: "Where the binaries will be downloaded. Note: ensure that you've enough disk space (about 1G)"
        )
    }
            	
    stages {
        stage('Clonning KubeSpray project') {
            steps {
                sh '''
                cd ${WORKSPACE}/
                git clone -b v2.21.0 https://github.com/kubernetes-sigs/kubespray.git
                cd kubespray
                echo "running whoami" && whoami
                '''
            }
        }

        stage('Reset K8s Cluster') {
            when {
                expression { params.reset_k8s_cluster == true }
            }
            steps {
                withCredentials([file(credentialsId: "${params.ansible_vault_credential}", variable: 'VAULT_FILE')]) {
                    // Pass the vault file to a file where is accessible by the roles, this info remains encrypted.
                    sh """
                    set -x
                    cat $VAULT_FILE > ${WORKSPACE}/roles/ansible_data_vault.yml
                    """
                    ansiblePlaybook(
                        installation: "${params.ansible_installation}",
                        playbook: "${env.WORKSPACE}/kubespray/reset.yml",
                        inventoryContent: "${params.inventory}",
                        disableHostKeyChecking : true,
                        become: true,
                        credentialsId: "${params.private_key_credential}",
                        vaultCredentialsId: "${params.decrypt_vault_key_credential}",
                        forks: 20,
                        colorized: true,
                        extras: "-e '@${WORKSPACE}/roles/ansible_data_vault.yml' --ssh-extra-args='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --flush-cache -vv",
                        extraVars: [
                            http_proxy: "${params.http_proxy}",
                            https_proxy: "${params.https_proxy}",
                            no_proxy: "${params.no_proxy}",
                            reset_confirmation: 'yes'
                        ]
                    )
                }
            }
        }

        stage('Reboot Nodes') {
            when {
                expression { params.restart_node == true }
            }
            steps {
                withCredentials([file(credentialsId: "${params.ansible_vault_credential}", variable: 'VAULT_FILE')]) {
                    sh """
                    set -x
                    cat $VAULT_FILE > ${WORKSPACE}/roles/ansible_data_vault.yml
                    """
                    ansiblePlaybook(
                        installation: "${params.ansible_installation}",
                        playbook: "${env.WORKSPACE}/roles/Requirements/reboot_target_nodes.yaml",
                        inventoryContent: "${params.inventory}",
                        disableHostKeyChecking : true,
                        become: true,
                        credentialsId: "${params.private_key_credential}",
                        vaultCredentialsId: "${params.decrypt_vault_key_credential}",
                        colorized: true,
                        extras: "-e '@${WORKSPACE}/roles/ansible_data_vault.yml' --ssh-extra-args=' -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --flush-cache -v",
                        extraVars: [
                            jenkins_workspace: "${env.WORKSPACE}/"
                        ]
                    )
                }
            }
        }

        stage('Running OS requirements K8s') { 
            when {
                expression { params.run_requirements == true && params.only_reset_k8s_cluster == false }
            }
            steps {
                withCredentials([file(credentialsId: "${params.ansible_vault_credential}", variable: 'VAULT_FILE')]) {
                    sh """
                    set -x
                    cat $VAULT_FILE > ${WORKSPACE}/roles/ansible_data_vault.yml
                    """
                    ansiblePlaybook(
                        installation: "${params.ansible_installation}",
                        playbook: "${env.WORKSPACE}/roles/Requirements/main.yaml",
                        inventoryContent: "${params.inventory}",
                        disableHostKeyChecking : true,
                        become: true,
                        credentialsId: "${params.private_key_credential}",
                        vaultCredentialsId: "${params.decrypt_vault_key_credential}",
                        forks: 20,
                        extras: "-e '@${WORKSPACE}/roles/ansible_data_vault.yml' --ssh-extra-args=' -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --flush-cache -v",
                        extraVars: [
                            jenkins_workspace: "${env.WORKSPACE}/",
                            http_proxy: "${params.http_proxy}",
                            https_proxy: "${params.https_proxy}",
                            no_proxy: "${params.no_proxy}",
                        ]
                    )
                }
            }
        }
        
        stage('Running KubeSpray') {
            when {
                expression { params.install_kubespray == true && params.only_reset_k8s_cluster == false }
            }
            steps {
                script {
                     withCredentials([file(credentialsId: "${params.ansible_vault_credential}", variable: 'VAULT_FILE')]) {
                        writeFile file: "${WORKSPACE}/external_lb_vars.yml", text: "${params.externalLB}"
                        writeFile file: "${WORKSPACE}/K8sAddons.yml", text: "${params.K8sAddons}"
                        sh """
                        set -x
                        cat $VAULT_FILE > ${WORKSPACE}/roles/ansible_data_vault.yml
                        """
                    }
                    retry(1) {
                        if (params.use_external_load_balancer) {
                            sh 'echo Running with use_external_load_balancer'
                            ansiblePlaybook(
                                installation: "${params.ansible_installation}",
                                playbook: "${env.WORKSPACE}/kubespray/cluster.yml",
                                inventoryContent: "${params.inventory}",
                                disableHostKeyChecking: true,
                                become: true,
                                credentialsId: "${params.private_key_credential}",
                                vaultCredentialsId: "${params.decrypt_vault_key_credential}",
                                forks: 20,
                                colorized: true,
                                extras: "-e '@${WORKSPACE}/external_lb_vars.yml' -e '@${WORKSPACE}/roles/ansible_data_vault.yml' -e '@${WORKSPACE}/K8sAddons.yml' --ssh-extra-args=' -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --flush-cache -vv",
                                extraVars: [
                                    http_proxy: "${params.http_proxy}",
                                    https_proxy: "${params.https_proxy}",
                                    no_proxy: "${params.no_proxy}",
                                    kube_version: "${params.kube_version}",
                                    cluster_name: "${params.cluster_name}",
                                    kube_proxy_mode: "${params.kube_proxy_mode}",
                                    dashboard_enabled: "${params.dashboard_enabled}",
                                    helm_enabled: "${params.helm_enabled}",
                                    registry_enabled: "${params.registry_enabled}",
                                    metrics_server_enabled: "${params.metrics_server_enabled}",
                                    ingress_nginx_enabled: "${params.ingress_nginx_enabled}",
                                    cert_manager_enabled: "${params.cert_manager_enabled}",
                                    use_localhost_as_kubeapi_loadbalancer: "${params.use_localhost_as_kubeapi_loadbalancer}",
                                ]
                            )
                        } else {
                            ansiblePlaybook(
                                installation: "${params.ansible_installation}",
                                playbook: "${env.WORKSPACE}/kubespray/cluster.yml",
                                inventoryContent: "${params.inventory}",
                                disableHostKeyChecking: true,
                                become: true,
                                credentialsId: "${params.private_key_credential}",
                                vaultCredentialsId: "${params.decrypt_vault_key_credential}",
                                forks: 20,
                                colorized: true,
                                extras: "-e '@${WORKSPACE}/roles/ansible_data_vault.yml' -e '@${WORKSPACE}/K8sAddons.yml' --ssh-extra-args=' -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null' --flush-cache -vv",
                                extraVars: [
                                    http_proxy: "${params.http_proxy}",
                                    https_proxy: "${params.https_proxy}",
                                    no_proxy: "${params.no_proxy}",
                                    kube_version: "${params.kube_version}",
                                    cluster_name: "${params.cluster_name}",
                                    kube_proxy_mode: "${params.kube_proxy_mode}",
                                    use_localhost_as_kubeapi_loadbalancer: "${params.use_localhost_as_kubeapi_loadbalancer}"
                                ]
                            )
                            
                        }
                    }

                }
            }
        }
    }
  
    post {
        always {
            echo 'Cleaning up the workspace'
            deleteDir()
        }
    }
}
