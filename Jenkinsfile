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
    node(""){
        git 'https://github.com/sitUboo/Yui'
        sh("ls")
    }
}
