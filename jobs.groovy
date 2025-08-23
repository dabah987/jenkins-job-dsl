// jobs.groovy
// Jenkins Job DSL script to create 3 pipeline jobs

pipelineJob('flask_docker_build') {
    description('Build Flask app Docker image and push to DockerHub')
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Checkout') {
                            steps {
                                git 'https://github.com/YOUR_USER/YOUR_FLASK_REPO.git'
                            }
                        }
                        stage('Build Docker Image') {
                            steps {
                                sh 'docker build -t macen9/flask-app:latest .'
                            }
                        }
                        stage('Push to DockerHub') {
                            steps {
                                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                                    sh 'echo \$PASS | docker login -u \$USER --password-stdin'
                                    sh 'docker push macen9/flask-app:latest'
                                }
                            }
                        }
                    }
                }
            """.stripIndent())
            sandbox()
        }
    }
}

pipelineJob('nginx_proxy_build') {
    description('Build Nginx reverse proxy image with header injection and push to DockerHub')
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Prepare Dockerfile') {
                            steps {
                                writeFile file: 'default.conf', text: '''server {
    listen 80;
    location / {
        proxy_pass http://flask-app:5000;
        proxy_set_header X-Forwarded-For \\remote_addr;
    }
}'''
                                writeFile file: 'Dockerfile', text: '''FROM nginx:alpine
COPY default.conf /etc/nginx/conf.d/default.conf'''
                            }
                        }
                        stage('Build Docker Image') {
                            steps {
                                sh 'docker build -t macen9/nginx-proxy:latest .'
                            }
                        }
                        stage('Push to DockerHub') {
                            steps {
                                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                                    sh 'echo \$PASS | docker login -u \$USER --password-stdin'
                                    sh 'docker push macen9/nginx-proxy:latest'
                                }
                            }
                        }
                    }
                }
            """.stripIndent())
            sandbox()
        }
    }
}

pipelineJob('deploy_and_test') {
    description('Run Flask + Nginx containers locally, test request, then clean up')
    definition {
        cps {
            script("""
                pipeline {
                    agent any
                    stages {
                        stage('Run Containers') {
                            steps {
                                sh 'docker network create testnet || true'
                                sh 'docker run -d --rm --name flask-app --network testnet macen9/flask-app:latest'
                                sh 'docker run -d --rm -p 8080:80 --name nginx-proxy --network testnet macen9/nginx-proxy:latest'
                            }
                        }
                        stage('Test Request') {
                            steps {
                                sh 'sleep 5'  // give containers time to start
                                sh 'curl -v http://localhost:8080'
                            }
                        }
                    }
                    post {
                        always {
                            sh 'docker stop flask-app || true'
                            sh 'docker stop nginx-proxy || true'
                            sh 'docker network rm testnet || true'
                        }
                    }
                }
            """.stripIndent())
            sandbox()
        }
    }
}
