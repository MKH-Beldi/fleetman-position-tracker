pipeline {
    agent any
    tools {
        maven 'maven 3.8.6'
    }
    environment {
        imageName = "fleetman-position-tracker"
        registry = ''
        registryCredentials = "nexus"
        dockerImage = ''
    }

    stages {

        stage("Set environment Develop") {
             when {
                branch "feature/*"
             }
            steps{
                 script {
                    registry = "nexus-registry.eastus.cloudapp.azure.com:8086/"
                 }
            }
        }

        stage("Set environment QA") {
             when {
                branch "release/*"
             }
            steps{
                 script {
                    registry = "nexus-registry.eastus.cloudapp.azure.com:8088/"
                 }
            }
        }

        stage('Git Preparation') {
            steps {
                cleanWs()
                checkout scm
                sh 'git rev-parse --short HEAD > .git/commit-id'
                script {
                    commit_id = readFile('.git/commit-id').trim()
                    branch_git = env.BRANCH_NAME
                }
                sh 'chmod 775 *'
            }
        }

        stage('JUnit Test') {
            steps {
                  sh './mvnw test'
            }
        }

        stage('Build Jar') {
            steps {
                sh './mvnw package'
            }
        }

        stage('SonarQube Scan Code Quality') {
            steps {
                withSonarQubeEnv('sonarqubeIns') {
                  sh './mvnw sonar:sonar'
                }
            }
        }

        stage("Quality Gate from SonarQube") {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Docker image environment Develop') {
            when {
                branch "feature/*"
            }
            steps {
                script {
                    dockerImage = docker.build imageName + ":${commit_id}-dev"
                }
            }
        }

        stage('Build Docker image environment QA') {
            when {
                branch "release/*"
            }
            steps {
                script {
                    dockerImage = docker.build imageName + ":${commit_id}-test"
                }
            }
        }

        stage('Push Docker image to Nexus Registry') {
            steps {
                script {
                    docker.withRegistry( 'https://'+registry, registryCredentials) {
                        dockerImage.push()
                        dockerImage.push("latest")
                    }
                }
            }
        }
        
        stage('Trigger K8S Manifest Updating') {
            steps {
                build job: 'k8s-update-manifests-fleetman-position-tracker', parameters: [string(name: 'DOCKERTAG', value: commit_id), string(name: 'BRANCH_GIT', value: branch_git)]
            }
        }
    }
}
