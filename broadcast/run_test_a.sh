#!/bin/bash
set -e

sbt broadcast/nativeImage
~/Downloads/maelstrom/maelstrom test -w broadcast --bin ./broadcast/target/native-image/broadcast --node-count 1 --time-limit 20 --rate 10