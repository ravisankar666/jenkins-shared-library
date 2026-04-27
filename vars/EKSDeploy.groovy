def call (Map configMap){

    pipeline {
    //this are pre-build section
    agent {
        node {
            label 'AGENT-1'
        }
    }
    environment {
        COURSE = "Jenkins"
        appVersion = configMap.get("appVersion")
        ACC_ID = "517542309828"
        PROJECT = configMap.get("project")
        COMPONENT = configMap.get("component")
        REGION = "us-east-1"
    }
    options {
        timeout(time: 10, unit: 'MINUTES') 
        disableConcurrentBuilds()
    }
    parameters {                                                                                           /*  #it just like provide the input to pipeline */
        string(name: 'appVersion', description: 'Which app version you want to deploy')
        choice(name: 'deploy_to', choices: ['dev', 'qa', 'prod'], description: 'Pick something')
    }  
   
    stages {
        
        stage('Deploy') {
            steps {
                script{
                    
                        withAWS(region:'us-east-1',credentials:'aws-creds') {

                            sh """
                            set -e
                            aws eks update-kubeconfig --region ${REGION} --name ${PROJECT}-${params.deploy-to}
                            kubectl get nodes
                            sed -i "s/IMAGE_VERSION/${appVersion}/g" values.yaml
                            helm upgrade --install ${COMPONENT} -f values-${deploy_to} -n ${PROJECT} --atomic --wait --timeout=5m .
                            
                            """
                            // if first time it will take install , otherwise it will upgrade
                            // --atomic : it will roll back if there is any failure
                            // --wait : it will wait until the deployment is successful or failed
                            // --timeout : it will wait for the specified time before it considers the deployment as failed
                        
                        }
                    
                }
            }
        }
        stage('Functional Testing'){
            when {
                epxression {
                    deploy_to == "dev"
                }
            }
            steps{
                script{
                    sh """
                     echo "Functional Test case's"
                    """
                }
            }
        }
        
       
         
            
       
        
    }
    post{
        always{
            echo 'I will always say Hello again!'
            cleanWs()
        }
        success {
            echo 'I will run if success'
        }
        failure {
            echo 'I will run if failure'
        }
        aborted {
            echo 'pipeline is aborted'
        }
    }
}


}

// rebuild plugin in jenkins , it use same parameter in pipeline , like prev... paramater