pipeline {
    agent any

    environment {
        SERVER_IP = '3.6.36.148'  // Server IP address
        REPO_URL = 'https://github.com/Ab-D-ev/kubernetes-devops-project.git'  // GitHub repository URL
        DOCKER_USER = 'anushree039'  // DockerHub username
    }

    stages {
        stage('Git Clone') {
            steps {
                script {
                    sshagent(['ansible']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} 'bash -c "if [ ! -d /home/ubuntu/k8-deployment ]; then mkdir -p /home/ubuntu/k8-deployment; fi && \
                        cd /home/ubuntu/k8-deployment && \
                        if [ ! -d kubernetes-devops-project ]; then git clone ${REPO_URL} kubernetes-devops-project; else cd kubernetes-devops-project && git pull; fi"'
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
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} 'bash -c "cd /home/ubuntu/k8-deployment/kubernetes-devops-project && \
                        docker build -t k8-deployment:v1.${BUILD_ID} . && \
                        docker tag k8-deployment:v1.${BUILD_ID} ${DOCKER_USER}/k8-deployment:v1.${BUILD_ID} && \
                        docker tag k8-deployment:v1.${BUILD_ID} ${DOCKER_USER}/k8-deployment:latest"'
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
                            ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} 'bash -c "echo ${DockerPass} | docker login -u ${DOCKER_USER} --password-stdin && \
                            docker push ${DOCKER_USER}/k8-deployment:v1.${BUILD_ID} && \
                            docker push ${DOCKER_USER}/k8-deployment:latest && \
                            docker logout"'
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
