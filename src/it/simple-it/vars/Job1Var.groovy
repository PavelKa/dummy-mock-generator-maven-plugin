import groovy.json.JsonSlurperClassic
import org.example.jobs.it.Job1

def call(){
    def job1= new Job1(this)
    job1.run()
    // just for testing purpose to verify if org.codehaus.groovy.control.CompilationUnit compile with dependencies
    def jsonSlurper = new JsonSlurperClassic()

}