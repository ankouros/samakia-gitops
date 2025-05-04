pipeline {
    agent {
        kubernetes {
            label 'jenkins-agent-pod'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    app: jenkins-agent
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:4.11.2-4
    resources:
      limits:
        cpu: 500m
        memory: 1Gi
      requests:
        cpu: 200m
        memory: 512Mi
  - name: kubectl
    image: bitnami/kubectl:1.27
    command: ['cat']
    tty: true
  - name: helm
    image: alpine/helm:3.12.0
    command: ['cat']
    tty: true
  - name: argocd
    image: argoproj/argocd-cli:2.7.4
    command: ['cat']
    tty: true
"""
        }
    }

    environment {
        GIT_REPO = 'git@github.com:ankouros/samakia-gitops.git'
        KUBE_NAMESPACE = 'samakia'
        ARGOCD_SERVER = 'argocd-server.argocd.svc.cluster.local'
        HELM_CHART_DIR = 'argo/apps'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                credentialsId: 'github-ssh-key',
                url: env.GIT_REPO
            }
        }

        stage('Lint Helm Charts') {
            steps {
                container('helm') {
                    script {
                        def charts = sh(script: "ls ${env.HELM_CHART_DIR} | grep -v 'argocd-apps.yaml'", returnStdout: true).trim().split('\n')
                        for (chart in charts) {
                            dir("${env.HELM_CHART_DIR}/${chart}") {
                                if (fileExists('Chart.yaml')) {
                                    sh "helm lint ."
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Unit Tests') {
            steps {
                container('kubectl') {
                    script {
                        // Add your unit tests here (e.g., conftest for policy validation)
                        sh """
                        kubectl kustomize --enable-helm . | kubectl apply --dry-run=server -f -
                        """
                    }
                }
            }
        }

        stage('Deploy to ArgoCD') {
            steps {
                container('argocd') {
                    script {
                        withCredentials([string(credentialsId: 'argocd-admin-token', variable: 'ARGOCD_TOKEN')]) {
                            sh """
                            argocd login ${env.ARGOCD_SERVER} --grpc-web --insecure --username admin --password ${env.ARGOCD_TOKEN}
                            argocd app sync samakia-apps
                            argocd app wait samakia-apps --health
                            """
                        }
                    }
                }
            }
        }

        stage('Integration Tests') {
            steps {
                container('kubectl') {
                    script {
                        // Example: Verify deployments are healthy
                        sh """
                        kubectl -n ${env.KUBE_NAMESPACE} wait --for=condition=available --timeout=300s deployment/jenkins
                        kubectl -n ${env.KUBE_NAMESPACE} wait --for=condition=available --timeout=300s deployment/minio
                        """
                    }
                }
            }
        }

        stage('Notify') {
            steps {
                script {
                    // Send notifications to Slack/Teams
                    slackSend color: 'good', 
                              message: "Pipeline SUCCESSFUL: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            script {
                slackSend color: 'danger', 
                          message: "Pipeline FAILED: ${env.JOB_NAME} ${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            }
        }
    }
}