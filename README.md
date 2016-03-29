# submod-env

Continuous delivery environment for a Git submodule test setup.


## Overview

This project is used to create and exercise build pipelines for Git 'superprojects',
that is, projects using submodules.

It is aimed at a workflow where developers work on feature branches and never push
directly to `master`. Instead, `master` is used as the integration branch and only the
build server pushes to `master`, once changes have been validated. This is similar to the
[Pretested Integration Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Pretested+Integration+Plugin)
but in the context of using submodules.

The project uses Jenkins and Job DSL to create pipelines for these superprojects:

* [praqma-test/super-red](https://github.com/praqma-test/super-red)
* [praqma-test/super-green](https://github.com/praqma-test/super-green)

Each superproject uses one or more submodules also located in `praqma-test`.
A submodule is used by one or more superprojects.

This test setup uses a fixed feature branch called `feature/1`. The branch name is the
same for both superprojects and submodules.

The flow of a superproject pipeline is:

* Trigger a pipeline build on SCM changes on a superproject branch
* In parallel, build and test all submodules used by the superproject
  * This is done on branches with the same name (but in the context of a submodule's repository)
  * If successful, changes to a submodule are fast-forward merged to the submodule's master branch
* Test the superproject
  * If successful, changes to the superproject are fast-forward merged to the superproject's master branch


## Creating the environment

The environment uses Docker, so a Docker machine must be runnning.
Execute these commands to start and configure Jenkins:

```sh
$ cd jenkins
$ ./build.sh
$ ./run.sh
```

Now Jenkins runs on `http://$(docker-machine ip default):8080`.

You then need to manually configure it with credentials to push to GitHub:

* Add credentials in Jenkins, for example with the name `github-push`.
* Edit the configuration of all `release` jobs to use the credentials in the
  __Source Code Management__ section.
* (Optional) For the `build-flow` jobs, set up a build trigger to poll SCM for example
  every two minutes: `H/2 * * * *`


## Testing

The CD environment can be tested by checking out the test projects and pushing changes to
the `feature/1` branch. This section contains some example steps. See also the
[GitSubmoduleTutorial](https://git.wiki.kernel.org/index.php/GitSubmoduleTutorial).

This assumes that Jenkins is configured with a build trigger to pick up SCM changes on a
feature branch.

```sh
$ git clone https://github.com/praqma-test/submod-env.git # to get set-aliases.sh
$ git clone https://github.com/praqma-test/super-red.git  # example superproject
$
$ cd super-red
$ git checkout feature/1         # developers always work on feature branches
$ ../submod-env/set-aliases.sh   # custom git aliases for submodules
$ git pullall                    # init project and submodules
$ <hack hack hack>               # make some local changes
$ git statusall                  # see changes
$ git addall                     # stage changes
$ git commitall                  # commit recursively
$ git pushall                    # push recursively
```

When Jenkins picks up the changes, it starts the build flow pipeline for the superproject.
The submodule parts of the pipeline should merge changes to the `master` branch of the
submodules. Then, the superproject part of the pipeline should do the same for the
superproject.

Following that, another developer should be able to fetch all changes from `master` into
another feature branch.
