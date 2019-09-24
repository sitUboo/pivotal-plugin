stage('checkout'){
    node(){
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.com:sitUboo/Yui.git']]])
    }
}
stage('build'){
    node(){
        sh 'ls'
    }
}
