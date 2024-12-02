pipeline { 
    agent any 

    stages { 
        stage('Git Clone') { 
            steps {
                git branch: "main", url: 'https://github.com/Ab-D-ev/kubernetes-devops-project.git'
            } 
        } 
        
        stage('Docker Build') {
            steps {
                script {
                    sh 'docker image build -t $JOB_NAME:v1.$BUILD_ID .'
                    sh 'docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:v1.$BUILD_ID'
                    sh 'docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:latest'
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'DockerPass', variable: 'DockerPass')]) {
                        sh 'docker login -u anushree039 -p ${DockerPass}'
                        sh 'docker image push anushree039/$JOB_NAME:v1.$BUILD_ID'
                        sh 'docker image push anushree039/$JOB_NAME:latest'
                    }
                }
            }
        }

        stage("Copy to Remote Server") {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'ansible', keyFileVariable: 'SSH_KEY')]) {
                        sh """
                        scp -o StrictHostKeyChecking=no -i ${SSH_KEY} ansible-playbook.yml ubuntu@13.233.215.80:/tmp
                        ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ubuntu@13.233.215.80 'mv /tmp/ansible-playbook.yml /home/ubuntu'
                        """
                    }
                }
            } 
        }
    }
}
