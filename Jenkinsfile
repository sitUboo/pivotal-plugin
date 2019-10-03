node {
   echo 'Hello World'
   checkout([$class: 'GitSCM',
             branches: [[name: '*/master']],
             doGenerateSubmoduleConfigurations: false,
             extensions: [[$class: 'DisableRemotePoll'],
                          [$class: 'UserExclusion', excludedUsers: 'noreply']
                         ],
             submoduleCfg: [],
             userRemoteConfigs: [[url: 'git@github.com:sitUboo/pivotal-plugin.git']]
            ])
   sh "ls"
}
