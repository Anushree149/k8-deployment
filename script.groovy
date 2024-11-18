pipeline { 
    agent any 

    stages { 
        stage('Git Clone') { 
            steps {
                git branch: "main", url:'https://github.com/Ab-D-ev/kubernetes-devops-project.git' 
            } 
        } 
        
        stage("docker build"){
        sh 'docker image build -t $JOB_NAME:v1.$BUILD_ID .'
        sh 'docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:v1.$BUILD_ID'
        sh 'docker image tag $JOB_NAME:v1.$BUILD_ID anushree039/$JOB_NAME:latest'
        }
    }
}