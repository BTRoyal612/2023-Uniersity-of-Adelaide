pacman::p_load(tidyverse, matlib)
setwd("~/RStudio")  

# Q2
y2 = c(2,5,1,3,4)
x2 = c(1,2,3,4,5)
V2 <- matrix(0, ncol = 5, nrow =5)
diag(V2) <- diag(cbind(x2,x2,x2,x2,x2))
V2
X2 <- matrix(1,nrow = length(x2), ncol = 2)
X2[,2] <- x2
X2
beta_gls <- solve(t(X2) %*% inv(V2) %*% X2) %*% t(X2) %*% inv(V2) %*% y2
beta_gls

# Q3
x3 = c(1,2,3,4,5)
n3 = c(10,10,10,10,10)
y3 = c(4,10,0,8,2)
v3 <- log((y3 + 0.5)/(n3 - y3 + 0.5))
v3
X3 <- matrix(1, nrow = length(x3), ncol = 2)
X3[,2] <- x3
X3
b03 <- solve(t(X3) %*% X3) %*% t(X3) %*% v3
b03


# Q8
x8 <- c(1,2,3,4,5)
n8 <- c(10,10,10,10,10)
y8 <- c(0,2,6,7,9)
beta_t <- c(-4, 1.3)
v8 <- log((y8 + 0.5)/(n8 - y8 + 0.5))
X8 <- matrix(1, nrow = length(x8), ncol = 2) 
X8[,2] <- x8
X8
eta_t = X8 %*% beta_t
eta_t
pii_t <- exp(eta_t)/(1+exp(eta_t))
npii_t <- n8*pii_t
npii_t
D<-matrix(0, nrow = 5, ncol = 5)
diag(D) <- npii_t*(1-pii_t)
D
bt1 <- beta_t + solve(t(X8) %*% D %*% X8) %*% t(X8) %*% (y8 - npii_t)
bt1

