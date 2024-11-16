pipeline { 
    agent any 

    stages { 
        stage('Git Clone') { 
            steps {
                git branch: "main", url:'https://github.com/Anushree149/k8-deployment.git' 
            } 
        } 
    }
}