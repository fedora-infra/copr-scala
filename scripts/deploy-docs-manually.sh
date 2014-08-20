#!/usr/bin/env bash

cwd="$( cd "${BASH_SOURCE[0]%/*}" && pwd )"
cd "$cwd/.."

PATH=/usr/lib/jvm/java-1.8.0-openjdk.x86_64/bin/:$PATH sbt -java-home /usr/lib/jvm/java-1.8.0-openjdk.x86_64/ doc

f=`mktemp -d`
git clone git@github.com:fedora-infra/copr-scala.git "$f/copr-scala.git"
pushd "$f/copr-scala.git"
  git checkout gh-pages
  git rm -rf api
popd

mkdir "$f/copr-scala.git/api"
cp -rv ./target/scala-*/api "$f/copr-scala.git/"

pushd "$f/copr-scala.git"
  git add -A
  git commit -m "[scripted] Manual docs deploy."
  git push origin gh-pages
popd
rm -rf "$f"

if [ $? == 0 ]; then
  echo "*** Done: https://codeblock.github.io/copr-scala"
  exit 0
else
  echo "*** ERROR!!! Fix the above and try again."
  exit 1
fi
