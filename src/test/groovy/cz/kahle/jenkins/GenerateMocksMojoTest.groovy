package cz.kahle.jenkins

import org.apache.maven.project.MavenProject
import spock.lang.Specification
import org.apache.maven.plugin.MojoExecutionException

class GenerateMocksMojoTest extends Specification {
    def "generateMocks should generate mock classes for methods in GDSL file"() {
        given:
        def mojo = new GenerateMocksMojo()
        mojo.mockPackage= 'xy'

        def gdslText = '''def ctx = context(scope: scriptScope())
contributor(ctx) {
    method(name: 'parallel', type: 'Object', params: ['closures': 'java.util.Map'], doc: 'Execute in parallel')
    method(name: 'parallel', type: 'Object', namedParams: [parameter(name: 'closures', type: 'java.util.Map'), parameter(name: 'failFast', type: 'boolean'),], doc: 'Execute in parallel')
    method(name: 'withAWS', type: 'Object', params: [body: 'Closure'], doc: 'set AWS settings for nested block')
    method(name: 'withAWS', type: 'Object', params: [body: Closure], namedParams: [parameter(name: 'credentials', type: 'java.lang.String'), parameter(name: 'duration', type: 'java.lang.Integer'), parameter(name: 'endpointUrl', type: 'java.lang.String'), parameter(name: 'externalId', type: 'java.lang.String'), parameter(name: 'federatedUserId', type: 'java.lang.String'), parameter(name: 'iamMfaToken', type: 'java.lang.String'), parameter(name: 'policy', type: 'java.lang.String'), parameter(name: 'principalArn', type: 'java.lang.String'), parameter(name: 'profile', type: 'java.lang.String'), parameter(name: 'region', type: 'java.lang.String'), parameter(name: 'role', type: 'java.lang.String'), parameter(name: 'roleAccount', type: 'java.lang.String'), parameter(name: 'roleSessionName', type: 'java.lang.String'), parameter(name: 'samlAssertion', type: 'java.lang.String'), parameter(name: 'useNode', type: 'boolean'),], doc: 'set AWS settings for nested block')
    method(name: 'test', type: 'Object',namedParams: [parameter(name: 'first', type: 'java.lang.String')], params: [body: Closure],  doc: 'set AWS settings for nested block')

}'''
        when:
        def mockObjects = mojo.evaluateGDSL(gdslText)
        then:
        mockObjects == [['name': 'parallel', 'type': 'Object', 'params': ['closures': 'java.util.Map'], 'doc': 'Execute in parallel', 'cType': 'parallel', 'isObjectMethod': false],
                        ['name': 'parallel', 'type': 'Object', 'namedParams': [['name': 'closures', 'type': 'java.util.Map'], ['name': 'failFast', 'type': 'boolean']], 'doc': 'Execute in parallel', 'cType': 'parallel', 'isObjectMethod': false],
                        ['name': 'withAWS', 'type': 'Object', 'params': ['body': 'Closure'], 'doc': 'set AWS settings for nested block', 'cType': 'withAWS', 'isObjectMethod': false],
                        ['name': 'withAWS', 'type': 'Object', 'params': ['body': 'groovy.lang.Closure'], 'namedParams': [['name': 'credentials', 'type': 'java.lang.String'], ['name': 'duration', 'type': 'java.lang.Integer'], ['name': 'endpointUrl', 'type': 'java.lang.String'], ['name': 'externalId', 'type': 'java.lang.String'], ['name': 'federatedUserId', 'type': 'java.lang.String'], ['name': 'iamMfaToken', 'type': 'java.lang.String'], ['name': 'policy', 'type': 'java.lang.String'], ['name': 'principalArn', 'type': 'java.lang.String'], ['name': 'profile', 'type': 'java.lang.String'], ['name': 'region', 'type': 'java.lang.String'], ['name': 'role', 'type': 'java.lang.String'], ['name': 'roleAccount', 'type': 'java.lang.String'], ['name': 'roleSessionName', 'type': 'java.lang.String'], ['name': 'samlAssertion', 'type': 'java.lang.String'], ['name': 'useNode', 'type': 'boolean']], 'doc': 'set AWS settings for nested block', 'cType': 'withAWS', 'isObjectMethod': false], ['name': 'test', 'type': 'Object', 'namedParams': [['name': 'first', 'type': 'java.lang.String']], 'params': ['body': 'groovy.lang.Closure'], 'doc': 'set AWS settings for nested block', 'cType': 'test', 'isObjectMethod': false]]
        when:
        def mockMethods = mojo.generateMocks(mockObjects)
        then:
println("mockMethods.inspect(): ${mockMethods.inspect()}")
        mockMethods ==  ['ParallelMock.groovy':'package xy\nclass ParallelMock {  def call(java.util.Map closures ) { println "Mock for parallel.call called with parameters:  (closures: $closures)" }\n}\n',
                         'WithAWSMock.groovy':'package xy\nclass WithAWSMock {  def call(Closure body ) { println "Mock for withAWS.call called with parameters:  (body: $body)" }\n  def call(groovy.lang.Closure body, java.util.Map namedParams ) { println "Mock for withAWS.call called with parameters:  (body: $body, namedParams: ${namedParams.inspect()})" }\n}\n',
                         'TestMock.groovy':'package xy\nclass TestMock {  def call(java.util.Map namedParams, groovy.lang.Closure body ) { println "Mock for test.call called with parameters:  (namedParams: ${namedParams.inspect()}, body: $body)" }\n}\n']
    }

    def "generateMocks should generate mock all classes for log gdsl file"() {
        given:
        def mojo = new GenerateMocksMojo()
        mojo.mockPackage= 'xy'
        def gdslText = '''def ctxrbrLog = context(ctype: 'rbrLog')
contributor(ctxrbrLog) {
  method(name: 'info', type: 'Object', params: [params:'java.lang.Object'], doc: 'Generated method from vars directory')
  method(name: 'logCommon', type: 'Object', params: [params:'java.lang.Object'], doc: 'Generated method from vars directory')
  method(name: 'info', type: 'Object', params: [params:'java.lang.Object', message:'java.lang.Object'], doc: 'Generated method from vars directory')
  method(name: 'warn', type: 'Object', params: [params:'java.lang.Object', message:'java.lang.Object'], doc: 'Generated method from vars directory')
  method(name: 'debug', type: 'Object', params: [params:'java.lang.Object', message:'java.lang.Object'], doc: 'Generated method from vars directory')
  method(name: 'error', type: 'Object', params: [params:'java.lang.Object', e:'java.lang.Object'], doc: 'Generated method from vars directory')
  method(name: 'error', type: 'Object', params: [params:'java.lang.Object', message:'java.lang.Object', e:'java.lang.Object'], doc: 'Generated method from vars directory')
}
'''
        when:
        def mockObjects = mojo.evaluateGDSL(gdslText)
        then:

        mockObjects == [['name': 'info', 'type': 'Object', 'params': ['params': 'java.lang.Object'], 'doc': 'Generated method from vars directory', 'cType': 'rbrLog', 'isObjectMethod': true],
                        ['name': 'logCommon', 'type': 'Object', 'params': ['params': 'java.lang.Object'], 'doc': 'Generated method from vars directory', 'cType': 'rbrLog', 'isObjectMethod': true],
                        ['name': 'info', 'type': 'Object', 'params': ['params': 'java.lang.Object', 'message': 'java.lang.Object'], 'doc': 'Generated method from vars directory', 'cType': 'rbrLog', 'isObjectMethod': true],
                        ['name': 'warn', 'type': 'Object', 'params': ['params': 'java.lang.Object', 'message': 'java.lang.Object'], 'doc': 'Generated method from vars directory', 'cType': 'rbrLog', 'isObjectMethod': true],
                        ['name': 'debug', 'type': 'Object', 'params': ['params': 'java.lang.Object', 'message': 'java.lang.Object'], 'doc': 'Generated method from vars directory', 'cType': 'rbrLog', 'isObjectMethod': true],
                        ['name': 'error', 'type': 'Object', 'params': ['params': 'java.lang.Object', 'e': 'java.lang.Object'], 'doc': 'Generated method from vars directory', 'cType': 'rbrLog', 'isObjectMethod': true],
                        ['name': 'error', 'type': 'Object', 'params': ['params': 'java.lang.Object', 'message': 'java.lang.Object', 'e': 'java.lang.Object'], 'doc': 'Generated method from vars directory', 'cType': 'rbrLog', 'isObjectMethod': true]]
        when:
        def mockMethods = mojo.generateMocks(mockObjects)
        then:
        println("mockMethods.inspect(): ${mockMethods.inspect()}")
        mockMethods == ['RbrLogMock.groovy':'package xy\nclass RbrLogMock {  def info(java.lang.Object params ) { println "Mock for rbrLog.info called with parameters:  (params: $params)" }\n  def logCommon(java.lang.Object params ) { println "Mock for rbrLog.logCommon called with parameters:  (params: $params)" }\n  def info(java.lang.Object params, java.lang.Object message ) { println "Mock for rbrLog.info called with parameters:  (params: $params, message: $message)" }\n  def warn(java.lang.Object params, java.lang.Object message ) { println "Mock for rbrLog.warn called with parameters:  (params: $params, message: $message)" }\n  def debug(java.lang.Object params, java.lang.Object message ) { println "Mock for rbrLog.debug called with parameters:  (params: $params, message: $message)" }\n  def error(java.lang.Object params, java.lang.Object e ) { println "Mock for rbrLog.error called with parameters:  (params: $params, e: $e)" }\n  def error(java.lang.Object params, java.lang.Object message, java.lang.Object e ) { println "Mock for rbrLog.error called with parameters:  (params: $params, message: $message, e: $e)" }\n}\n']

    }

    def "generateMocks should generate mock all classes for jiraGetIssueTransitions"() {
        given:
        def mojo = new GenerateMocksMojo()
        mojo.mockPackage= 'xy'
        def gdslText = '''def ctx = context(scope: scriptScope())
     contributor(ctx) {
     method(name: 'jiraGetIssueTransitions', type: 'Object', params: [idOrKey: 'java.lang.String'], doc: 'JIRA Steps: Get Issue Transitions')
     method(name: 'jiraGetIssueTransitions', type: 'Object', namedParams: [parameter(name: 'idOrKey', type: 'java.lang.String'), parameter(name: 'auditLog', type: 'boolean'), parameter(name: 'failOnError', type: 'boolean'), parameter(name: 'queryParams', type: 'java.util.Map'), parameter(name: 'site', type: 'java.lang.String'),], doc: 'JIRA Steps: Get Issue Transitions')
}
'''
        when:
        def mockObjects = mojo.evaluateGDSL(gdslText)
        then:

        mockObjects == [['name':'jiraGetIssueTransitions', 'type':'Object', 'params':['idOrKey':'java.lang.String'], 'doc':'JIRA Steps: Get Issue Transitions', 'cType':'jiraGetIssueTransitions', 'isObjectMethod':false],
                        ['name':'jiraGetIssueTransitions', 'type':'Object', 'namedParams':[['name':'idOrKey', 'type':'java.lang.String'], ['name':'auditLog', 'type':'boolean'], ['name':'failOnError', 'type':'boolean'], ['name':'queryParams', 'type':'java.util.Map'], ['name':'site', 'type':'java.lang.String']], 'doc':'JIRA Steps: Get Issue Transitions', 'cType':'jiraGetIssueTransitions', 'isObjectMethod':false]]
        when:
        def mockMethods = mojo.generateMocks(mockObjects)
        then:
        println("mockMethods.inspect(): ${mockMethods.inspect()}")
         mockMethods ==  ['JiraGetIssueTransitionsMock.groovy':'package xy\nclass JiraGetIssueTransitionsMock {  def call(java.lang.String idOrKey ) { println "Mock for jiraGetIssueTransitions.call called with parameters:  (idOrKey: $idOrKey)" }\n  def call(java.util.Map namedParams ) { println "Mock for jiraGetIssueTransitions.call called with parameters:  (namedParams: ${namedParams.inspect()})" }\n}\n']


    }
}