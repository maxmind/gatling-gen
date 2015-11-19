#!/usr/sbin/perl

use Modern::Perl;

use JavaScript::Duktape;
use Path::Class qw( dir file );

my $project_name  = 'gatling-gen';
my $sbt_project   = 'gatlingGen';
my $package       = 'com.maxmind.gatling.gen';
my @test_names    = qw( GenerationTests );
my $scala_version = '2.11';
my $compile_dir   = 'target';
my @part_names    = qw( jsdeps fastopt launcher );
my $test_lang     = 'JVM';
my $js_dir        = dir( 'js', $compile_dir, "scala-$scala_version" );
my @part_files    = map { $js_dir->file( "${project_name}-$_.js" ) } @part_names;
my $tests         = join q{ }, map { "${package}.$_" } @test_names;
my $sbt_test      = "${sbt_project}${test_lang}/testOnly -- $tests";
my $tasks         = qq{sbt compile '$sbt_test' fastOptJS};

say "# Sbt tasks: $tasks";
system($tasks);

my @sources = map { scalar $_->slurp } @part_files;
splice @sources, 2, 0, "console={log:function(m){print(m);}};\n";
my $source = join "\n", @sources;

my $result = JavaScript::Duktape->new->eval($source);

say "# Duktape: $result" if defined $result;
say "# If some random seed was printed above this line, then all is OK.";

my $out = dir($compile_dir)->file("$project_name-duktape.js");
$out->spew($source);

say "# Compiled $out";
