
% based on code from Wikipedia: http://en.wikipedia.org/wiki/PageRank
function [v] = pagerank(A, alpha, priors, previous_v, v_quadratic_error_threshold)

% scalars
n = size(M, 2); % number of nodes in the graph

% matrices
M = norm(A, 1); % L1 norm
P = M.';

% vectors
% build dangling nodes vector, where entry is 1 when node is dangling, 0 otherwise
d = max(P, [], 1); % dangling nodes will be 0 here, others will be non-0
for i = 0:n
  if (d (i) == 0)
    d (i) = 1;
  else
    d (i) = 0;
  endif
endfor

ones = ones(n, n);

% constants
P_hat = (1 - alpha) .* (P - diag((1 / n - 1) .* d));

% iterate
% use L2 norm of diff between solution vectors to check for stopping criteria
while(norm(v - last_v, 2) > v_quadratic_error_threshold)
  last_v = v;
  mu = (1 - alpha) .* ((1 / n - 1) .* d.') .* last_v + (alpha * 1 / n)
  v = P_hat .* last_v + mu .* ones;
end
 
endfunction
