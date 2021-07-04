def setDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription("<h5><span style=\"color:#138D75\">KubeSpray Automation Test</span></h5>")
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
        name: 'Proxy',
        defaultValue: '',
        description: '<h5>e.g http://my_proxy.com:8080</h5>'
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
		name: 'k8s_network_plugin',
		choices: ['calico','flannel','cilium','weave','cloud'],
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

                echo ${etcd_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[etcd\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${kube_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[kube_node\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                echo ${calico_rr_nodes} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[calico_rr\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done

                cat ${WORKSPACE}/inventory.ini		
				'''
			}
		}

        stage('Requirements') {
            steps {
                ansiblePlaybook(
                playbook: "${env.WORKSPACE}/roles/Requirements/main.yaml",
                inventory: "${env.WORKSPACE}/inventory.ini",
                colorized: true,
                extras: '-v --ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
                extraVars: [
                    proxy_addr: "${params.Proxy}",
                    k8s_network_plugin: "${params.k8s_network_plugin}",
                    ansible_password: [value: '${Host_Password}', hidden: true]
                ])
            }
        }
    }
  
    // post {
    //     always {
    //         echo 'Cleaning up the workspace'
    //         deleteDir()
    //     }
    // }
}
