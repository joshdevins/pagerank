# PageRank

PageRank is one of those definitive graph algorithms these days. It seems that everyone more or less knows it (at least intuitively), and everyone that has a linear algebra or graph library uses it as a demo of some sorts. The downside is that most of the times, there is no consensus on the exact definition of the PageRank algorithm and so most demos use some version of a very simple/naive implementation. Useful for illustration purposes, but not much else.

Inside this little gem I'll be implementing the same PageRank algorithm in various languages, frameworks and toolkits with the express condition that they are somehow worthy and applicable to be used in a production environment. Call it the Rosetta Stone of PageRanks.

# Algorithm

An outline of the PageRank algorithm implemented will follow once I get around to LaTeX'ing it all.

# Implementations

 * Finished implementations link to their source
 * Planned implementations link to, well, nothing

## Single-machine

 * Octave/Matlab -- because everyone needs an Octave/Matlab example (although I would never do this in production)
 * Julia -- high-level, readable, open-source and wickedly fast
 * Cassovary -- awesomely fast, Scala API and Twitter use it in production (for now) so why not?!
 * GraphChi -- sharded graphs, not memory bound, easy to port to big brother GraphLab, too bad it's C++

## Distributed

 * Scalding -- for the raw power of the Hadoops in a nice Scala API
 * Giraph -- because iterative algorithms in plain-old Hadoop are painfully slow
 * GraphLab -- well, probably not because C++ is annoying
