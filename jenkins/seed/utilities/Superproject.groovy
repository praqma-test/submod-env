package utilities

/**
 * Utilities for creating superproject jobs. A superproject contains submodules and
 * owns a build flow.
 */
public class Superproject {

  /**
   * Create a job that checks out a feature branch, merges master into it
   * and builds.
   *
   * @param job Initial job with a name.
   * @param repo GitHub repository name.
   * @param buildClosure Closure containing the build and publish steps.
   */
  static def getBuildJob(def job, def repo, Closure buildClosure) {
    job.with {
      description "Check out feature branch and build the ${repo} superproject"
      scm {
        github(repo) { scm ->
          scm / branches / 'hudson.plugins.git.BranchSpec' {
            name 'refs/heads/feature/1'
          }
          scm / 'extensions' / 'hudson.plugins.git.extensions.impl.SubmoduleOption' {
            disableSubmodules false
            recursiveSubmodules true
            trackingSubmodules false
          }
        }
      }

      steps {
        shell('''\
          git checkout master
          git checkout feature/1
          git merge master
        '''.stripIndent())
      }
    }

    buildClosure.delegate = job
    buildClosure.run()
  }

  /**
   * Create a test job for a superproject repository.
   *
   * @param job Initial job with a name.
   * @param repo GitHub repository name.
   * @param testClosure Closure containing the test steps, including copying artifacts
   * from a build job.
   */
  static def getTestJob(def job, def repo, Closure testClosure) {
    job.with {
      description "Test the ${repo} superproject"
    }

    testClosure.delegate = job
    testClosure.run()
  }

  /**
   * Create a job that merges a feature branch into master for a superproject.
   *
   * @param job Initial job with a name.
   * @param repo GitHub repository name.
   */
  static def getReleaseJob(def job, def repo) {
    job.with {
      description("Merge the ${repo} feature branch into master")

      scm {
        github(repo) { scm ->
          scm / branches / 'hudson.plugins.git.BranchSpec' {
            name 'master'
          }
          scm / 'extensions' / 'hudson.plugins.git.extensions.impl.SubmoduleOption' {
            disableSubmodules false
            recursiveSubmodules true
            trackingSubmodules false
          }
        }
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
  }

}
