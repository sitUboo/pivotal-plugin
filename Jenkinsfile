stage('checkout'){
    node(){
        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PathRestriction', excludedRegions: '', includedRegions: 'junk/**']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.com:sitUboo/Yui.git']]])
    }
}
stage('build'){
    node(){
        sh 'ls'
    }
}
