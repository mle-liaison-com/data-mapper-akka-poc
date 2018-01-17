#!groovy

@Library('visibilityLibs')
import com.liaison.jenkins.visibility.Utilities;

def utils = new Utilities();

node('agent') {
    stage('checkout') {
        timeout(5) {
            checkout scm
        }
    }

    stage('build') {
        timeout(10) {
            env.PACKAGE_NAME = utils.runSh('sh scripts/projectname.sh')
            env.PACKAGE_VERSION = utils.runSh('sh scripts/projectversion.sh')

            echo "package name: ${env.PACKAGE_NAME}"
            echo "package version: ${env.PACKAGE_VERSION}"

            sh "./gradlew clean build"
        }
    }

    stage('dist') {
        timeout(5) {
            sh "./gradlew assemble"
        }
    }

    // setting up docker image & container names
    dockerImageName = "hermes/${env.PACKAGE_NAME}";

    stage('docker build') {
        timeout(5) {
            //utils.dockerBuild(dockerImageName)
            def dockerRegistryHost = env.DOCKER_REGISTRY_HOST;
            def dockerCredentialsId = "jenkins-docker-publisher";
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: dockerCredentialsId,
                usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD']]) {
                // authenticating to docker
                sh "docker login -u ${env.DOCKER_USERNAME} -p ${env.DOCKER_PASSWORD} ${dockerRegistryHost}"

                // build the image
                sh "docker build --build-arg APPLICATION_ID=${env.PACKAGE_NAME} -t ${dockerImageName} ."
            }
        }
    }

    stage('docker push') {
        timeout(5) {
            utils.dockerPush(dockerImageName, env.PACKAGE_VERSION)
            utils.dockerPush(dockerImageName, "latest")
        }
    }

    stage ('k8s setup dev') {
        withCluster(credentialId: 'at4d-c1', namespace: 'dev') {
            if (env.PACKAGE_NAME == 'akka-nucleus') {
                sh 'kubectl apply -f k8s/dev_default_dev-int_us_at4/env/alloy-core-akkanucleus.yaml -n dev'
                sh 'kubectl apply -f k8s/dev_default_dev-int_us_at4/secret/alloy-core-akkanucleus.yaml -n dev'
            }
            sh 'kubectl apply -f k8s/dev_default_dev-int_us_at4/ingress/alloy-core-akkanucleus-worker.yaml -n dev'
        }
    }

    stage ('k8s deploy dev') {
        // Deploy to Kubernetes
        withCluster(credentialId: 'at4d-c1', namespace: 'dev') {
            def dockerImagePrimaryName = "hermes/${env.PACKAGE_NAME}:${env.PACKAGE_VERSION}"

            // clustered
            if (fileExists('k8s-file-seed.yaml')) {
                utils.deployToKubernetes('k8s-file-seed.yaml', dockerImagePrimaryName, 'dev')
                // allow all seeds nodes to be up before deploying workers
                sleep 60
                utils.deployToKubernetes('k8s-file-worker.yaml', dockerImagePrimaryName, 'dev')
            } else {
                utils.deployToKubernetes('k8s-file.yaml', dockerImagePrimaryName, 'dev')
            }
        }
    }
}
