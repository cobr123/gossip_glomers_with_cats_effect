#!/bin/bash
set -e

sbt unique-ids/nativeImage
~/Downloads/maelstrom/maelstrom test -w unique-ids --bin ./unique-ids/target/native-image/unique-ids --time-limit 30 --rate 1000 --node-count 3 --availability total --nemesis partition