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
                    sh 'docker image build -t $JOB_NAME:v1.$BUILD_ID .'
                    sh 'docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:v1.$BUILD_ID'
                    sh 'docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:latest'
                }

            }
         }
        }

        stage('Docker Push') {
            steps {
                script {
                    sshagent(['ansible']) {
                    withCredentials([string(credentialsId: 'DockerPass', variable: 'DockerPass')]) {
                        sh 'docker login -u anushree039 -p ${DockerPass}'
                        sh 'docker image push anushree039/$JOB_NAME:v1.$BUILD_ID'
                        sh 'docker image push anushree039/$JOB_NAME:latest'
                    }
                }
            }
        }

        // stage("Copy") {
        //     steps {
        //         script {
        //             sh "scp /var/lib/jenkins/workspace/k8-deployment/ansible-playbook.yml ubuntu@13.233.215.80:/tmp"
        //             sshagent(['ansible']) {
        //                 sh 'ssh -o StrictHostKeyChecking=no ubuntu@13.233.215.80'
        //                 sh "mv /tmp/ansible-playbook.yml /home/ubuntu"
                    // } 
                    
                // }
            } 
        }
    }

