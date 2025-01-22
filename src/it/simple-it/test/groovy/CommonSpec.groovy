import spock.lang.Specification

class CommonSpec extends Specification {

    def "log should be called"() {
        given:
        // global vrable pouzivaji pro volání jinych global variables  nazev souboru, soubor je kompilovan jako trida se stejnym nazvem, potom se pri volani predpoklada, ze metoda je staticka,
        // proto je nutne dynamicky pridat statickou metodu do tridy a v ni pak pridat volai mockovane metody
        def rbrLogClass = Class.forName('rbrLog')
        def rbrLogMock = GroovyMock(RbrLogMock)
        rbrLogClass.metaClass.static.info = { rbrLogMock.info(it) }
        def commonToTest = new Common()
        when:
        commonToTest.simplePipeline()
        then:
        1 * rbrLogMock.info('Log from simplePipeline')
    }
}