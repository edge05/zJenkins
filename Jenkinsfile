pipeline {
    agent { node { label 'zOS' } }

    options {
        timestamps()
    }
    
    environment {
		polydir 	= '/opt/lpp/polycephaly/bin'
		ddbDir  	= '/opt/lpp/IBM/dbb/lib'
		srcJavaDir 	= 'src/main/java'
		binDir		= fileExists 'bin'
		distDir		= fileExists 'classes'
		
    }

    stages {
        stage('if directory dist exists'){
            when { expression { binDir == 'false' } }
            steps {
                echo "directory dist does not exist"
                sh 'mkdir bin'
            }
        }
        stage('if directory classes exists'){
            when { expression { distDir == 'false' } }
            steps {
                echo "directory classes does not exist"
                sh 'mkdir classes'
            }
        }
        
	    stage ('Start') {
	      steps {
	        // send to email
	        emailext (
	            subject: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
	            body: """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	              <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
	            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	          )
	        }
	    }
    	stage("CheckOut")  {
    		options {
    			 timeout(time: 1, unit: "MINUTES")
    		}
    		steps {
    			checkout([$class: 'GitSCM', branches: [[name: '*/edge05/branch01']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'edge05', url: 'https://github.com/openmainframeproject/polycephaly.git']]])
    		}	 
		}
		
		stage('Java Hello and version') {
            steps {
                echo 'Hello, JDK'
                sh '/usr/lpp/java/J8.0_64/bin/java -version'
            }
        }
        stage('Build zOSFile') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/javac -d bin src/main/java/polycephalyzOSFile/*.java'
            }
        }
        stage('Build zOSUtilities') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/javac -d bin src/main/java/polycephalyzOSUtilities/*.java'
            }
        }
        stage('Build zOSResBuild') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/javac -cp .:usr/lpp/java/J8.0_64/lib/*.jar:usr/lpp/java/J8.0_64/lib/ext/*.jar:/opt/lpp/IBM/dbb/lib/*.jar -d bin src/main/zOS/polycephalyzOSResBuild/*.java' 
            }
        }
        
        stage('Build CICS Utilities') {
            steps {
                sh '/opt/lpp/IBM/dbb/groovy-2.4.12/bin/groovyc -d bin src/main/groovy/polycephalyCICSUtilities/*.groovy' 
            }
        }
        stage('Build Languages') {
            steps {
                sh '/opt/lpp/IBM/dbb/groovy-2.4.12/bin/groovyc -d bin src/main/groovy/polycephalyLanguages/*.groovy'
            }
        }
        stage('Build zOS Groovy Utilities') {
            steps {
                sh '/opt/lpp/IBM/dbb/groovy-2.4.12/bin/groovyc -d bin src/main/groovy/polycephalyzOSGroovyUtilities/*.groovy'   
            }
        }
        stage('Build zOSResBuild') {
            steps {
                sh '/usr/lpp/java/J8.0_64/bin/javac -cp .:usr/lpp/java/J8.0_64/lib/*.jar:usr/lpp/java/J8.0_64/lib/ext/*.jar:/opt/lpp/IBM/dbb/lib/*.jar -d bin src/main/zOS/polycephalyzOSResBuild/*.java' 
            }
        }

        stage("Test") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
                sh 'printf "\\Some tests execution here...\\e[0m\\n"'
            }
        }
        stage("Deploy") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
                sh 'printf "\\e[31m Deploy package...\\e[0m\\n"'
            }
        }
    }
	post {
	    success {

	
	      emailext (
	          subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
	          body: """<p>SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	            <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
	          recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	        )
	    }
	
	    failure {

	      emailext (
	          subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
	          body: """<p>FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
	            <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>""",
	          recipientProviders: [[$class: 'DevelopersRecipientProvider']]
	        )
	    }
    }
}
  