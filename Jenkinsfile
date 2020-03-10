pipeline {
    agent any 
    stages {
        stage('checkout') { 
            steps {
                checkout scm
            }
        }
        stage('Test') { 
            steps {
                sh 'ls;env;touch test.txt' 
                archiveArtifacts 'test.txt'
            }
        }
    }
}
