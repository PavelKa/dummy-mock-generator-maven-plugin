File file = new File(basedir,'build.log')
println file.text
File gs = new File(basedir, "target/generated-test-sources")
println("gs.isDirectory()")
assert gs.isDirectory()
