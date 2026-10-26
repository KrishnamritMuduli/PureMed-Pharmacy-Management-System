#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"
if [ ! -f "target/classes/com/pharmacy/cli/StandaloneConsoleApp.class" ]; then
    mvn compile
fi
java -cp target/classes com.pharmacy.cli.StandaloneConsoleApp
