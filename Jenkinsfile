node {
   echo 'Hello World'
   checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'PathRestriction', excludedRegions: '*/pom.xml', includedRegions: '']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.com:sitUboo/Pivotal-plugin.git']]])
}
