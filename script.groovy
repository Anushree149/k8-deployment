    stages { 
        stage('Git Clone') { 
            steps {
                git branch: "main", url:'https://github.com/Ab-D-ev/kubernetes-devops-project.git' 
            } 
        } 
    }