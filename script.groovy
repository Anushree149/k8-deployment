pipeline {
    agent any

    environment {
        SERVER_IP = '3.6.36.148'  // Server IP address
        REPO_URL = 'https://github.com/Anushree149/k8-deployment.git'  // GitHub repository URL
        DOCKER_USER = 'anushree039'  // DockerHub username
    }

    stages {
        stage('Git Clone') {
            steps {
                script {
                    sshagent(['ansible']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} << 'EOF'
                        # Ensure the directory exists
                        if [ ! -d "/home/ubuntu/k8-deployment" ]; then
                            mkdir -p /home/ubuntu/k8-deployment
                        fi

                        cd /home/ubuntu/k8-deployment

                        # Clone or pull the repository
                        if [ ! -d "kubernetes-devops-project" ]; then
                            git clone ${REPO_URL} kubernetes-devops-project
                        else
                            cd kubernetes-devops-project
                            git pull
                        fi
                        EOF
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
                        ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} << 'EOF'
                        cd /home/ubuntu/k8-deployment/kubernetes-devops-project

                        # Docker Build
                        docker build -t k8-deployment:v1.${BUILD_ID} .
                        
                        # Tag the image
                        docker tag k8-deployment:v1.${BUILD_ID} ${DOCKER_USER}/k8-deployment:v1.${BUILD_ID}
                        docker tag k8-deployment:v1.${BUILD_ID} ${DOCKER_USER}/k8-deployment:latest
                        EOF
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
                            ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_IP} << 'EOF'
                            # Docker login
                            echo ${DockerPass} | docker login -u ${DOCKER_USER} --password-stdin

                            # Push Docker images
                            docker push ${DOCKER_USER}/k8-deployment:v1.${BUILD_ID}
                            docker push ${DOCKER_USER}/k8-deployment:latest
                            
                            # Logout of Docker
                            docker logout
                            EOF
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
