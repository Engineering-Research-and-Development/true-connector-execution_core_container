#!/bin/bash

echo "Installing maven dependencies..."

mkdir -p $HOME/.m2/de

cp -f ./ci/.m2/settings/settings.xml  $HOME/.m2/repository
cp -rf ./ci/.m2/de/*  $HOME/.m2/repository/de/
