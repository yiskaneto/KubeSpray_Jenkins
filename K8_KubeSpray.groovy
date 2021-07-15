def setDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription("<h5><span style=\"color:#138D75\">KubeSpray Automation job. Before running this pipeline, make sure to read the README.MD from <a href='https://github.com/escanoru/KubeSpray_Jenkins'>https://github.com/escanoru/KubeSpray_Jenkins</a></span></h5>")
    item.save()
}
setDescription()


def inventorySample = '''# ## Configure 'ip' variable to bind kubernetes services on a
# ## different ip than the default iface
# ## We should set etcd_member_name for etcd cluster. The node that is not a etcd member do not need to set the value, or can set the empty string value.
[all]
# node1 ansible_host=95.54.0.12  # ip=10.3.0.1 etcd_member_name=etcd1
# node2 ansible_host=95.54.0.13  # ip=10.3.0.2 etcd_member_name=etcd2
# node3 ansible_host=95.54.0.14  # ip=10.3.0.3 etcd_member_name=etcd3
# node4 ansible_host=95.54.0.15  # ip=10.3.0.4 etcd_member_name=etcd4
# node5 ansible_host=95.54.0.16  # ip=10.3.0.5 etcd_member_name=etcd5
# node6 ansible_host=95.54.0.17  # ip=10.3.0.6 etcd_member_name=etcd6

# ## configure a bastion host if your nodes are not directly reachable
# [bastion]
# bastion ansible_host=x.x.x.x ansible_user=some_user

[kube_control_plane]
# node1
# node2
# node3

[etcd]
# node1
# node2
# node3

[kube_node]
# node2
# node3
# node4
# node5
# node6

[calico_rr]

[k8s_cluster:children]
kube_control_plane
kube_node
calico_rr'''

pipeline {
	agent { label 'ansible' }
	options {
		ansiColor('xterm')
		// ansiColor('vga')
		// ansiColor('css')
		// ansiColor('gnome-terminal')
		
		buildDiscarder(logRotator(daysToKeepStr: '30'))
		}
  
    parameters {
        text(
            name: 'inventory',
            defaultValue: "${inventorySample}",
            description: ''
        )
        string(
            name: 'user',
            defaultValue: 'root',
            description: '<h5>Username that will run the installation, the user must have enough privileges for writing SSL keys in /etc/, installing packages and interacting with various systemd daemons</h5>'
        )
        string(
            name: 'http_proxy',
            defaultValue: '',
            description: '<h5>e.g http://my_proxy.com:8080</h5>'
        )
        string(
            name: 'https_proxy',
            defaultValue: '',
            description: '<h5>e.g http://my_proxy.com:8080</h5>'
        )
        string(
            name: 'no_proxy',
            defaultValue: '127.0.0.1,localhost',
            description: 'list of hostnames or range of domains to exclude from the proxy'
        )
        string(
            name: 'cluster_name',
            defaultValue: 'cluster.local',
            description: 'Leave empty if not needed'
        )
        // string(
        //     name: 'kube_control_plane_nodes',
        //     defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
        //     description: 'List of kube control planes IPs, separated by comas"'
        // )
        // string(
        //     name: 'etcd_nodes',
        //     defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
        //     description: 'List of kube control planes IPs, separated by comas"'
        // )
        // string(
        //     name: 'kube_nodes',
        //     defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
        //     description: 'List of kube control planes IPs, separated by comas"'
        // )
        // string(
        //     name: 'calico_rr_nodes',
        //     defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
        //     description: 'List of kube control planes IPs, separated by comas"'
        // )
        choice(
            name: 'kube_network_plugin',
            choices: ['calico','flannel','cilium','weave','cloud'],
		)
        choice(
            name: 'container_runtime',
            choices: ['docker','crio','containerd'],
            description: 'docker for docker, crio for cri-o and containerd for containerd.'
		)
        booleanParam(
            name: 'use_external_load_balancer',
            defaultValue: false,
            description: 'Whether or not to use an external load balancer fpr the kube api'
        )
        string(
            name: 'apiserver_loadbalancer_domain_name',
            defaultValue: '',
            description: 'VIP domain name for external Load Balancer. Leave empty if not needed'
        )
        string(
            name: 'apiserver_loadbalancer_address',
            defaultValue: '',
            description: 'VIP ip address for external Load Balancer. Leave empty if not needed'
        )
        string(
            name: 'apiserver_loadbalancer_port',
            defaultValue: '8383',
            description: 'VIP port for external Load Balancer. Leave empty if not needed'
        )
        booleanParam(
            name: 'dashboard_enabled',
            defaultValue: true,
            description: 'Whether or not to install nginx ingress'
        )
        booleanParam(
            name: 'ingress_nginx_enabled',
            defaultValue: true,
            description: 'Whether or not to install nginx ingress'
        )
        booleanParam(
            name: 'metrics_server_enabled',
            defaultValue: true,
            description: 'Whether or not to enable metrics'
        )
        booleanParam(
            name: 'helm_enabled',
            defaultValue: true,
            description: 'Whether or not to install HELM'
        )
        booleanParam(
            name: 'cert_manager_enabled',
            defaultValue: true,
            description: 'Whether or not to install cert manager'
        )
        booleanParam(
            name: 'use_internal_loadbalancer',
            defaultValue: false,
            description: 'Whether or not to use internal loadbalancers for kube api servers'
        )
        choice(
            name: 'loadbalancer_apiserver_type',
            choices: ['nginx','haproxy'],
            description: 'What load balancer provider to use, this will only be consider if the parameter above was set to true'
		)
        string(
            name: 'kube_service_addresses',
            defaultValue: '10.233.0.0/18',
            description: 'Kubernetes internal network for services, unused block of space.'
        )
        string(
            name: 'kube_pods_subnet',
            defaultValue: '10.233.64.0/18',
            description: 'Internal network. When used, it will assign IP addresses from this range to individual pods. This network must be unused in your network infrastructure!'
        )
        string(
            name: 'kubespray_temp_dir',
            defaultValue: '/tmp/kubespray_temp_dir',
            description: "Where the binaries will be downloaded. Note: ensure that you've enough disk space (about 1G)"
        )
    }
            	
    stages {
        stage('Creating Inventory File') {
			steps {
                sh '''
                echo "" > ${WORKSPACE}/inventory.ini
                echo ${inventory} > ${WORKSPACE}/inventory.ini
                cat ${WORKSPACE}/inventory.ini
                '''
			// 	sh '''
            //     echo ${kube_control_plane_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
            //     echo ${etcd_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
            //     echo ${kube_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
            //     echo ${calico_rr_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done

            //     echo ${main-master-node-install} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[main-master-node-install\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
            //     echo ${kube_control_plane_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[kube_control_plane\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
            //     echo ${etcd_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[etcd\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
            //     echo ${kube_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[kube_node\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
            //     echo ${calico_rr_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[calico_rr\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done

            //     cat ${WORKSPACE}/inventory.ini		
			// 	'''
			}
		}

        // stage('SSH Key Pair Tasks') {
        //     steps {
        //         ansiblePlaybook(
        //             playbook: "${env.WORKSPACE}/roles/playbooks/ssh_keys_tasks.yaml",
        //             inventory: "${env.WORKSPACE}/inventory.ini",
        //             forks: 16,
        //             colorized: true,
        //             extras: '-v --ssh-extra-args="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"'
        //         )
        //     }
        // }

        // stage('Running Requirements') {
        //     steps {
        //         ansiblePlaybook(
        //             playbook: "${env.WORKSPACE}/roles/Requirements/main.yaml",
        //             inventory: "${env.WORKSPACE}/inventory.ini",
        //             forks: 16,
        //             colorized: true,
        //             extras: '-vv --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
        //             extraVars: [
        //                 jenkins_workspace: "${env.WORKSPACE}/",
        //                 http_proxy: "${params.http_proxy}"
        //             ]
        //         )    
        //     }
        // }

        stage('Setting KubeSpray Env') {
            steps {
                sh '''
                mkdir ${WORKSPACE}/roles/tmp/
                cd ${WORKSPACE}/roles/tmp/
                pwd
                git clone https://github.com/kubernetes-sigs/kubespray.git
                cd kubespray
                git checkout release-2.16
                cp ${WORKSPACE}/roles/scripts/kubeSpray_venv_install_requirements.sh .
                chmod +x kubeSpray_venv_install_requirements.sh
                ./kubeSpray_venv_install_requirements.sh
                cp -rfp inventory/sample inventory/mycluster
                rm -rf inventory/mycluster/inventory.ini
                cp ${WORKSPACE}/inventory.ini inventory/mycluster/inventory.ini
                '''
                ansiblePlaybook(
                    playbook: "${env.WORKSPACE}/roles/Requirements/populate_vars.yaml",
                    inventory: "${env.WORKSPACE}/inventory.ini",
                    forks: 16,
                    colorized: true,
                    extras: '-vv --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
                    extraVars: [
                        jenkins_workspace: "${env.WORKSPACE}/",
                        http_proxy: "${params.http_proxy}",
                        https_proxy: "${params.https_proxy}",
                        no_proxy: "${params.no_proxy}",
                        cluster_name: "${params.cluster_name}",
                        use_external_load_balancer: "${params.use_external_load_balancer}",
                        apiserver_loadbalancer_domain_name: "${params.apiserver_loadbalancer_domain_name}",
                        apiserver_loadbalancer_address: "${params.apiserver_loadbalancer_address}",
                        apiserver_loadbalancer_port: "${params.apiserver_loadbalancer_port}",
                        dashboard_enabled: "${params.dashboard_enabled}",
                        ingress_nginx_enabled: "${params.ingress_nginx_enabled}",
                        metrics_server_enabled: "${params.metrics_server_enabled}",
                        helm_enabled: "${params.helm_enabled}",
                        cert_manager_enabled: "${params.cert_manager_enabled}",
                        use_internal_loadbalancer: "${params.use_internal_loadbalancer}",
                        loadbalancer_apiserver_type: "${params.loadbalancer_apiserver_type}",
                        kube_network_plugin: "${params.kube_network_plugin}",
                        container_runtime: "${params.container_runtime}",
                        local_release_dir: "${params.kubespray_temp_dir}",
                        kube_service_addresses: "${params.kube_service_addresses}",
                        kube_pods_subnet: "${params.kube_pods_subnet}"
                    ]
                )
            }
        }
        
        stage('Running KubeSpray') {
            steps {                
                // This is the recommended way of running ansible playbooks/roles from Jennkins
                retry(10) {
                    ansiblePlaybook(
                        playbook: "${env.WORKSPACE}/roles/tmp/kubespray/cluster.yml",
                        inventory: "${env.WORKSPACE}/roles/tmp/kubespray/inventory/mycluster/inventory.ini",
                        forks: 16,
                        colorized: true,
                        become: true,
                        becomeUser: "root",
                        extras: '-u ${user} --flush-cache --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" --flush-cache -v',
                        extraVars: [
                            http_proxy: "${params.http_proxy}",
                            https_proxy: "${params.https_proxy}",
                            no_proxy: "${params.no_proxy}"
                        ]
                    )
                }

                // This also works but doesn't show the colors on the output which at the end could help us find easier error or warnings.
                // sh '''
                // cd ${WORKSPACE}/roles/tmp/kubespray/ ; echo -e "\n"
                // pwd ; echo -e "\n"
                // source venv/bin/activate ; echo -e "\n\n"
                // until time ansible-playbook -i ${WORKSPACE}/inventory.ini cluster.yml -u root --become --become-user=root --extra-vars "http_proxy=${http_proxy} https_proxy=${https_proxy} no_proxy=${no_proxy}" ; do sleep 5 ; done
                // deactivate ; echo -e "\n"s
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
