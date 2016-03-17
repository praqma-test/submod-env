repo = 'submod-red'

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

  triggers {
    scm('H/2 * * * *')
  }

  steps {
    shell('./build.sh')
  }

  publishers {
    archiveArtifacts {
      pattern('main')
    }

    downstream("${repo}-test")
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

  publishers {
    downstream("${repo}-release")
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

buildPipelineView('Pipeline') {
  title('submod-red pipeline')
  displayedBuilds(50)
  selectedJob("${repo}-build")
  alwaysAllowManualTrigger()
  showPipelineParametersInHeaders()
  showPipelineParameters()
  showPipelineDefinitionHeader()
  refreshFrequency(60)
}
