
import utilities.Submodule
import utilities.Superproject

/* The superprojects, located in praqma-test. */
superRedRepo = 'super-red'
superGreenRepo = 'super-green'

/* The build flow pipeline for the super-red superproject. */
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

/* The build flow pipeline for the super-green superproject. */
Superproject.getBuildFlow(superGreenRepo, """\
  parallel (
    {
      build('lower-letters-test')
      build('lower-letters-release')
    },
  )

  build('${superGreenRepo}-build')
  build('${superGreenRepo}-test')
  build('${superGreenRepo}-release')
  """.stripIndent()
)

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


/* Superproject: praqma-test/super-red */
Superproject.getBuildJob(job("${superRedRepo}-build"), "praqma-test/${superRedRepo}") {
  steps {
    shell('./build.sh')
  }

  publishers {
    archiveArtifacts {
      pattern('archive.tgz')
    }
  }
}
Superproject.getTestJob(job("${superRedRepo}-test"), "praqma-test/${superRedRepo}") {
  steps {
    copyArtifacts("${superRedRepo}-build") {
      includePatterns('archive.tgz')
    }
    // Just test that it looks like a tar archive.
    shell('tar -tvf archive.tgz')
  }
}
Superproject.getReleaseJob(job("${superRedRepo}-release"), "praqma-test/${superRedRepo}")


/* Superproject: praqma-test/super-green */
Superproject.getBuildJob(job("${superGreenRepo}-build"), "praqma-test/${superGreenRepo}") {
  steps {
    shell('./build.sh')
  }

  publishers {
    archiveArtifacts {
      pattern('output.txt')
    }
  }
}
Superproject.getTestJob(job("${superGreenRepo}-test"), "praqma-test/${superGreenRepo}") {
  steps {
    copyArtifacts("${superGreenRepo}-build") {
      includePatterns('output.txt')
    }
    shell('cat output.txt')
  }
}
Superproject.getReleaseJob(job("${superGreenRepo}-release"), "praqma-test/${superGreenRepo}")
