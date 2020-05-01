pipeline {
  options {
      timestamps()
      preserveStashes()
      buildDiscarder(logRotator(numToKeepStr: '10'))
  }   
  agent none

  stages{
    stage('Build'){
      agent {
        label 'mothership'
      }   
      steps{         
        script{
           sh 'echo hello build'
        }             
      }   
      post {
        failure {
          script{
            sh 'echo failed'    
          }          
        }
         success {
          deleteDir()
         }   
      }
    }
      
    stage('Unit Tests'){
      agent {
        label 'remote'
      }
      steps{
        script{
          sh 'echo test'
        }
      }
      post {
        failure {
          script{
            echo 'echo failed'
          }
        }
        success {
          deleteDir()
        }
      }
    }
  }
}

