def setDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription("<h5><span style=\"color:#138D75\">KubeSpray Automation Test, the file kubeSpray_home/inventory/mycluster/group_vars/all/all.ymlcontains all the parameters for k8s</span></h5>")
    item.save()
}
setDescription()

pipeline {
	agent { label 'ansible' }
	options {
		ansiColor('xterm')
		// ansiColor('vga')
		// ansiColor('css')
		// ansiColor('gnome-terminal')
		
		buildDiscarder(logRotator(daysToKeepStr: '15'))
		}
  
    parameters {
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
        string(
            name: 'kube_control_plane_nodes',
            defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
            description: 'List of kube control planes IPs, separated by comas"'
        )
        string(
            name: 'etcd_nodes',
            defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
            description: 'List of kube control planes IPs, separated by comas"'
        )
        string(
            name: 'kube_nodes',
            defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
            description: 'List of kube control planes IPs, separated by comas"'
        )
        string(
            name: 'calico_rr_nodes',
            defaultValue: '192.168.0.10,192.168.0.11,192.168.0.12',
            description: 'List of kube control planes IPs, separated by comas"'
        )
        choice(
            name: 'kube_network_plugin',
            choices: ['calico','flannel','cilium','weave','cloud'],
		)
        choice(
            name: 'container_runtime',
            choices: ['docker','crio','containerd'],
            description: 'docker for docker, crio for cri-o and containerd for containerd.'
		)
        string(
            name: 'apiserver_loadbalancer_domain_name',
            defaultValue: '',
            description: 'Leave empty if not needed'
        )
        string(
            name: 'apiserver_loadbalancer_address',
            defaultValue: '',
            description: 'Leave empty if not needed'
        )
        string(
            name: 'apiserver_loadbalancer_port',
            defaultValue: '8443',
            description: 'Leave empty if not needed'
        )        
        booleanParam(
            name: 'use_internal_loadbalancer',
            defaultValue: true,
            description: 'Whether or not to use internal loadbalancers for apiservers'
        )
        choice(
            name: 'loadbalancer_apiserver_type',
            choices: ['nginx','haproxy'],
            description: 'What load balancer provider to use, this will only be consider if the paramter above was set to true'
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
        password(
        name: 'Host_Password',
        defaultValue: 'empty',
        description: '<h5>Host root\'s password. The default password is <span style=\"color:#E74C3C\">empty</span>, you can change it by clicking on \"Change Password\".</h5>'
        )
    }
            	
    stages {
        stage('Creating Inventory file') {
			steps {
                // echo -e "[all]\\n\\n[kube_control_plane]\\n\\n[etcd]\\n\\n[kube_node]\\n\\n[calico_rr]\\n\\n[k8s_cluster:children]\\nkube_control_plane\\nkube_node\\ncalico_rr" > ${WORKSPACE}/inventory.ini
				sh '''
                echo ${kube_control_plane_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${etcd_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${kube_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${calico_rr_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done

                echo ${main-master-node-install} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[main-master-node-install\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${kube_control_plane_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[kube_control_plane\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${etcd_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[etcd\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${kube_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[kube_node\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${calico_rr_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[calico_rr\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done

                cat ${WORKSPACE}/inventory.ini		
				'''
			}
		}

        // stage('SSH Key Pair Tasks') {
        //     steps {
        //         ansiblePlaybook(
        //             playbook: "${env.WORKSPACE}/roles/playbooks/ssh_keys_tasks.yaml",
        //             inventory: "${env.WORKSPACE}/inventory.ini",
        //             colorized: true,
        //             extras: '-v --ssh-extra-args="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -o IdentityFile=~/.ssh/id_rsa"',
        //             extraVars: [
        //                 ansible_password: [value: '${Host_Password}', hidden: true]
        //             ]
        //         )
        //     }
        // }

        // stage('Running Requirements') {
        //     steps {
        //         ansiblePlaybook(
        //             playbook: "${env.WORKSPACE}/roles/Requirements/main.yaml",
        //             inventory: "${env.WORKSPACE}/inventory.ini",
        //             colorized: true,
        //             extras: '-vv --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
        //             extraVars: [
        //                 jenkins_workspace: "${env.WORKSPACE}/",
        //                 http_proxy: "${params.http_proxy}",
        //                 ansible_password: [value: '${Host_Password}', hidden: true]
        //             ]
        //         )    
        //     }
        // }

        // stage('Updating Templates') {
        //     steps {
        //         ansiblePlaybook(
        //             playbook: "${env.WORKSPACE}/roles/Requirements/populate_vars.yaml",
        //             inventory: "${env.WORKSPACE}/inventory.ini",
        //             colorized: true,
        //             become: true,
        //             becomeUser: "root",
        //             extras: '-vv --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
        //             extraVars: [
        //                 jenkins_workspace: "${env.WORKSPACE}/",
        //                 http_proxy: "${params.http_proxy}",
        //                 https_proxy: "${params.https_proxy}",
        //                 no_proxy: "${params.no_proxy}",
        //                 cluster_name: "${params.cluster_name}",
        //                 apiserver_loadbalancer_domain_name: "${params.apiserver_loadbalancer_domain_name}",
        //                 apiserver_loadbalancer_address: "${params.apiserver_loadbalancer_address}",
        //                 apiserver_loadbalancer_port: "${params.apiserver_loadbalancer_port}",
        //                 use_internal_loadbalancer: "${params.use_internal_loadbalancer}",
        //                 loadbalancer_apiserver_type: "${params.loadbalancer_apiserver_type}",
        //                 kube_network_plugin: "${params.kube_network_plugin}",
        //                 container_runtime: "${params.container_runtime}",
        //                 local_release_dir: "${params.kubespray_temp_dir}",
        //                 kube_service_addresses: "${params.kube_service_addresses}",
        //                 kube_pods_subnet: "${params.kube_pods_subnet}",
        //                 ansible_password: [value: '${Host_Password}', hidden: true]
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
                '''
                ansiblePlaybook(
                    playbook: "${env.WORKSPACE}/roles/Requirements/populate_vars.yaml",
                    inventory: "${env.WORKSPACE}/inventory.ini",
                    colorized: true,
                    extras: '-vv --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
                    extraVars: [
                        jenkins_workspace: "${env.WORKSPACE}/",
                        http_proxy: "${params.http_proxy}",
                        https_proxy: "${params.https_proxy}",
                        no_proxy: "${params.no_proxy}",
                        cluster_name: "${params.cluster_name}",
                        apiserver_loadbalancer_domain_name: "${params.apiserver_loadbalancer_domain_name}",
                        apiserver_loadbalancer_address: "${params.apiserver_loadbalancer_address}",
                        apiserver_loadbalancer_port: "${params.apiserver_loadbalancer_port}",
                        use_internal_loadbalancer: "${params.use_internal_loadbalancer}",
                        loadbalancer_apiserver_type: "${params.loadbalancer_apiserver_type}",
                        kube_network_plugin: "${params.kube_network_plugin}",
                        container_runtime: "${params.container_runtime}",
                        local_release_dir: "${params.kubespray_temp_dir}",
                        kube_service_addresses: "${params.kube_service_addresses}",
                        kube_pods_subnet: "${params.kube_pods_subnet}",
                        ansible_password: [value: '${Host_Password}', hidden: true]
                    ]
                )
            }
        }
        
        stage('Running KubeSpray') {
            steps {
                sh '''
                cd ${WORKSPACE}/roles/tmp/kubespray/ ; echo -e "\n"
                pwd ; echo -e "\n"
                source venv/bin/activate ; echo -e "\n\n"
                bash -c "until time ansible-playbook -i ${WORKSPACE}/inventory.ini  --become --become-user=root cluster.yml ; sleep 5 ; done"
                source venv/bin/activate ; echo -e "\n\n"
                '''
                // sh '''
                // cd ${WORKSPACE}/roles/tmp/kubespray/ ; echo -e "\n"
                // pwd ; echo -e "\n"
                // deactivate ; echo -e "\n"
                // '''
                // ansiblePlaybook(
                //     playbook: "${env.WORKSPACE}/roles/tmp/kubespray/cluster.yml",
                //     inventory: "${WORKSPACE}/inventory.ini",
                //     colorized: true,
                //     extras: '-v -u root --become --become-user=root --flush-cache'
                // )
                // sh '''
                // cd "${env.WORKSPACE}/roles/tmp/kubespray/" ; echo -e "\n"
                // pwd ; echo -e "\n"
                // source venv/bin/activate ; echo -e "\n\n"
                // '''
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
