pipeline {
    agent any 
    stages {
        stage('checkout') { 
            steps {
                scm 
            }
        }
        stage('Test') { 
            steps {
                sh 'ls;scm' 
            }
        }
    }
}
