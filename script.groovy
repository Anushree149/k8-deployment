pipeline { 
    agent any 

    stages { 
        stage('Git Clone') { 
            steps {
                script {
                    sshagent(['ansible']) {
                        git branch: "main", url: 'https://github.com/Ab-D-ev/kubernetes-devops-project.git'
                    }
                }
            } 
        } 
        
        stage('Docker Build') {
            steps {
                script {
                    sshagent(['ansible']) {
                        sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@3.6.36.148 << EOF
                        cd /home/ubuntu
                        docker image build -t $JOB_NAME:v1.$BUILD_ID .
                        docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:v1.$BUILD_ID
                        docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:latest
                        EOF
                        '''
                    }
                }
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    sshagent(['ansible']) {
                        withCredentials([string(credentialsId: 'DockerPass', variable: 'DockerPass')]) {
                            sh '''
                            ssh -o StrictHostKeyChecking=no ubuntu@3.6.36.148 << EOF
                            echo ${DockerPass} | docker login -u anushree039 --password-stdin
                            docker image push anushree039/$JOB_NAME:v1.$BUILD_ID
                            docker image push anushree039/$JOB_NAME:latest
                            EOF
                            '''
                        }
                    }
                }
            }
        }
    }
}
