#!/bin/sh
# mark all picks as reword so we can change messages
sed -i "" -e 's/^pick /reword /' "$1"
