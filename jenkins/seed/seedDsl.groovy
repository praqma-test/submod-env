
import utilities.Submodule
import utilities.Superproject

/**
 * Create build pipelines for Git 'superprojects', i.e., projects using submodules.
 * The flow of a superproject pipeline is:
 *
 * <ul>
 * <li> Trigger a pipeline build on SCM changes on a superproject branch. </li>
 * <li> In parallel, build and test all submodules used by the superproject. This is done
 * on branches with the same name (but in the context of a submodule's repository).
 * If successful, changes to a submodule are fast-forward merged to the submodule's
 * master branch of. </li>
 * <li> Test the superproject. If successful, changes to the superproject are fast-forward
 * merged to the superproject's master branch. </li>
 * </ul>
 */


/**
 * Submodules in praqma-test used by one or more superprojects.
 * Each is assumed to have a test.sh script.
 */
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


/* The superprojects, located in praqma-test. */
superRedRepo = 'super-red'
superGreenRepo = 'super-green'


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
Superproject.getBuildFlow(buildFlowJob("${superRedRepo}-build-flow"), "praqma-test/${superRedRepo}",
  """\
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
  """.stripIndent()
)


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
Superproject.getBuildFlow(buildFlowJob("${superGreenRepo}-build-flow"), "praqma-test/${superGreenRepo}",
  """\
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
