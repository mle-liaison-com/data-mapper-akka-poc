#!/bin/bash
read -r line < settings.gradle
name=$(echo $line | cut -d "'" -f 2)
echo $name
