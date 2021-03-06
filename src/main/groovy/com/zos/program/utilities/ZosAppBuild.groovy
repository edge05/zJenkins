package com.zos.groovy.utilities

import com.ibm.dbb.build.*
import com.ibm.dbb.build.report.*
import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import groovy.time.*
import groovy.lang.GroovyClassLoader
import groovy.lang.Script
import com.zos.language.*
import com.zos.cics.groovy.utilities.*

/**
* @author gedgingt
* @version v4.0.0
* Date 12/24/2018
*
* SPDX-License-Identifier: Apache-2.0
*/

class ZosAppBuild {
	/**
	 *
	 * <br />
	 * Main zOS application build routine
	 * 	Loads property files
	 * 	Prints out project build information
	 * 	Calls various routines to build/compile source
	 * 	Assmebler, BMSProcessing, CicsApiBuild, CicsWsBuild
	 * 	CobolCompile, Compile, Copybook, DualCompile
	 * 	Easytrieve, JCLCheck, Linkedit, MFSGenUtility, SDFGenUtility
	 *
	 * 	Usage: Called from build.grooy in zJenkins Project
	 *  	GroovyObject zBuild = (GroovyObject) ZosAppCopy.newInstance()
	 *  	def build = zBuild.execute()
	 *
	 * @param args
	 * @deprecated
	 * @since version 1.00
	 */

	static main(args) {

	}
	public void execute(String[] executeArgs, String usage) {

		GroovyObject tools = (GroovyObject) Tools.newInstance()

		def opts = tools.parseArgs(executeArgs, usage)
		if (properties.debug) println("opts = $opts")
		def properties = tools.loadProperties(opts)

		def startTime = new Date()
		println()
		println("*****************************************************************************************************")
		println("                    Project build started at $startTime ")
		println("*****************************************************************************************************")

		if (!properties.userBuild)
			tools.validateRequiredProperties(["dbb.RepositoryClient.url", "dbb.RepositoryClient.userId", "password", "collection"])

		if (properties.debug) println("******************* system properties loaded *********************************************************")
		if (properties.debug) {
			println(properties.list())
			def env = System.getenv()
				env.each{
				println it
			}
		}
		if (properties.debug) println("******************************************************************************************************")
		tools.validateRequiredProperties(["BuildList"])

		properties.startTime = startTime.format("yyyyMMdd.hhmmss.mmm")
		//println("** Build start at $properties.startTime")

		// initialize build artifacts
		tools.initializeBuildArtifacts()

		// create workdir (if necessary)
		new File(properties.workDir).mkdirs()
		if (properties.debug) println("** Build output will be in $properties.workDir")

		// create build list from input build file
		def buildList = tools.getBuildList(opts.arguments())


		// scan all the files in the process list for dependency data (team build only)
		if (!properties.userBuild && buildList.size() > 0) {
			// create collection if needed
			def repositoryClient = tools.getDefaultRepositoryClient()
			if (!repositoryClient.collectionExists(properties.collection))
				repositoryClient.createCollection(properties.collection)

			println("** Scan the build list to collect dependency data")
			def scanner = new DependencyScanner()
			def logicalFiles = [] as List<LogicalFile>
			if (properties.debug) println("logicalFiles = $logicalFiles")
			if (properties.debug) println("buildList = $buildList")

			buildList.each { file ->
				def scanFile = "${properties.'src.zOS.dir'}/$file"
				if (properties.debug) println("Scanning $scanFile for ${properties.'src.zOS.dir'}/$file")
				def logicalFile = scanner.scan(scanFile, properties.workDir)
				if (properties.debug) println("logicalFile = $logicalFile")
				logicalFiles.add(logicalFile)

				if (logicalFiles.size() == 500) {
					if (properties.debug) println("** Storing ${logicalFiles.size()} logical files in repository collection '$properties.collection'")
					 repositoryClient.saveLogicalFiles(properties.collection, logicalFiles);
					if (properties.debug) println(repositoryClient.getLastStatus())
					logicalFiles.clear()
				}
			}

			if (properties.debug) println("** Storing remaining ${logicalFiles.size()} logical files in repository collection '$properties.collection'")
			repositoryClient.saveLogicalFiles(properties.collection, logicalFiles);
			println(repositoryClient.getLastStatus())
		}
		def totalNumLines = 0
		def processCounter = 0
		if (properties.debug) println("buildList = $buildList")
		if (buildList.size() == 0)
			println("** No files in build list.  Nothing to build.")
		else {
			// build programs by invoking the appropriate build script
			def buildOrder = Eval.me(properties.buildOrder)
			if (properties.debug) println("buildOrder = $buildOrder")
			// optionally execute IMS MFS builds
			if (properties.BUILD_MFS.toBoolean())
				buildOrder << "MFSGenUtility"

			def asm = new Assembler()
			def bms = new BMSProcessing()
			def cicsApi = new CicsApiBuild()
			def cicsWs = new CicsWsBuild()
			def cCompile = new CobolCompile()
			def compile = new Compile()
			def copybook = new Copybook()
			def dualCompile = new DualCompiles()
			def ezt = new Easytrieve()
			def jcl = new JCLcheck()
			def lnkEdit = new LinkEdit()
			def mfs = new MFSGenUtility()
			def sdf = new SDFGenUtility()
			def submitJcl = new SubmitJCL()
			def lines = null
			def numLines = 0
			def buildFile
			def tempFile

			if (properties.debug) println("** Invoking build scripts according to build order: ${buildOrder[1..-1].join(', ')}")
			buildOrder.each { script ->
		    	// Use the ScriptMappings class to get the files mapped to the build script
				def buildFiles = ScriptMappings.getMappedList(script, buildList)
				if (properties.debug) println("buildList = $buildList, buildFiles = $buildFiles, script = $script")
				buildFiles.each { file ->
					if (properties.debug) println("---- file = ${properties.'src.zOS.dir'}/$file ----")
					buildFile = "${properties.'src.zOS.dir'}/$file"
					numLines = 0
					tempFile = new File(buildFile)
					lines = tempFile.readLines()
					numLines = lines.size()
					totalNumLines = totalNumLines + numLines

					switch (script) {
						case "Assembler":
							asm.run([buildFile] as String[])
							break
						case "BMSProcessing":
							bms.run([buildFile] as String[])
							break
						case "CicsApiBuild":
							cicsApi.run([buildFile] as String[])
							break
						case "CicsWsBuild":
							cicsWs.run([buildFile] as String[])
							break
						case "CobolCompile":
							cCompile.run([buildFile] as String[])
							break
						case "Compile":
							compile.run([buildFile] as String[])
							break
						case "Copybook":
							copybook.run([buildFile] as String[])
							break
						case "DualCompile":
							dualCompile.run([buildFile] as String[])
							break
						case "Eztrieve":
							ezt.run([buildFile] as String[])
							break
						case "JCLcheck":
							jcl.run([buildFile] as String[])
							break
						case "LinkEdit":
							lnkEdit.run([buildFile] as String[])
							break
						case "MFSGenUtility":
							mfs.run([buildFile] as String[])
							break
						case "SubmitJCL":
							submitJcl.run([buildFile] as String[])
							break
					}
					if (properties.debug) println("**** Finished running for file $file ****")
					processCounter++
				}
			}
		}

		println("total number of lines = $totalNumLines")
		// generate build report
		def (File jsonFile, File htmlFile) = tools.generateBuildReport()

		// finalize build result
		tools.finalizeBuildResult(jsonReport:jsonFile, htmlReport:htmlFile, filesProcessed:processCounter)

		def endTime = new Date()
		def duration = TimeCategory.minus(endTime, startTime)

		// Print end build message
		def state = (properties.error) ? "ERROR" : "CLEAN"

		println()
		println("*****************************************************************************************************")
		println("                    Project build finished at $endTime  ")
		println("                    Build State : $state ")
		println("                    Total files processed : $processCounter")
		println("                    Project total build time  : $duration  ")
		println("*****************************************************************************************************")

		// if error signal process error for Jenkins to record failed build
		if (properties.error)
		   System.exit(1)
	}
}
