# Q1
k <- 17
lg_lik <- -493.86
aic <- 2*(k-lg_lik)
aic

# Q2
aic <- 186
k <- 10
n <- 200
aicc <- aic + (2*k*(k+1))/(n-k-1)
aicc

# Q3
aic <- 127
k <- 7
n <- 160
lg_lik <- k - (aic/2)
bic <- log(n)*k - 2*lg_lik
bic

# Q4
library(Metrics)
y <- c(3,6,10,4,7)
predicted <- c(4,6,9,3,6)

res4 <- mae(predicted, y)
res4

# Q5
library(Metrics)
y <- c(8,1,4,2,5)
predicted <- c(9,0,4,3,4)

res5 <- rmse(predicted, y)
res5