# submod-env

Continuous delivery environment for the submodule test setup.

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
* Edit the configuration of the release jobs to use the credentials in the
  __Source Code Management__ section.
