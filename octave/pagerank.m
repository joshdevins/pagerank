%%
%% PageRank
%%

% PageRank with uniform priors and no previous solution
function [v, num_iters] = unbiased_pagerank(A, alpha, v_quadratic_error_threshold, max_iters)

  n = size(A, 1); % number of nodes in the graph
  s = ones(n, 1) / n;

  % uniform prior: 1/n
  % uniform starting solution: 1/n
  % both are the same so just use s for both

  [v, num_iters] = pagerank(A, alpha, s, s, v_quadratic_error_threshold, max_iters);
endfunction

% PageRank with specific priors vector and a previous solution
function [v, num_iters] = pagerank(A, alpha, priors, v, v_quadratic_error_threshold, max_iters)

  % scalars
  n = size(A, 1); % number of nodes in the graph

  % matrices
  sum_vector = sum(A, 2); % col vector with values are sum of rows

  % use eps to ensure dangling rows don't kill this
  M = spdiags(1 ./ (eps + sum_vector), 0, n, n) * A; % L1 row normalised
  P = M';

  % vectors
  d = sum_vector < eps; % dangling nodes vector where d(i) is 1 for dangling node
  p = alpha * priors;

  % derived constants
  P_hat = (1 - alpha) * (P - diag((1 / n - 1) * d));

  % iterate
  % use L2 norm of diff between solution vectors to check for stopping criteria
  for num_iters = 1:max_iters
    last_v = v;
    mu = ((1 - alpha) / (n - 1)) * (d' * last_v);
    v = (P_hat * last_v) + mu + p;

    if norm(v - last_v, 2) < v_quadratic_error_threshold
      return;
    end
  end
endfunction
