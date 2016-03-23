
import utilities.Submodule
import utilities.Superproject

/* The superprojects, located in praqma-test. */
superRedRepo = 'super-red'
superGreenRepo = 'super-green'

buildFlowJob("${superRedRepo}-build-flow") {
  buildNeedsWorkspace() // In order to detect SCM changes

  buildFlow("""\
  parallel (
    {
      build('lower-letters-test')
      build('lower-letters-release')
    },
    {
      build('capital-letters-test')
      build('capital-letters-release')
    },
  )

  build('${superRedRepo}-build')
  build('${superRedRepo}-test')
  build('${superRedRepo}-release')
  """.stripIndent())

  scm {
    github("praqma-test/${superRedRepo}",
      { scm ->
        scm / branches / 'hudson.plugins.git.BranchSpec' {
          name 'refs/heads/feature/1'
        }
        scm / 'extensions' / 'hudson.plugins.git.extensions.impl.SubmoduleOption' {
          disableSubmodules false
          recursiveSubmodules true
          trackingSubmodules false
        }
      }
    )
  }

  triggers {
    scm('H/2 * * * *')
  }
}

/* Submodules in praqma-test.  Each is assumed to have a test.sh script. */
submodules = [
    'capital-letters',
    'lower-letters',
  ]
submodules.each {
  Submodule.getTestJob(job("${it}-test"), "praqma-test/${it}") {
    steps {
      shell('./test.sh')
    }
  }
  Submodule.getReleaseJob(job("${it}-release"), "praqma-test/${it}")
}

job("${superRedRepo}-build") {
  description('Check out feature branch and build source code')

  scm {
    github("praqma-test/${superRedRepo}",
      { scm ->
        scm / branches / 'hudson.plugins.git.BranchSpec' {
          name 'refs/heads/feature/1'
        }
        scm / 'extensions' / 'hudson.plugins.git.extensions.impl.SubmoduleOption' {
          disableSubmodules false
          recursiveSubmodules true
          trackingSubmodules false
        }
      }
    )
  }

  steps {
    shell('''\
      git checkout master
      git checkout feature/1
      git merge master
    '''.stripIndent())

    shell('./build.sh')
  }

  publishers {
    archiveArtifacts {
      pattern('archive.tgz')
    }
  }
}

job("${superRedRepo}-test") {
  description('Run tests on the artifact from the build job')

  steps {
    copyArtifacts("${superRedRepo}-build") {
      includePatterns('archive.tgz')
    }

    shell('./test.sh')
  }
}

job("${superRedRepo}-release") {
  description('Check out master and merge feature branch into master')

  scm {
    github("praqma-test/${superRedRepo}",
      { scm ->
        scm / branches / 'hudson.plugins.git.BranchSpec' {
          name 'master'
        }
        scm / 'extensions' / 'hudson.plugins.git.extensions.impl.SubmoduleOption' {
          disableSubmodules false
          recursiveSubmodules true
          trackingSubmodules false
        }
      }
    )
  }

  /** Merge feature branch into master. */
  steps {
    shell('''\
    git checkout feature/1
    git pull
    git checkout master
    git merge --ff-only feature/1
    '''.stripIndent())
  }

  /** Push to master. */
  publishers {
    git {
      pushOnlyIfSuccess()
      branch("origin", "master")
    }
  }
}

Superproject.getBuildJob(job("${superGreenRepo}-build"), "praqma-test/${superGreenRepo}") {
  steps {
    shell('./build.sh')
  }

  publishers {
    archiveArtifacts {
      pattern('archive.tgz')
    }
  }
}
Superproject.getTestJob(job("${superGreenRepo}-test"), "praqma-test/${superGreenRepo}") {
  steps {
    copyArtifacts("${repo}-build") {
      includePatterns('archive.tgz')
    }
    shell('./test.sh')
  }
}
Superproject.getReleaseJob(job("${superGreenRepo}-release"), "praqma-test/${superGreenRepo}")
