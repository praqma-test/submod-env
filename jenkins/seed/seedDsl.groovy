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

    shell('./main $BUILD_NUMBER')
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

  steps {
    shell('''\
    git pull origin feature/1
    git push
    '''.stripIndent())
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
