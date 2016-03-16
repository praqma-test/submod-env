repo = 'submod-red'

job("${repo}-build") {
  description('Check out feature branch and build source code')

  scm {
    git('https://github.com/praqma-test/submod-red.git') {
      branch 'feature/1'
      extensions {
        submoduleOptions {
          disable false
          recursive true
          tracking false
        }
      }
    }
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

    shell('./main $BUILD_NUMBER')
  }
}

job("${repo}-release") {
  description('Check out master and merge feature branch into master')

  scm {
    git('https://github.com/praqma-test/submod-red.git') {
      branch 'master'
      extensions {
        submoduleOptions {
          disable false
          recursive true
          tracking false
        }
      }
    }
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
  selectedJob('submod-red')
  alwaysAllowManualTrigger()
  showPipelineParametersInHeaders()
  showPipelineParameters()
  showPipelineDefinitionHeader()
  refreshFrequency(60)
}
