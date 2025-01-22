import org.example.jobs.it.Job1
import groovy.json.JsonSlurperClassic
def call(){
    def job1= new Job1()
    job1.run()
    // test if org.codehaus.groovy.control.CompilationUnit compile with dependencies
    def jsonSlurper = new JsonSlurperClassic()

}