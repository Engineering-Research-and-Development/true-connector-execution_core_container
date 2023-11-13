#!/bin/bash

echo "Installing maven dependencies..."

mkdir -p $HOME/.m2/repository/de

cp -f ./ci/.m2/settings/settings.xml  $HOME/.m2
cp -rf ./ci/.m2/de/*  $HOME/.m2/repository/de/
