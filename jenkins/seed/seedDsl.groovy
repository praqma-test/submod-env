job('submod-red') {
  description('Build the submod-red project')

  scm {
    github('praqma-test/submod-red',
      { scm ->
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
    ./build.sh
    ./main 3
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
