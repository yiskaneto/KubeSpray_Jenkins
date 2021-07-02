def setDescription() {
    def item = Jenkins.instance.getItemByFullName(env.JOB_NAME)
    item.setDescription("<h5><span style=\"color:#138D75\">KubeSpray Automation Test</span></h5>")
    item.save()
}
setDescription()

def inventory_conf = '''# ## Configure 'ip' variable to bind kubernetes services on a
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
		
		buildDiscarder(logRotator(daysToKeepStr: '15s'))
		}
  
    parameters {
        string(
        name: 'Proxy',
        defaultValue: '',
        description: '<h5>e.g http://my_proxy.com:8080</h5>'
        )
        text(
            name: 'inventory_conf', 
            defaultValue: "${inventory_conf}",
            description: '<a href="https://github.com/kubernetes-sigs/kubespray/blob/master/inventory/sample/inventory.ini" target="_blank" rel="noopener noreferrer">Inventory Example</a>')

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

        stage('Write Inventory file') {
           steps {
               echo "${params.inventory_conf}" > "${WORKSPACE}/roles/inventory.yaml"
            //    script {
            //        writeFile(file: "${WORKSPACE}/roles/inventory.yaml", text: "${params.inventory_conf}", encoding: "UTF-8")
            //        sh "ls -l"
            //    }
           }
        }

        stage('Requirements') {
            steps {
                ansiblePlaybook(
                playbook: "${WORKSPACE}/roles/KubeSpray/requirements.yaml",
                inventory: "${WORKSPACE}/roles/inventory.ini",
                colorized: true,
                extras: '--ssh-extra-args=" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"',
                extraVars: [
                    proxy_addr: "${params.Proxy}",
                    k8s_network_plugin: "${params.k8s_network_plugin}",
                    ansible_password: [value: "${Host_Password}", hidden: true]
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
