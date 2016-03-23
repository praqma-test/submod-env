package utilities

/**
 * Utilities for creating submodule jobs.
 */
public class Submodule {

  /**
   * Create a test job, specifying the test steps. The job checks out a feature
   * branch and merges master into it before testing.
   *
   * @param job Initial job with a name.
   * @param repo GitHub repository name.
   * @param testClosure Closure containing the test steps.
   */
  static def getTestJob(def job, def repo, Closure testClosure) {
    job.with {
      description "Test the ${repo} submodule"
      scm {
        github(repo) { scm ->
          scm / branches / 'hudson.plugins.git.BranchSpec' {
            name 'refs/heads/feature/1'
          }
        }
      }
      steps {
        /* Merge master into the feature branch before testing. */
        shell('''\
          git checkout master
          git checkout feature/1
          git merge master
        '''.stripIndent())
      }
    }

    testClosure.delegate = job
    testClosure.run()
  }

  /**
   * Create a job that merges a feature branch into master for a submodule.
   *
   * @param job Initial job with a name.
   * @param repo GitHub repository name.
   */
  static def getReleaseJob(def job, def repo) {
    job.with {
      description "Merge the ${repo} feature branch into master"
      scm {
        github(repo) { scm ->
          scm / branches / 'hudson.plugins.git.BranchSpec' {
            name 'master'
          }
        }
      }

      /* Merge feature branch into master. */
      steps {
        shell('''\
          git checkout feature/1
          git pull
          git checkout master
          git merge --ff-only feature/1
        '''.stripIndent())
      }

      /* Push to master. */
      publishers {
        git {
          pushOnlyIfSuccess()
          branch('origin', 'master')
        }
      }
    }
  }

}
