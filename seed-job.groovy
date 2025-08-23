job('seed-job') {
    description('Seed job that loads jobs.groovy to generate pipeline jobs')

    scm {
        git('https://github.com/dabah987/jenkins-job-dsl.git', 'main')
        
    }

    steps {
        dsl {
            // points to jobs.groovy inside your repo
            external('jobs.groovy')
            removeAction('DELETE')  // remove old jobs not in DSL
            ignoreExisting(false)   // overwrite existing jobs
        }
    }
}
