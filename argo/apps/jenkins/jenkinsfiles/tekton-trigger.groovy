pipeline {
    agent any
    environment {
        // Set environment variables for Tekton
        TEKTON_PIPELINE_NAME = 'build-scan-deploy'  // Tekton pipeline to trigger
        TEKTON_NAMESPACE = 'default'  // Kubernetes namespace where Tekton pipelines are deployed
        KUBECONFIG = '/home/jenkins/.kube/config'  // Path to Kubeconfig for access to the cluster
    }
    stages {
        stage('Trigger Tekton Pipeline') {
            steps {
                script {
                    // Trigger Tekton pipeline using `kubectl`
                    sh """
                    kubectl --kubeconfig=${KUBECONFIG} create -f - <<EOF
apiVersion: tekton.dev/v1beta1
kind: PipelineRun
metadata:
  generateName: ${TEKTON_PIPELINE_NAME}-run-
spec:
  pipelineRef:
    name: ${TEKTON_PIPELINE_NAME}
  params:
    - name: GIT_REPO
      value: "git@github.com:ankouros/samakia.git"  // Git repo URL to clone
    - name: GIT_BRANCH
      value: "main"  // Branch to build
    - name: IMAGE_NAME
      value: "my-image"  // Image name to be built
    - name: IMAGE_TAG
      value: "latest"  // Image tag for the build
  serviceAccountName: tekton-pipelines-sa  // Service account to use for the pipeline run
EOF
                    """
                }
            }
        }
    }
    post {
        success {
            echo 'Tekton pipeline triggered successfully!'
        }
        failure {
            echo 'Failed to trigger Tekton pipeline.'
        }
    }
}
