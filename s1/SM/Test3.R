# Q1
b1 <- 0.5
exp(b1)

# Q2
x <- 2.2
b0 <- 2.5
b1 <- 1.4
eta <- b0 + x*b1
y <- exp(eta)
y

# Q3
library(MASS)
k <- 3
sigma2 <- 2
obs <- c(7,6,10,7,4)
pred <- c(7,8,8,8,4)
n <- length(obs)

res <- obs - pred

rss <- sum(res^2)
mae <- mean(abs(res))
mse <- mean(res^2)
r2 <- cor(obs, pred)^2
MCp <- rss/sigma2 + 2*k - n

# Q4
X <- c(1,2,3,4,5)
Y <- c(7,9,4,9,5)
x0 <- 6
y_mean <- mean(Y)
y_mean

# Q5

# Q6
lambda <- 4
X <- c(-2,-1,0,1,2)
Y <- c(1.4,0.4,3.4,-3.6,-1.6)

M1 <- lm(Y~X)
X_matrix <- model.matrix(M1)
I <- diag(ncol(X_matrix))

beta <- solve(t(X_matrix) %*% X_matrix + lambda*I) %*% t(X_matrix) %*% Y
beta