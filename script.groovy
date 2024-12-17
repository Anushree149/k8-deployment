pipeline {
    agent any

    environment {
        //SERVER_IP = '43.205.126.199'  // Server IP address
        DOCKER_USER = 'anushree039'  // DockerHub username
        ANSIBLE_HOST_IP = '13.201.8.20'  // Ansible Server IP
        K8S_HOST_IP = '3.110.136.28'  // Kubernetes Server IP
    }

    stages {
        stage('Git Clone') {
            steps {
                script {
                    sshagent(['ansible']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} 'cd /home/ubuntu/code/K8-Final && \
                        git clone -b main https://github.com/Anushree149/K8-Final.git'
                        """
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    sshagent(['ansible']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} 'cd /home/ubuntu/code/K8-Final && \
                        docker build -t k8-deployment:v1.${BUILD_ID} . && \
                        docker tag k8-deployment:v1.${BUILD_ID} ${DOCKER_USER}/k8-deployment:v1.${BUILD_ID} && \
                        docker tag k8-deployment:v1.${BUILD_ID} ${DOCKER_USER}/k8-deployment:latest'
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
                            ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} 'echo ${DockerPass} | docker login -u ${DOCKER_USER} --password-stdin && \
                            docker push ${DOCKER_USER}/k8-deployment:v1.${BUILD_ID} && \
                            docker push ${DOCKER_USER}/k8-deployment:latest && \
                            docker logout'
                            """
                        }
                    }
                }
            }
        }

        stage('Send Files to Ansible & K8 Servers') {
            steps {
                sshagent(['ansible']) {
                sh """
                scp -o StrictHostKeyChecking=no /home/ubuntu/code/K8-Final/service.yml ubuntu@${K8S_HOST_IP}:/home/ubuntu/
                scp -o StrictHostKeyChecking=no /home/ubuntu/code/K8-Final/deployment.yml ubuntu@${K8S_HOST_IP}:/home/ubuntu/
                """
                }
            }
        }

        stage('Run Ansible Playbook') {
            steps {
                sshagent(['ansible']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ubuntu@${ANSIBLE_HOST_IP} '
                    ansible-playbook -i /home/ubuntu/my_inv /home/ubuntu/code/K8-Final/ansible-playbook.yml
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
