pipeline {
    agent any

    environment {
        DOCKER_USER = 'anushree039'  // DockerHub username
        ANSIBLE_HOST_IP = '15.206.94.162'  // Ansible Server IP
        K8S_HOST_IP = '65.2.131.164'  // Kubernetes Server IP
    }

stages {
        stage('Clone Code') {
            steps {
                git branch: 'main', url: 'https://github.com/Anushree149/K8-Final.git'
            }
        }


        stage('Git Clone') {
            steps {
                script {
                    sshagent(['ansible']) {
                        
                        sh 'ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} "sudo git clone -b main https://github.com/Anushree149/K8-Final.git /home/ubuntu/code"'
                        
                    }
                }
            }
        }


        stage('Docker Build') {
            steps {
                script {
                    sshagent(['ansible']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} '
                        cd /home/ubuntu/code/ &&
                        docker build -t k8:v1.${BUILD_ID} . &&
                        docker tag k8:v1.${BUILD_ID} ${DOCKER_USER}/k8:v1.${BUILD_ID} &&
                        docker tag k8:v1.${BUILD_ID} ${DOCKER_USER}/k8:latest
                        '
                        """
                    }
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    sshagent(['ansible']) {
                        withCredentials([string(credentialsId: 'DockerPass', variable: 'DockerPass')]) {
                            sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} '
                            echo ${DockerPass} | docker login -u ${DOCKER_USER} --password-stdin &&
                            docker push ${DOCKER_USER}/k8:v1.${BUILD_ID} &&
                            docker push ${DOCKER_USER}/k8:latest &&
                            docker logout
                            '
                            """
                        }
                    }
                }
            }
        }

        stage('Send Files to Ansible & K8 Servers') {
            steps {
                
                    
                    sh "scp -o StrictHostKeyChecking=no /var/lib/jenkins/workspace/k8-deployment/ansible-playbook.yml ubuntu@${ANSIBLE_HOST_IP}:/home/ubuntu/"
                    sh "scp -o StrictHostKeyChecking=no /var/lib/jenkins/workspace/k8-deployment/service.yml ubuntu@${K8S_HOST_IP}:/home/ubuntu/"
                    sh "scp -o StrictHostKeyChecking=no /var/lib/jenkins/workspace/k8-deployment/deployment.yml ubuntu@${K8S_HOST_IP}:/home/ubuntu/"
                    
                    
                
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                sshagent(['ansible']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} '
                    ansible-playbook -i /home/ubuntu/my_inventory.ini /home/ubuntu/code/K8-Final/ansible-playbook.yml
                    '
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed. Please check the logs for more details.'
        }
    }
}

