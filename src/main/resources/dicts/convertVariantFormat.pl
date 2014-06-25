%hash;

my $from = shift;
my $targetVariant = 0;
if ($from eq "s")
{
    $targetVariant = 1;
}

unless ($targetVariant)
{
    while ($l=<stdin>)
    {
	chomp $l;
	($variant, @synsets)= split /\s+/, $l;

	foreach $i (@synsets)
	{
	    if (! defined $hash{$i})
	    {
		$hash{$i}="$variant";
	    }
	    else
	    {
		$hash{$i}.=", $variant";
	    }
	}
    }
}
else
{
    while ($l=<stdin>)
    {
	chomp $l;
	($synset, my $variant)= split /\t/, $l;
	(my @variants)= split /,/, $variant;

	foreach $i (@variants)
	{
	    $i=~s/^\s*//;

	    if (! defined $hash{$i})
	    {
		$hash{$i}="$synset";
	    }
	    else
	    {
		$hash{$i}.=" $synset";
	    }
	}
    }
}


for $key (sort keys %hash)
{
    if  ($targetVariant)
    {
	print "$key $hash{$key}\n";
    }
    else
    {
	print "$key\t$hash{$key}\n";
    }
}
