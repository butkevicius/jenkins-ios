def branches = []
readFileFromWorkspace("branches.txt").eachLine { branches << it }

branches.each {
    def branchName = it
    def branchTitle = branchName.substring(7).replaceAll('/', '-').replaceAll('#', '')
    def jobName = "name_${branchTitle}_ios"
    println(jobName)
    job(jobName) {
        displayName("Name ${branchName} iOS")
        logRotator {
            numToKeep(15)
        }
        authorization {
            permission('hudson.model.Item.Discover', 'user')
            permission('hudson.model.Item.Read', 'user')
            permission('hudson.model.Item.Workspace', 'user')
            permission('hudson.model.Item.Build', 'user')
            permission('hudson.model.Item.Cancel', 'user')
        }
        label('imac')
        scm {
            git {
                remote {
                    name('origin')
                    url('repo')
                    credentials('credentials')
                }
                branch(branchName)
                extensions {
                    cleanBeforeCheckout()
                }
            }
        }

        triggers {
            scm('')
        }

        wrappers {
            maskPasswords()
            colorizeOutput()
        }

        steps {
            shell('''\
#!/bin/bash -l

security -v unlock-keychain -p "$JENKINS_PASS" /Users/jenkins/Library/Keychains/login.keychain

bundle install
bundle exec fastlane build
'''
            )
            shell('cloc --by-file --xml --out=build/cloc.xml DIR')
        }

        publishers {
            archiveArtifacts {
                pattern('build/*.ipa, build/*.zip')
                onlyIfSuccessful()
            }
            //mailer('email@telesoftas.com', false, true)
        }
        configure { project ->
            project / 'publishers' / 'hudson.plugins.sloccount.SloccountPublisher'(plugin: "sloccount@1.21") {
                'pattern'('build/cloc.xml')
                'encoding'('')
                'commentIsCode'('false')
                'numBuildsInGraph'('0')
                'ignoreBuildFailure'('false')
            }
        }
    }
    if (!jenkins.model.Jenkins.instance.getItemByFullName(jobName)) {
        queue(jobName)
    }
}
