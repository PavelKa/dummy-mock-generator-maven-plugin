package org.example.jobs.it
class Job1{
    def steps

    Job1(steps) {
        this.steps = steps
    }

    def run(){
        steps.myLog.info "Job1 executed"
    }
}