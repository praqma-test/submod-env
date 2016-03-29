# submod-env

Continuous delivery environment for a Git submodule test setup.


## Overview

This project is used to create and exercise build pipelines for Git 'superprojects',
that is, projects using submodules.

The project uses Jenkins and Job DSL to create pipelines for these superprojects:

* [praqma-test/super-red](https://github.com/praqma-test/super-red)
* [praqma-test/super-green](https://github.com/praqma-test/super-green)

Each superproject uses one or more submodules also located in `praqma-test`.
A submodule is used by one or more superprojects.


## Usage

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
* Run the seed job to create all jobs.
* Edit the configuration of all `release` jobs to use the credentials in the
  __Source Code Management__ section.
