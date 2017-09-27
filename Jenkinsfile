#!groovy

node('agent') {
    stage('checkout') {
        checkout scm
    }

    stage('build') {
       sh "./gradlew clean build"
    }

    stage('test') {
       sh "./gradlew test"
    }

    stage('dist') {
        sh "./gradlew assemble"
    }
}
