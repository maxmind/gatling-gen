#!/usr/sbin/perl

use Modern::Perl;

use JavaScript::Duktape;
use Path::Class qw( dir file );

my $js_project    = 'vandegraaf';
my $scala_version = '2.11';
my $compile_dir   = 'target';
my @part_names    = qw( jsdeps fastopt launcher );
my $js_dir        = dir( 'js', $compile_dir, "scala-$scala_version" );
my @part_files    = map { $js_dir->file( "$js_project-$_.js" ) } @part_names;

system("sbt compile fastOptJS");

my @sources = map { scalar $_->slurp } @part_files;
splice @sources, 2, 0, "console={log:function(m){print(m);}};\n";
my $source = join "\n", @sources;

my $result = JavaScript::Duktape->new->eval($source);

say "# Duktape: $result" if defined $result;

my $out = dir($compile_dir)->file("$js_project-duktape.js");
$out->spew($source);

say "# Compiled $out";
