
import utilities.Submodule

repo = 'submod-red'

buildFlowJob("${repo}-build-flow") {
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

  build('${repo}-build')
  build('${repo}-test')
  build('${repo}-release')
  """.stripIndent())

  scm {
    github('praqma-test/submod-red',
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

Submodule.getTestJob(job('lower-letters-test'), 'praqma-test/lower-letters') {
  steps {
    shell('./test.sh')
  }
}

Submodule.getReleaseJob(job('lower-letters-release'), 'praqma-test/lower-letters')

job("capital-letters-test") {
  description('Test capital-letters submodule')

  scm {
    github('praqma-test/capital-letters')
    { scm ->
      scm / branches / 'hudson.plugins.git.BranchSpec' {
        name 'refs/heads/feature/1'
      }
    }
  }

  steps {
    shell('''\
      git checkout master
      git checkout feature/1
      git merge master
    '''.stripIndent())

    shell('./test.sh')
  }

}

job("capital-letters-release") {
  description('Merge capital-letters ready branch into master')

  scm {
    github('praqma-test/capital-letters',
      { scm ->
        scm / branches / 'hudson.plugins.git.BranchSpec' {
          name 'master'
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
      branch('origin', 'master')
    }
  }
}

job("${repo}-build") {
  description('Check out feature branch and build source code')

  scm {
    github('praqma-test/submod-red',
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

job("${repo}-test") {
  description('Run tests on the artifact from the build job')

  steps {
    copyArtifacts("${repo}-build") {
      includePatterns('archive.tgz')
    }

    shell('./test.sh')
  }
}

job("${repo}-release") {
  description('Check out master and merge feature branch into master')

  scm {
    github('praqma-test/submod-red',
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
