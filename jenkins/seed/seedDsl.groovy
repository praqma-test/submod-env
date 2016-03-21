
repo = 'submod-red'

buildFlowJob("${repo}-build-flow") {
  buildFlow("""\
  parallel (
    {
      build('lower-letters-test')
      build('lower-letters-release')
    },
    // TODO: Other submodules
  )

  build('${repo}-build')
  build('${repo}-test')
  build('${repo}-release')
  """.stripIndent())

}

job("lower-letters-test") {
  description('Test lower-letters submodule')

  scm {
    github('praqma-test/lower-letters')
    { scm ->
      scm / branches / 'hudson.plugins.git.BranchSpec' {
            name 'feature/1'
      }
    }
  }

  steps {
    shell('./test.sh')
  }

}

job("lower-letters-release") {
  description('Merge lower-letters ready branch into master')

  scm {
    github('praqma-test/lower-letters',
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
             	name 'feature/1'
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
    shell('./build.sh')
  }

  publishers {
    archiveArtifacts {
      pattern('main')
    }
  }
}

job("${repo}-test") {
  description('Run tests on the artifact from the build job')

  steps {
    copyArtifacts("${repo}-build") {
      includePatterns('main')
    }

    shell('''\
    chmod a+x main
    ./main $BUILD_NUMBER
    '''.stripIndent())
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
