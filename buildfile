require 'buildr/protobuf'

repositories.remote << 'http://repo1.maven.org/maven2'

JUNIT = 'junit:junit:jar:4.11'

define 'asciidoctor-diagram-java', :version => '1.3.0-SNAPSHOT' do
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