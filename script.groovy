pipeline {
    agent any

    stages {
        stage('Git Clone') {
            steps {
                script {
                    sshagent(['ubuntu']) {
                        sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@3.6.36.148 << EOF
                        cd /home/ubuntu/k8-deployment
                        git clone https://github.com/Ab-D-ev/kubernetes-devops-project.git || (cd k8-deployment && git pull)
                        EOF
                        '''
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    sshagent(['ubuntu']) {
                        sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@3.6.36.148 << EOF
                        cd /home/ubuntu/k8-deployment
                        docker build -t k8-deployment:v1.$BUILD_ID .
                        docker tag k8-deployment:v1.$BUILD_ID anushree039/k8-deployment:v1.$BUILD_ID
                        docker tag k8-deployment:v1.$BUILD_ID anushree039/k8-deployment:latest
                        EOF
                        '''
                    }
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    sshagent(['ubuntu']) {
                        withCredentials([string(credentialsId: 'DockerPass', variable: 'DockerPass')]) {
                            sh '''
                            ssh -o StrictHostKeyChecking=no ubuntu@3.6.36.148 << EOF
                            echo ${DockerPass} | docker login -u anushree039 --password-stdin
                            docker push anushree039/k8-deployment:v1.$BUILD_ID
                            docker push anushree039/k8-deployment:latest
                            EOF
                            '''
                        }
                    }
                }
            }
        }
    }
}
