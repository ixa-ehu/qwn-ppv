my $sense="";
my $trainAcc="";
my $thres="";
my $dict="";
my $pp=0;
my $pn=0;
my $rp=0;
my $rn=0;
my $fp=0;
my $fn=0;
my $acc=0;
my $size=0;
my $graph="";

print "dict\tsize\tthreshold\taccuracy on train\taccurary\tP+\tR+\tF+\tP-\tR-\tF-\tsense\n";

while ($l=<stdin>)
{
    chomp $l;    
    if ($l =~/^AvgRatioEstimator: lexicon loaded  --> (.*) - ([0-9]+) entries$/)
    {
        $dict= $1;
	@path=split /\//, $dict;
	$dict=$path[$#path];
	$graph=$dict;
	$graph=~s/^.*_([^\.]+)\.dict$/$1/;
        $size=$2;
        
    }
    elsif ($l =~ /Sense information: (.*)$/)
    {
	$sense=$1;
    }
    elsif ($l=~ /Accuracy => ([0-9\.]+)/)
    {
        $acc = $1 * 100;
    }
    elsif ($l=~ /Positive docs: P => ([0-9\.]+)\tR => ([0-9\.]+)\tF => ([0-9\.]+)/)
    {
        $pp = $1 * 100;
        $rp = $2 * 100;
        $fp = $3 * 100;
                
    }
    elsif ($l=~ /Negative docs: P => ([0-9\.]+)\tR => ([0-9\.]+)\tF => ([0-9\.]+)/)
    {
        $pn = $1 * 100;
        $rn = $2 * 100;
        $fn = $3 * 100;

    }
    elsif ($l=~ / - Threshold: (-?[0-9\.]+) - max Accuracy: ([0-9\.]+) ----/)
    {
        $thres=$1;
        $trainAcc=$2 * 100;
    } 
    elsif ($l =~ /QWN-PPV: Lexicon evaluator: End./)
    {
        print "$dict\t$size\t$thres\t$trainAcc\t$acc\t$pp\t$rp\t$fp\t$pn\t$rn\t$fn\t$sense\t$graph\n";	
    }

}

