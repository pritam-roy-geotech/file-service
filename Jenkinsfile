pipeline {
    agent any

    environment {
        APP_NAME = 'file-service'
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'
        PATH = "/usr/lib/jvm/java-21-openjdk-amd64/bin:${env.PATH}"
        DOCKER_IMAGE= 'roypritam26/file-service:latest'
    }

    tools {
        maven 'Maven 3.9.13'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Building branch: ${env.BRANCH_NAME}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'mvn test'
                    }
                    post {
                        always {
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
                stage('Integration Tests') {
                    steps {
                        sh 'mvn verify -DskipUnitTests'
                    }
                    post {
                        always {
                            junit 'target/failsafe-reports/*.xml'
                        }
                    }
                }
//                 stage('Code Quality') {
//                     steps {
//                         sh 'mvn checkstyle:check'
//                     }
//                 }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build & Push') {
            when {
                branch 'master'
            }
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'docker-hub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        docker build -t ${DOCKER_IMAGE} .
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${DOCKER_IMAGE}
                    """
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to staging?', ok: 'Deploy'
                sh './scripts/deploy.sh staging ${DOCKER_IMAGE}'
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded!'
//             slackSend channel: '#ci-cd', message: "✅ Build #${BUILD_NUMBER} passed for ${APP_NAME}"
        }
        failure {
            echo 'Pipeline failed!'
//             slackSend channel: '#ci-cd', message: "❌ Build #${BUILD_NUMBER} failed for ${APP_NAME}"
//             mail to: 'team@example.com',
//                     subject: "Build Failed: ${APP_NAME} #${BUILD_NUMBER}",
//                     body: "Check ${BUILD_URL} for details."
        }
        always {
            cleanWs()
        }
    }
}