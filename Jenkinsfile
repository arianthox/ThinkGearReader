/* import shared library */
@Library('jenkins-shared-library')_

pipeline {
  environment {
      registry = "brainwaves/thinkgear-reader"
      registryCredential = 'dockerhub'
      dockerImage = ''
      appName="ThinkGearReader"
  }
  agent any
    stages {

      stage('Checkout') {
                  steps {
                      dir("Commons") {
                          git branch: 'development',url: 'git@github.corp.globant.com:BrainWaves/Commons.git'
                      }
                      dir(appName){
                          checkout scm
                      }
                  }
      }

      stage('build_Project'){
         steps{
              dir(appName){
                  sh './gradlew clean build'
              }
         }
      }

      stage("Clean Workspace"){
          steps{
           step([$class: 'WsCleanup'])
          }
      }

    }
    post {
          always {
             slackNotificator(currentBuild.currentResult)
          }
    }
}