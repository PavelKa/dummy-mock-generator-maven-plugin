package org.example.jobs.it

import org.example.it.mocks.MyLogMock
import spock.lang.Specification

class Job1Spec extends Specification {
    Job1 job1ToTest
    MyLogMock myLogMock
    def setup() {
        myLogMock = GroovyMock(MyLogMock)
        def steps = [:]
        steps.myLog = myLogMock
        job1ToTest = new Job1(steps)

    }

    def "test readDUs"() {
        when:
        job1ToTest.run()
        then:
        1 * myLogMock.info ("Job1 executed")
    }
}
