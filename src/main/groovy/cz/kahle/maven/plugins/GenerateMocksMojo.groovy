package cz.kahle.maven.plugins

import groovy.io.FileType
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.lang.reflect.Modifier
import java.nio.file.Path

@Mojo(name = "generate-mocks", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
class GenerateMocksMojo extends AbstractMojo {
    @Parameter(defaultValue = '${project}', readonly = true, required = true)
    private MavenProject project
    /**
     * Destination directory for generated Mocks sources
     */
    @Parameter(defaultValue = '${project.build.directory}/generated-test-sources', property = 'outputDir', required = true)
    private File outputDirectory
    @Parameter(defaultValue = '${project.groupId}.mocks', property = 'mockPackage', required = false)
    private String mockPackage

    void execute() throws MojoExecutionException {

        def gdslFiles = findFilesWithExtensions(['gdsl'])
        if (gdslFiles.isEmpty()) {
            getLog().warn('No GDSL files found')
            return
        }
        if (!outputDirectory.exists()) {
            getLog().info("Creating output directory: ${outputDirectory}")
            outputDirectory.mkdirs()
        }

        gdslFiles.each { gdslFile ->
            getLog().info("Processing file: ${gdslFile.getAbsolutePath()}")
            def mockObjects = evaluateGDSL(gdslFile.text)
            def generatedMocks = generateMocks(mockObjects)
            storeMocksTofiles(outputDirectory,  generatedMocks)

        }

    }

    List<Path> findFilesWithExtensions(extensions) {
        List<Path> filesWithExtensions = []
        List<String> sourceRoots = project.getCompileSourceRoots()
        getLog().info("Source roots where gdsl files will be searched: \n ${sourceRoots.join('\n')} ")

        sourceRoots.each { sourceRoot ->
            File sourceDir = new File(sourceRoot)
            if (sourceDir.exists()) {
                sourceDir.eachFile { file ->
                    if (extensions.any { file.name.endsWith(it) }) {
                        filesWithExtensions.add(file)
                    }
                }
            } else {
                getLog().warn("Source directory does not exist: $sourceDir")
            }
        }
        return filesWithExtensions
    }

    private generateMocks(mockObjects) {
        def generated = [:]
        def mocks = [:]
        def groupedByCType = mockObjects.findAll { true }.groupBy { it.cType }
        getLog().debug("groupedByCType: ${groupedByCType.inspect()}")
        groupedByCType.each { classType, methods ->
            getLog().debug("Generating mock for ctype: $classType methods: ${methods.inspect()}")
            def className = classType.capitalize() + "Mock"
            if (generated[className]) {
                generated[className] = generated[className] + 1
                className = className + generated[className]
            } else {
                generated[className] = 1
            }
            def mockObject = "package $mockPackage\nclass $className {"
            def methodSignatures = []
            methods.each { method ->
                def argsString = new StringWriter()
                def echoString = new StringWriter()
                def methodSignature = new StringWriter()
                methodSignature << method.name
                method.findAll { it.key == 'params' || it.key == 'namedParams' }.entrySet().each { it ->
                    if (!argsString.toString().isEmpty()) {
                        argsString << ", "
                    }
                    if (!echoString.toString().isEmpty()) {
                        echoString << ", "
                    }
                    if (!methodSignature.toString().isEmpty()) {
                        methodSignature << ", "
                    }
                    if (it.key == 'params') {
                        if (!it.value.isEmpty()) {
                            argsString << (it.value.entrySet().collect {
                                if (it.value instanceof Class) {
                                    "${it.value.simpleName} ${it.key}"
                                } else {
                                    "$it.value $it.key"
                                }
                            }.join(', ')
                            )

                            echoString << (it.value.keySet().collect { "$it: \$$it" }.join(', '))
                            methodSignature << it.value.entrySet().collect {
                                def type = it.value
                                if (it.value instanceof Class) {
                                    type = "${it.value.simpleName}"
                                }
                                if (type == 'Map') {
                                    type = 'java.util.Map'
                                }
                                type
                            }.join(', ')
                        }
                    } else {

                        argsString << "java.util.Map namedParams"
                        methodSignature << "java.util.Map"
                        echoString << "namedParams: \${namedParams.inspect()}"

                    }
                }
                def methodName = method.isObjectMethod ? method.name : 'call'

                if (!methodSignatures.contains(methodSignature.toString())) {
                    methodSignatures.add(methodSignature.toString())
                    mockObject = mockObject << "  def $methodName(${argsString} ) { println \"Mock for ${classType}.$methodName called with parameters:  ($echoString)\" }\n"
                } else {
                    getLog().warn("Method $methodName with signature $methodSignature already exists in $className")
                }
            }

            mockObject = mockObject + "}\n"
            mockObject.stripIndent()

            mocks.put("${className}.groovy".toString(), mockObject)

        }
        mocks
    }

    private evaluateGDSL(gdslFileText) {
        def mockObjects = []
        def cType = null
        GroovyShell shell = new GroovyShell()
        def shellMetaClass = Object.metaClass

        shellMetaClass.scriptScope = { -> getLog().debug('scriptScope called') }
        shellMetaClass.context = { scope ->
            getLog().debug("context called scope:$scope")
            cType = scope.ctype
        }
        shellMetaClass.contributor = { ctx, closure -> getLog().debug("contributor called ctx:$ctx closure:$closure") }
        shellMetaClass.method = { map ->
            getLog().debug("method called map:$map")
            map.cType = cType ? cType : map.name
            map.isObjectMethod = cType != null
            map = replaceClassObjectsWithClassNames(map)
            mockObjects.add(map)
            return
        }
        shellMetaClass.parameter = { map ->
            getLog().debug("parameter called map:$map")
            map
        }
        shellMetaClass.property = { map -> getLog().debug("property called map:$map"); map }
        shellMetaClass.closureScope = { scope -> getLog().debug("closureScope called scope:$scope") }
        shellMetaClass.enclosingCall = { enclosingCall -> getLog().debug("enclosingCall called enclosingCall:$enclosingCall") }
        shellMetaClass.scriptScope = { -> 'scriptScope' }
        shell.evaluate(gdslFileText)
        mockObjects
    }

    def replaceClassObjectsWithClassNames(Map map) {
        map.collectEntries { key, value ->
            if (value instanceof Class) {
                [key, value.name]
            } else if (value instanceof Map) {
                [key, replaceClassObjectsWithClassNames(value)]
            } else {
                [key, value]
            }
        }
    }

    void storeMocksTofiles(File outputDirectory, Map<String, String> mocksClasses) {
        def packagePath = mockPackage.replace('.', File.separator)
        def mockDir = new File(outputDirectory, packagePath)
        mockDir.mkdirs()
        mocksClasses.each { className, mockObject ->
            def mockFile = new File(mockDir, className)
            mockFile.text = mockObject
        }
    }

}



