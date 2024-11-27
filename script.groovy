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

        stage("Copy") {
            steps {
                script {
                    sh "scp /var/lib/jenkins/workspace/k8-deployment/ansible-playbook.yml ubuntu@65.0.80.22:/tmp"
                    sshagent(['ansible']) {
                        sh 'ssh -o StrictHostKeyChecking=no ubuntu@65.0.80.22'
                        sh "mv /tmp/ansible-playbook.yml /home/ubuntu"
                    } 
                    
                }
            } 
        }
    }
}
