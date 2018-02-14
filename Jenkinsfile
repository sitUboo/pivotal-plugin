def mvn(String goals) {
    def mvnHome = tool "Maven-3.2.3"
    def javaHome = tool "JDK1.8.0_102"

    withEnv(["JAVA_HOME=${javaHome}", "PATH+MAVEN=${mvnHome}/bin"]) {
        sh "mvn -B ${goals}"
    }
}

stage("build"){
    node(""){
        git 'git@github.com:sitUboo/pivotal-plugin.git'
        echo env.BRANCH_NAME
        mvn "clean verify"
        sh "ls;exit"
    }
}

stage("test"){
  node {
   parallel (
     phase1: { 
         //sh "echo p1; sleep 20s; echo phase1"
         readUrl
         readUrl('http://www.cnn.com')
         readUrl('http://www.google.com')
     },
     phase2: { 
         //sh "echo p2; sleep 40s; echo phase2" 
         readUrl('http://www.google.com')
         readUrl('http://www.cnn.com')
         readUrl
     },
     phase3: { 
         //sh "echo p1; sleep 20s; echo phase1"
         try {
           readUrl
         } catch(err) {
             echo 'Something bad happened'
             echo err.toString()
         }
         readUrl('http://www.cnn.com')
         readUrl('http://www.google.com')
     },
     phase4: { 
         //sh "echo p2; sleep 40s; echo phase2" 
         readUrl('http://www.google.com')
         readUrl('http://www.cnn.com')
         readUrl
     }
   )
  sh "echo run this after both phases complete"   
 }
}
