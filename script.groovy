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

        stage('Copy') {
            steps {
                script {
                    // Using WORKSPACE for dynamic path
                    sh "scp ${WORKSPACE}/ansible-playbook.yml ubuntu@13.232.174.81:/tmp"
                    
                    // Checking if the file exists before moving it
                    sshagent(['ansible']) {
                        sh """
                            if [ -f /tmp/ansible-playbook.yml ]; then
                                mv /tmp/ansible-playbook.yml /home/ubuntu
                            else
                                echo "File not found in /tmp, skipping move."
                            fi
                        """
                    }
                }
            }
        }
    }
}
