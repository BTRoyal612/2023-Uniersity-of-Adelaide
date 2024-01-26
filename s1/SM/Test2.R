x# Load the necessary library
library(dplyr)

# Create the tibble
df <- tibble(
  x = 1:10,
  y = c(2.44, 3.32, 8.40, 8.38, 11.3, 13.1, 14.8, 17.6, 18.3, 20.5)
)

# Fit the linear regression model
model <- lm(y ~ x, data = df)

# Calculate the leverage for all points
leverage <- hatvalues(model)

# Get the leverage for the 4th point and round it to two decimal places
leverage_4th_point <- round(leverage[4], 2)

# Print the result
print(leverage_4th_point)

x <- 196.56
y <- 101.37
2*(x-y)

1 - pchisq(1773.6, df = 96)

x = c(1,2,3,4,5) #time data - numeric predictor
X <- matrix(1,nrow = length(x), ncol = 2) #design matrix
X[,2] <- x
X

n = c(10,10,10,10,10)
y = c(8,5,2,1,10)

v <- log((y + 0.5)/(n-y+0.5))
b0 = solve(t(X) %*% X) %*% t(X) %*% v
b0 #initial estimate of beta

eta = X %*% b0
eta

pii <- exp(eta)/(1+exp(eta))
npii = n*pii
npii

D <- matrix(0,nrow=5,ncol=5)
diag(D) <- npii*(1-pii)

beta_t <- c(-4.0, 1.4)
beta_t1 <- beta_t + solve(t(X) %*% D %*% X) %*% t(X) %*% (y - npii)
beta_t1

# 6)
eta <- (-4)
pi <- exp(eta)/(1+exp(eta))
pi
y_i <- 76
n_i <- 90
r_i <- (y_i-n_i*pi)/sqrt(n_i*pi*(1-pi))

# 8)
x <- c(1,2,3,4,5)
X <- matrix(1, nrow = length(x), ncol=2)
X[,2] <- x
X

n <- c(10,10,10,10,10)
y <- c(0,2,6,7,9)

b0 <- c(-4, 1.2)
b0
eta = X %*% b0
eta
pii <- exp(eta)/(1+exp(eta))
npii = n*pii
npii

D <- matrix(0,nrow=5,ncol=5)
diag(D) <- npii*(1-pii)

b1 <- b0 + solve(t(X) %*% D %*% X) %*% t(X) %*% (y-npii)
b1

# 2)
x = c(1,2,3,4,5)
y = c(2,5,4,3,1)
M1 <- lm(y~x)

V_sqrt <- diag(1 / sqrt(x))
y_trans <- V_sqrt %*% y

X <- model.matrix(M1)
X_trans <- V_sqrt %*% X
X_trans

M2 <- lm(y_trans ~ X_trans - 1)
summary(M2)
