#!/bin/bash
## Set Git aliases (local to a repository).
## Should be run at the root of a superproject.

## This blog post describes the concept of using shell functions in aliases:
## http://blogs.atlassian.com/2014/10/advanced-git-aliases/

# Pull all remote changes to the superproject
# and all submodules.
# Use 'git rev-parse' to get the branch name, such that the same branch is pulled
# for both the superproject and all submodules.
# Gotcha: 'git submodule update' will silently overwrite any local changes
# within a submodule. See https://git.wiki.kernel.org/index.php/GitSubmoduleTutorial
git config alias.pullall "!f() { git pull && \
    git submodule update --init --recursive && \
    git submodule foreach git pull origin `git rev-parse --abbrev-ref HEAD`; \
  }; f"

# Show the status of the superproject and all submodules.
git config alias.statusall "!f() { \
    git submodule foreach git status && \
    git status; \
  }; f"

# Stage all changes in submodules and in the superproject.
git config alias.addall "!f() { \
    git submodule foreach git add --all && \
    git add --all; \
  }; f"

# Commit in submodules and the superproject.
# Use 'git diff' to check that there are staged changes - otherwise 'git commit'
# exits with an error code.
git config alias.commitall "!f() { \
    git submodule foreach 'git diff --quiet --exit-code --cached || git commit' && \
    git commit; \
  }; f"

# Push submodules, add the submodules, and push the superproject.
git config alias.pushall "!f() { \
    git submodule foreach git push && \
    grep path .gitmodules | sed 's/.*= //' | xargs git add && \
    git diff --quiet --exit-code --cached || git commit -m 'Update submodules' && \
    git push; \
  }; f"
