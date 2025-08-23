job('seed_job') {
    description('Seed job that loads jobs.groovy to generate pipeline jobs')

    scm {
        git {
            remote {
                url('https://github.com/dabah987/jenkins-job-dsl.git')
            }
            branch('*/main')
            extensions {
                cloneOptions {
                    shallow(true)
                    noTags(true)
                    reference('')
                    timeout(10)
                }
            }
        }
    }

    wrappers {
        preBuildCleanup()
    }

    steps {
        dsl {
            external('jobs.groovy')
            removeAction('DELETE')
            ignoreExisting(false)
        }
    }
}
