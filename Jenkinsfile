node('imac') {
    checkout scm
    properties([buildDiscarder(logRotator(numToKeepStr: '15'))])
    gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

    catchError {
        stage('Setup') {
            shWithColor 'bundle install'
        }

        stage('Test') {
            shWithColor 'bundle exec fastlane test'
            junit 'build/test_output/*.junit'
        }

        stage('Build') {
            withCredentials([string(credentialsId: 'JENKINS_PASS', variable: 'JENKINS_PASS')]) {
                env.GIT_COMMIT = gitCommit
                shWithColor '''
                security -v unlock-keychain -p "$JENKINS_PASS" /Users/jenkins/Library/Keychains/login.keychain
                bundle exec fastlane build
                '''
            }        
            archive 'build/*.ipa, build/*.zip'
        }

    }

    step([$class: 'Mailer', notifyEveryUnstableBuild: false, recipients: 'youremail@example.com', sendToIndividuals: true])
}

void shWithColor(String cmd) {
    ansiColor('xterm') {
        sh "#!/bin/bash -l\n${cmd}"        
    }
}
