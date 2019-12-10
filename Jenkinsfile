properties([pipelineTriggers([githubPush()])])
node {
   scm
   echo 'Hello World'
   echo "Git Branch ${env.BRANCH_NAME}"
   echo "Git Url ${env.GIT_URL}"

   //checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'DisableRemotePoll'], [$class: 'PathRestriction', excludedRegions: 'pom.xml', includedRegions: '']], submoduleCfg: [], userRemoteConfigs: [[url: 'git@github.com:sitUboo/pivotal-plugin.git']]])
   //checkout([$class: 'GitSCM',
   //          branches: [[name: '*/master']],
   //          doGenerateSubmoduleConfigurations: false,
   //          extensions: [[$class: 'DisableRemotePoll'],
   //                       [$class: 'UserExclusion', excludedUsers: 'noreply']
   //                      ],
   //          submoduleCfg: [],
   //          userRemoteConfigs: [[url: 'git@github.com:sitUboo/pivotal-plugin.git']]
   //         ])
   sh "ls;env"
}
