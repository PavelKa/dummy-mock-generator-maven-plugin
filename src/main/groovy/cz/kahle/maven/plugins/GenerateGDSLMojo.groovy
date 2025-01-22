package cz.kahle.maven.plugins

import groovy.io.FileType
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration

@Mojo(name = "generate-gdsl", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES)
class GenerateGDSLMojo extends AbstractMojo {
    @Parameter(defaultValue = '${project}', readonly = true, required = true)
    private MavenProject project
    /**
     * Location of the directory where the generated GDSL file will be stored.
     */
    @Parameter(defaultValue = '${project.build.directory}/generated-sources', property = 'outputDir', required = true)
    private File outputDirectory
    /**
     * Location od a directory where groovy from which GDSL will be generated are stored.
     */
    @Parameter(defaultValue = '${project.basedir}/vars', property = 'sourceDir', required = true)
    private File sourceDirectory

    void execute() throws MojoExecutionException {
        getLog().info("Generating GDSL file to ${outputDirectory} from ${sourceDirectory}")
        if (!outputDirectory.exists()) {
            throw new MojoExecutionException("Output directory does not exist: ${outputDirectory}")
        }
        if (!sourceDirectory.exists()) {
            throw new MojoExecutionException("SourceDirectory directory does not exist: ${sourceDirectory}")
        }

        generateGDSL()

    }

    def parseGroovyFile(File file) {
        getLog().debug("Project compile classpath:\n" + project.getCompileClasspathElements())

        def methods = []
        def config = new CompilerConfiguration()
        config.targetDirectory = new File(project.build.directory, 'ast')
        GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader, config)
        project.getCompileClasspathElements().each { classLoader.addClasspath(it) }

        CompilationUnit cu = new CompilationUnit(config, null, classLoader)
        cu.addSource(file)

        cu.compile()

        cu.getAST().getModules().each { module ->
            module.getClasses().each { classNode ->
                classNode.methods.each { methodNode ->
                    if (methodNode.declaringClass == classNode && !['run', 'main', '$getStaticMetaClass', '$getLookup'].contains(methodNode.name)) {
                        def params = methodNode.parameters.collect { param ->
                            [name: param.name, type: param.type.name]

                        }
                        methods << [name: methodNode.name, params: params]
                    }
                }
            }
        }
        return methods
    }

    private generateGDSL() {
        sourceDirectory.eachFileMatch(FileType.FILES, ~/.*\.groovy/) { file ->
            def fileMethods = parseGroovyFile(file)
            def gdslFileContent = getGdslFileContent(fileMethods, file.name - '.groovy')

            def gdslFile = new File(outputDirectory, "${file.name - '.groovy'}.gdsl")
            getLog().info("Generating GDSL file: ${gdslFile.absolutePath} with content: ${gdslFileContent}")
            gdslFile.text = gdslFileContent
        }
    }

    def getGdslFileContent(methods, fileName) {
        def gdslFile = new StringWriter()
        gdslFile.withWriter { writer ->
            writer.write "def ctx$fileName = context(ctype: '${fileName}')\n"
            writer.write "contributor(ctx$fileName) {\n"
            methods.each { method ->
                writer.write "  method(name: '${method.name}', type: 'Object', params: [${collectParams(method.params)}], doc: 'Generated method from vars directory')\n"

            }
            writer.write "}\n"
        }
        gdslFile.toString()
    }

    private collectParams(params) {
        def x = params.collect { map ->
            "$map.name:'$map.type'"
        }.join(', ')
        x
    }
}


