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
* Edit the configuration of the job `submod-red-release` to use the credentials in the
  __Source Code Management__ section.

## Submodule Workflow

Assume a workflow where developers check out super repositories with submodules as subfolders inside.
Changes are made to both submodules and super repositories at the same time.

These `git` commands are involved:

### Editing a submodule

```sh
$ cd submod-square
$ <hack hack hack>
$ git commit -m "Updating stuff in the submodule"
$ git push
```

### Then editing the super repo code and updating the submodule in the super repo

```sh
$ cd ..
$ <hack hack hack>
$ git add submod-square
$ git commit -m "Updating submodule in super repo"
$ git push
```

### Updating to get other changes

```sh
$ cd super
$ git pull
$ git submodule update
```

__Note:__ The submodule update will silently overwrite local changes within a submodule.
We should check for that first.

### Other flows to consider

* Initialization
* "super git pull" - getting updates to both super repo and submodules at once
* "super git status" - seeing all local, unstaged changes
* "super git commit/push" - help to push subs, then super, in a single step
* The situations where you reach a detached head state


