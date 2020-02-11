stage 'build_Project'
node{

  stage 'checkout'

  checkout scm

  stage 'build'

  if(isUnix()){
    sh './gradlew clean build'
  } else {
    bat 'gradlew.bat clean build'
  }
}