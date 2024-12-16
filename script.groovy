pipeline {
    agent any

    environment {
        SERVER_IP = '52.66.199.164'  // Server IP address
        DOCKER_USER = 'anushree039'  // DockerHub username
    }

    stages {
        stage('Git Clone') {
            steps {
                script {
                    sshagent(['ansible']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} 'cd /home/ubuntu/code && \
                        git clone -b main https://github.com/Anushree149/K8-Deploy.git'
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
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} 'cd /home/ubuntu/k8-deployment/kubernetes-devops-project && \
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
                            ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} 'echo ${DockerPass} | docker login -u ${DOCKER_USER} --password-stdin && \
                            docker push ${DOCKER_USER}/k8-deployment:v1.${BUILD_ID} && \
                            docker push ${DOCKER_USER}/k8-deployment:latest && \
                            docker logout'
                            """
                        }
                    }
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
