package cz.kahle.jenkins

import org.apache.maven.model.Dependency
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.project.MavenProject

class GenerateGDSLMojoTest extends spock.lang.Specification {
    def "ParseGroovyFile no params"() {
        given:
        def mojo = new GenerateGDSLMojo()
        MavenProject project = new MavenProject()
        project.build.directory = 'target'
        mojo.project = project
        mojo.outputDirectory = new File('target/test/generated')
        mojo.outputDirectory.mkdirs()
        mojo.sourceDirectory = new File('src/test/resources/vars')

        when:
        def methods = mojo.parseGroovyFile(new File("src/test/resources/vars/LogNoParams.groovy"))
        then:
        methods == [[name: 'info', params: []]]
    }


    def "ParseGroovyFile default value"() {
        given:
        def mojo = new GenerateGDSLMojo()
        MavenProject project = new MavenProject()
        mojo.project = project
        mojo.project.build.directory = 'target'
        mojo.outputDirectory = new File('target/test/generated')
        mojo.outputDirectory.mkdirs()
        mojo.sourceDirectory = new File('src/test/resources/vars')
        when:
        def methods = mojo.parseGroovyFile(new File("src/test/resources/vars/LogDefaultValues.groovy"))
        then:
        methods == [[name:'info', params:[[name:'message', type: 'java.lang.Object']]], [name:'info', params:[]]]
    }

    def "ParseGroovyFile"() {
        given:
        def mojo = new GenerateGDSLMojo()
        MavenProject project = new MavenProject()
        mojo.project = project
        mojo.project.build.directory = 'target'
        mojo.outputDirectory = new File('target/test/generated')
        mojo.outputDirectory.mkdirs()
        mojo.sourceDirectory = new File('src/test/resources/vars')
        when:
        def methods = mojo.parseGroovyFile(new File("src/test/resources/vars/Log.groovy"))
        then:
        methods == [[name: 'info', params: [[name: 'message', type: 'java.lang.String']]], [name: 'info', params: [[name: 'message', type: 'java.lang.String'], [name: 'context', type: 'java.lang.Object']]],[name: 'info', params: []]]
    }

    def getGdslFileContent() {
        given:
        def mojo = new GenerateGDSLMojo()
        when:
        def gdslFileContent = mojo.getGdslFileContent([[name: 'info', params: [[name: 'message', type: 'java.lang.String']]], [name: 'info', params: [[name: 'message', type: 'java.lang.String'], [name: 'context', type: 'java.lang.String']]]], "Log")

        then:
        gdslFileContent == """
def ctxLog = context(ctype: 'Log')
contributor(ctxLog) {
  method(name: 'info', type: 'Object', params: [message:'java.lang.String'], doc: 'Generated method from vars directory')
  method(name: 'info', type: 'Object', params: [message:'java.lang.String', context:'java.lang.String'], doc: 'Generated method from vars directory')
}
""".stripLeading().stripIndent().stripMargin()

    }


    def "execute read source groovy files  and generate gdsl"() {
        given:
        def mojo = new GenerateGDSLMojo()
        MavenProject project = new MavenProject()
        mojo.project = project
        mojo.project.build.directory = 'target'
        mojo.outputDirectory = new File('target/test/generated')
        mojo.outputDirectory.mkdirs()
        mojo.sourceDirectory = new File('src/test/resources/vars')

        when:
        mojo.execute()

        then:
          true

        when:
        mojo.outputDirectory = new File('non/existent/output')
        mojo.execute()

        then:
        thrown(MojoExecutionException)

        when:
        mojo.outputDirectory = new File('target/generated-test-sources')
        mojo.sourceDirectory = new File('non/existent/source')
        mojo.execute()

        then:
        thrown(MojoExecutionException)
    }
}
