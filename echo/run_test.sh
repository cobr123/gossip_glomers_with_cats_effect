#!/bin/bash
set -e

sbt echo/nativeImage
~/Downloads/maelstrom/maelstrom test -w echo --bin ./echo/target/native-image/echo --node-count 1 --time-limit 10