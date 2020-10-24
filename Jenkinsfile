pipeline {
    agent { node { label 'zOS' } }

    options {
        timestamps()
    }
    
    environment {	
    	binDir				= 'bin'
    	classesDir			= 'classes'	
		srcJavaZosFile		= 'src/main/java/com/jenkins/zos/file'
		srcJavaZosUtil		= 'src/main/java/com/zos/java/utilities'
		srcZosResbiuld		= 'src/main/zOS/com.zos.resbuild'
		srcGroovyZosLang	= 'src/main/groovy/com/zos/language'
		srcGrovoyZosUtil	= 'src/main/groovy/com/zos/groovy/utilities'
		srcGroovyCICSutil	= 'src/main/groovy/com/zos/cics/groovy/utilities'
		javaHome			= '/usr/lpp/java/J8.0_64/bin'
		groovyHome			= '/u/jerrye/jenkins/groovy/bin'
		ibmjzos				= '/usr/lpp/java/J8.0_64/lib/ext/ibmjzos.jar'
		dbbcore				= '/opt/lpp/IBM/dbb/lib/dbb.core_1.0.6.jar'
		polycephalyJar		= "${env.binDir}/polycephaly.jar"
		javaClassPath		= "${env.ibmjzos}:${env.dbbcore}"
		groovyClassPath		= "${env.javaClassPath}:${env.polycephalyJar}"
		polyRuntime			= '/u/jerrye'
		
    }

    stages {
        stage('Clean workspace') {
            steps {
                cleanWs()
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
    		steps {
    			checkout([$class: 'GitSCM', branches: [[name: '*/edge05/branch01']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'edge05', url: 'https://github.com/openmainframeproject/polycephaly.git']]])
    		}	 
		}
		stage('Create Directories'){
            steps {
                sh "mkdir ${env.binDir}"
                sh "mkdir ${env.classesDir}"
            }
        }
        stage('Build zOS utilities') {
            steps {
                sh "${env.javaHome}/javac -d ${env.classesDir} ${env.srcJavaZosFile}/*.java"
                sh "${env.javaHome}/javac -d ${env.classesDir} ${env.srcJavaZosUtil}/*.java"
                sh "${env.javaHome}/javac -cp .:${env.javaClassPath}  -d ${env.classesDir} ${env.srcZosResbiuld}/*.java"
            }
        }
        stage('Create Java Jar file') {
            steps {
                sh "${env.javaHome}/jar cvf ${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build Groovy Language Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcGroovyZosLang}/*.groovy"
            }
        }
        stage('Add Groovy Language Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build Groovy CICS Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcGroovyCICSutil}/*.groovy"
            }
        }
        stage('Add Groovy CICS Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.polycephalyJar} -C ${env.classesDir} . "
            }
        }
        stage('Build Groovy zOS Utilities') {
            steps {
                sh "${env.groovyHome}/groovyc-1047 -cp .:${env.groovyClassPath}  -d ${env.classesDir} ${env.srcGrovoyZosUtil}/*.groovy"
            }
        }
        stage('Add Groovy ZOS Utilities to JAR') {
            steps {
                sh "${env.javaHome}/jar uf ${env.polycephalyJar} -C ${env.classesDir} . "
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
                sh "cp -Rf ${WORKSPACE}/${env.polycephalyJar} ${env.polyRuntime}/bin/" 
                sh "cp -Rf ${WORKSPACE}/conf/*.properties ${env.polyRuntime}/conf/"
                sh "cp -Rf ${WORKSPACE}/conf/*.pw ${env.polyRuntime}/conf/"   
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
  