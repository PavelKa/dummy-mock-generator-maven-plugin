import org.example.it.mocks.EchoMock
import spock.lang.Specification

class MyLogSpec extends Specification {
    //* Test for myCommon class which represent Jenkins global variable
    def "example test which mocks echo and test if echo was called within global variable method "() {
        given:
        def myEchoMock = GroovySpy(EchoMock)
        def myLogClass = Class.forName('myLog')
        def logToTest = myLogClass.constructors[0].newInstance()
        logToTest.metaClass.echo = myEchoMock.&call
        when:
        logToTest.info('Log message')
        then:
        1 * myEchoMock.call('INFO: Log message')
        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(myLogClass)
    }

}