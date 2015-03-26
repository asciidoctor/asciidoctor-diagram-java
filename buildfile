require 'buildr/protobuf'

repositories.remote << 'http://repo1.maven.org/maven2'
repositories.release_to = "file://#{File.dirname(__FILE__)}"

JUNIT = 'junit:junit:jar:4.11'

THIS_VERSION = "1.3.2"

define 'asciidoctor-diagram-java', :version => THIS_VERSION do
  no_ipr
  iml.jdk_version = '1.7'

  deps = FileList[_('lib/**.jar')].flatten
  compile.with deps

  test.with JUNIT

  jar = package(:jar)
  package(:zip).tap do |z|
    z.include jar
    z.include deps
  end
end