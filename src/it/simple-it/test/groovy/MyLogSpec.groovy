import org.example.it.mocks.EchoMock
import spock.lang.Specification

class MyLogSpec extends Specification {
    //* Test for myCommon class which represent Jenkins global variable
    def "test that tests a global variable vars_myLog and validates if the echo method was called"() {
        given:
        def myEchoMock = GroovySpy(EchoMock)
        def myLogClass = Class.forName('myLog')
        def logToTest = myLogClass.getDeclaredConstructor().newInstance()
        logToTest.metaClass.echo = myEchoMock.&call
        when:
        logToTest.info('Log message')
        then:
        1 * myEchoMock.call('INFO: Log message')
        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(myLogClass)
    }

}