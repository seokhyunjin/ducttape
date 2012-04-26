#!/usr/bin/env bash
set -ueo pipefail
#scriptDir=$(scala -e 'println(new java.io.File("'$(dirname $0)'").getAbsolutePath)')
scriptDir=$(readlink -f $(dirname $0))

echo >&2 "Building JAR..."
(cd $scriptDir/target/scala-2.9.2/classes; zip -qr $scriptDir/ducttape.jar *)
