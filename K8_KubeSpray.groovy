def setDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription("<h5><span style=\"color:#138D75\">KubeSpray Automation Test</span></h5>")
    item.save()
}
setDescription()

def default_inventory_conf = '''# ## Configure 'ip' variable to bind kubernetes services on a
# ## different ip than the default iface
# ## We should set etcd_member_name for etcd cluster. The node that is not a etcd member do not need to set the value, or can set the empty string value.
[all]
node1 ansible_host=95.54.0.12  # ip=10.3.0.1 etcd_member_name=etcd1
node2 ansible_host=95.54.0.13  # ip=10.3.0.2 etcd_member_name=etcd2
node3 ansible_host=95.54.0.14  # ip=10.3.0.3 etcd_member_name=etcd3
node4 ansible_host=95.54.0.15  # ip=10.3.0.4 etcd_member_name=etcd4
node5 ansible_host=95.54.0.16  # ip=10.3.0.5 etcd_member_name=etcd5
node6 ansible_host=95.54.0.17  # ip=10.3.0.6 etcd_member_name=etcd6

# ## configure a bastion host if your nodes are not directly reachable
# [bastion]
# bastion ansible_host=x.x.x.x ansible_user=some_user

[kube_control_plane]
node1
node2
node3

[etcd]
node1
node2
node3

[kube_node]
node2
node3
node4
node5
node6

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
		
		buildDiscarder(logRotator(daysToKeepStr: '15'))
		}
  
    parameters {
        string(
        name: 'Proxy',
        defaultValue: '',
        description: '<h5>e.g http://my_proxy.com:8080</h5>'
        )
        string(
            name: 'kube_control_plane',
            defaultValue: '123,456,789',
            description: 'List of kube control planes IPs separated by comas"'
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
				sh '''
				echo -e "[all]\\n\\n[kube_control_plane]\\n\\n[etcd]\\n\\n[kube_node]\\n\\n[calico_rr]\\n\\n[k8s_cluster:children]\\nkube_control_plane\\nkube_node\\ncalico_rr" > ${WORKSPACE}/inventory.ini
                echo ${kube_control_plane} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[all\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
                cat ${WORKSPACE}/inventory.ini
				
				'''
			}
		}
        // stage('CAT') {
            // echo ${Master} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[master\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
			//	echo ${Workers} | sed \'s/,/\\n/g\' | while read line ; do sed -i \'/\\[workers\\]/a \\\'"${line}"\'\' ${WORKSPACE}/inventory.ini ; done
        //    steps {
        //        sh '''
        //        echo -e ${kube_control_planes} >> ${WORKSPACE}/roles/inventory.yaml
        //        '''
        //    }
        // }

        // stage('Write Inventory file') {
        //    steps {
        //        //echo "${params.inventory_conf}" > "${env.WORKSPACE}/roles/inventory.yaml"
        //        script {
        //            writeFile(file: "${WORKSPACE}/roles/inventory.yaml", text: "${params.inventory_conf}", encoding: "UTF-8")
        //        }
        //    }
        // }

        stage('Requirements') {
            steps {
                ansiblePlaybook(
                playbook: "${env.WORKSPACE}/roles/KubeSpray/requirements.yaml",
                inventory: "${env.WORKSPACE}/inventory.ini",
                colorized: true,
                extras: '--ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
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
