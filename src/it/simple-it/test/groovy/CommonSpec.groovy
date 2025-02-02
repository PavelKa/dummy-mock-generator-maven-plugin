import org.example.it.mocks.MyLogMock
import spock.lang.Specification

class CommonSpec extends Specification {
    def "example test of vars/common using generated MyLogMock and verifies if method myLog.info and println in mock called were called"() {
        given:
        def myLogMock = GroovySpy(MyLogMock)
        def myLogClass = Class.forName('myLog')

        myLogClass.metaClass.static.info = myLogMock.&info
        def commonToTest = Class.forName('myCommon').getDeclaredConstructor().newInstance()
        when:
        commonToTest.simplePipeline()
        then:
        1 * myLogMock.info('Log from simplePipeline')
        1 * myLogMock.println('Mock for myLog.info called with parameters:  (message: Log from simplePipeline)')
        cleanup:
        GroovySystem.metaClassRegistry.removeMetaClass(myLogClass)
    }
}