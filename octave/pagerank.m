%%
%% PageRank
%%

% common setup with unbiased priors, no starting solution
function [n, priors, v] = setup_pagerank(A)

  % matrix size/number of nodes -- assumes a square matrix already
  n = size(A, 1);

  unbiased = ones(n, 1) / n;
  priors = unbiased; % unbiased priors
  v = unbiased;      % no starting solution
endfunction

% iteratove PageRank via power method
function [v, num_iters] = naive_pagerank_core(P, alpha, priors, v, error_threshold, max_iters)
  % iterate
  % use L2 norm of diff between solution vectors to check for stopping criteria
  for num_iters = 1:max_iters
    last_v = v;
    v = ((1 - alpha) * P * v) + (alpha * priors);

    if norm(v - last_v, 2) < error_threshold
      return;
    end
  end
endfunction

% PageRank: naïve
function [v, num_iters] = naive_pagerank(A, alpha, error_threshold, max_iters)
  [n, priors, v] = setup_pagerank(A);

  % L1 row normalize
  % use eps to ensure dangling rows don't kill this
  sum_vector = sum(A, 2); % col vector where values are sum of rows
  M = spdiags(1 ./ (eps + sum_vector), 0, n, n) * A;
  P = M';

  [v, num_iters] = naive_pagerank_core(P, alpha, priors, v, error_threshold, max_iters);
endfunction

% PageRank: dangling
function [v, num_iters] = dangling_pagerank(A, alpha, error_threshold, max_iters)
  [n, priors, v] = setup_pagerank(A);

  % build dangling nodes matrix where d(ij) is 1 for dangling node i (row)
  d = sum(A, 2) < eps;
  D = (spdiags(d / n, 0, n, n) * ones(n, n));

  % L1 row normalize
  % use eps to ensure dangling rows don't kill this
  B = A + D;
  sum_vector = sum(B, 2); % col vector where values are sum of rows
  M = spdiags(1 ./ (eps + sum_vector), 0, n, n) * B;
  P = M';

  [v, num_iters] = naive_pagerank_core(P, alpha, priors, v, error_threshold, max_iters);
endfunction

% PageRank: dangling, no self-references
function [v, num_iters] = dangling_selfless_pagerank(A, alpha, error_threshold, max_iters)
  [n, priors, v] = setup_pagerank(A);

  % build dangling nodes matrix where d(ij) is 1 for dangling node i (row)
  d = sum(A, 2) < eps;
  D = (spdiags(d, 0, n, n) * ones(n, n) - diag(d));

  % L1 row normalize
  % use eps to ensure dangling rows don't kill this
  B = A + D;
  sum_vector = sum(B, 2); % col vector where values are sum of rows
  M = spdiags(1 ./ (eps + sum_vector), 0, n, n) * B;
  P = M';

  [v, num_iters] = naive_pagerank_core(P, alpha, priors, v, error_threshold, max_iters);
endfunction

% PageRank: dangling, optimized, unbiased
function [v, num_iters] = pagerank(A, alpha, error_threshold, max_iters)
  [n, priors, v] = setup_pagerank(A);

  % L1 row normalize
  % use eps to ensure dangling rows don't kill this
  sum_vector = sum(A, 2); % col vector where values are sum of rows
  M = spdiags(1 ./ (eps + sum_vector), 0, n, n) * A;
  P = M';

  % build dangling nodes vector where d(i) is 1 for dangling node
  d = sum_vector < eps;

  % build constants
  a = alpha * priors;
  P_dot = (1 - alpha) * (P - diag((1 / (n - 1)) * d));
  mu = ((1 - alpha) / (n - 1)) * d';

  % iterate
  % use L2 norm of diff between solution vectors to check for stopping criteria
  for num_iters = 1:max_iters
    last_v = v;
    v = (P_dot * last_v) + (mu * last_v) + a;

    if norm(v - last_v, 2) < error_threshold
      return;
    end
  end
endfunction

% PageRank: alternative dangling, optimized, unbiased
function [v, num_iters] = alt_dangling_pagerank(A, alpha, error_threshold, max_iters)
  [n, priors, v] = setup_pagerank(A);

  % L1 row normalize
  % use eps to ensure dangling rows don't kill this
  sum_vector = sum(A, 2); % col vector where values are sum of rows
  M = spdiags(1 ./ (eps + sum_vector), 0, n, n) * A;
  P = M';

  % build dangling nodes vector where d(i) is 1 for dangling node
  dangling = sum_vector < eps;
  not_dangling = sum_vector > eps;

  % iterate
  % use L2 norm of diff between solution vectors to check for stopping criteria
  for num_iters = 1:max_iters
    last_v = v;
    v = ((1 - alpha) * P * v) + (alpha * priors);
    beta = (1.0 - sum(v)) / n;
    v = v + beta;

    if norm(v - last_v, 2) < error_threshold
      return;
    end
  end
endfunction

% PageRank: selfless, alternative dangling, optimized, unbiased
function [v, num_iters] = alt_dangling_selfless_pagerank(A, alpha, error_threshold, max_iters)
  [n, priors, v] = setup_pagerank(A);

  % L1 row normalize
  % use eps to ensure dangling rows don't kill this
  sum_vector = sum(A, 2); % col vector where values are sum of rows
  M = spdiags(1 ./ (eps + sum_vector), 0, n, n) * A;
  P = M';

  % build dangling nodes vector where d(i) is 1 for dangling node
  dangling = sum_vector < eps;
  not_dangling = sum_vector > eps;

  P_dot = P - diag((1 / (n - 1)) * dangling);

  % iterate
  % use L2 norm of diff between solution vectors to check for stopping criteria
  for num_iters = 1:max_iters
    last_v = v;
    v = ((1 - alpha) * P_dot * v) + (alpha * priors);
    beta = (1.0 - sum(v)) / n;
    v = v + beta;

    if norm(v - last_v, 2) < error_threshold
      return;
    end
  end
endfunction

without_dangle = [
    0.0 0.0 0.0 0.0 1.0 ;
    0.5 0.0 0.0 0.0 0.0 ;
    0.5 0.0 0.0 0.0 0.0 ;
    0.0 1.0 0.5 0.0 0.0 ;
    0.0 0.0 0.5 1.0 0.0 ;
  ];

with_dangle = [
    0.0 0.0 0.0 0.0 0.0 ; % dangling
    0.5 0.0 0.0 0.0 0.0 ;
    0.5 0.0 0.0 0.0 0.0 ;
    0.0 1.0 0.5 0.0 0.0 ;
    0.0 0.0 0.5 1.0 0.0 ;
  ];

weightless_without_dangle = [
    0.0 0.0 0.0 0.0 1.0 ;
    1.0 0.0 0.0 0.0 0.0 ;
    1.0 0.0 0.0 0.0 0.0 ;
    0.0 1.0 1.0 0.0 0.0 ;
    0.0 0.0 1.0 1.0 0.0 ;
  ];

weightless_with_dangle = [
    0.0 0.0 0.0 0.0 0.0 ; % dangling
    1.0 0.0 0.0 0.0 0.0 ;
    1.0 0.0 0.0 0.0 0.0 ;
    0.0 1.0 1.0 0.0 0.0 ;
    0.0 0.0 1.0 1.0 0.0 ;
  ];

[v, num_iters] = naive_pagerank                (without_dangle, 0.15, 0.001, 100)
[v, num_iters] = dangling_pagerank             (without_dangle, 0.15, 0.001, 100)
[v, num_iters] = dangling_selfless_pagerank    (without_dangle, 0.15, 0.001, 100)
[v, num_iters] = pagerank                      (without_dangle, 0.15, 0.001, 100)
[v, num_iters] = alt_dangling_pagerank         (without_dangle, 0.15, 0.001, 100)
[v, num_iters] = alt_dangling_selfless_pagerank(without_dangle, 0.15, 0.001, 100)

[v, num_iters] = naive_pagerank                (with_dangle,    0.15, 0.001, 100)
[v, num_iters] = dangling_pagerank             (with_dangle,    0.15, 0.001, 100)
[v, num_iters] = dangling_selfless_pagerank    (with_dangle,    0.15, 0.001, 100)
[v, num_iters] = pagerank                      (with_dangle,    0.15, 0.001, 100)
[v, num_iters] = alt_dangling_pagerank         (with_dangle,    0.15, 0.001, 100)
[v, num_iters] = alt_dangling_selfless_pagerank(with_dangle,    0.15, 0.001, 100)
