def setDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription("<h5><span style=\"color:#138D75\">KubeSpray Automation job. Before running this pipeline, make sure to read the README.MD from <a href='https://github.com/escanoru/KubeSpray_Jenkins'>https://github.com/escanoru/KubeSpray_Jenkins</a></span></h5>")
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
		
		buildDiscarder(logRotator(daysToKeepStr: '30'))
		}
  
    parameters {
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
    }
            	
    stages {
        stage('Creating Inventory File') {
			steps {
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

        stage('SSH Key Pair Tasks') {
            steps {
                ansiblePlaybook(
                    playbook: "${env.WORKSPACE}/roles/playbooks/ssh_keys_tasks.yaml",
                    inventory: "${env.WORKSPACE}/inventory.ini",
                    colorized: true,
                    extras: '-v --ssh-extra-args="-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"'
                )
            }
        }

        // stage('Running Requirements') {
        //     steps {
        //         ansiblePlaybook(
        //             playbook: "${env.WORKSPACE}/roles/Requirements/main.yaml",
        //             inventory: "${env.WORKSPACE}/inventory.ini",
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
                        inventory: "${WORKSPACE}/inventory.ini",
                        colorized: true,
                        become: true,
                        becomeUser: "root",
                        extras: '-u ${user} --flush-cache --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" -v',
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
