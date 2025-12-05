# Banking System CLI
# Usage: ./banking_cli.ps1

$AUTH_SERVICE_URL = "http://localhost:8081/api/auth"
$TECHNICAL_SERVICE_URL = "http://localhost:8082/api/technical"
$BUSINESS_FUNCTION_URL = "http://localhost:8083/api/business/functions"
$BUSINESS_TRANSACTION_URL = "http://localhost:8084/api/business/transactions"
$PAYMENT_GATEWAY_URL = "http://localhost:8085/api/payments"

function Show-Menu {
    param ($Role)
    Write-Host "`n--- Banking System Menu ($Role) ---" -ForegroundColor Cyan
    if ($Role -eq "CUSTOMER") {
        Write-Host "1. View Balance"
        Write-Host "2. View Transactions"
        Write-Host "3. Transfer Money"
    }
    elseif ($Role -eq "OPERATIONS_CLERK") {
        Write-Host "1. Deposit Money"
    }
    elseif ($Role -eq "ADMIN") {
        Write-Host "1. Create Account"
        Write-Host "2. View All Accounts"
        Write-Host "3. Lock Account Funds"
    }
    Write-Host "Q. Quit"
}

function Login {
    Write-Host "`n--- Login ---" -ForegroundColor Yellow
    $username = Read-Host "Username"
    $password = Read-Host "Password"

    try {
        $response = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/login" -Method Post -ContentType "application/json" -Body (@{username = $username; password = $password } | ConvertTo-Json)
        Write-Host "Login Successful! Welcome, $($response.username) (ID: $($response.id))" -ForegroundColor Green
        return $response
    }
    catch {
        Write-Host "Login Failed: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

function Wait-ForPaymentStatus {
    param ($TransactionId, $UserId)
    Write-Host "Processing Payment..." -NoNewline
    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Seconds 2
        try {
            $statusRes = Invoke-RestMethod -Uri "$PAYMENT_GATEWAY_URL/$TransactionId/status" -Headers @{"X-User-Id" = $UserId }
            $status = $statusRes.status
            if ($status -eq "SUCCESS") {
                Write-Host " Done!"
                Write-Host "Transaction SUCCESS" -ForegroundColor Green
                return
            }
            elseif ($status -eq "FAILED") {
                Write-Host " Done!"
                Write-Host "Transaction FAILED" -ForegroundColor Red
                return
            }
            Write-Host "." -NoNewline
        }
        catch {
            Write-Host "." -NoNewline
        }
    }
    Write-Host "`nTimeout: Transaction status unknown (check later)" -ForegroundColor Yellow
}

# Main Loop
while ($true) {
    $user = Login
    if ($null -eq $user) { continue }

    while ($true) {
        Show-Menu -Role $user.role
        $choice = Read-Host "Select an option"

        if ($choice -eq "Q") { break }

        try {
            if ($user.role -eq "CUSTOMER") {
                if ($choice -eq "1") {
                    # View Balance
                    $accNum = Read-Host "Enter Account Number"
                    $balance = Invoke-RestMethod -Uri "$BUSINESS_FUNCTION_URL/$accNum/balance" -Headers @{"X-User-Id" = $user.id }
                    Write-Host "Balance: $balance" -ForegroundColor Green
                }
                elseif ($choice -eq "2") {
                    # View Transactions
                    $accNum = Read-Host "Enter Account Number"
                    $txns = Invoke-RestMethod -Uri "$BUSINESS_FUNCTION_URL/$accNum/transactions" -Headers @{"X-User-Id" = $user.id }
                    Write-Host "Transactions:" -ForegroundColor Green
                    $txns | Format-Table
                }
                elseif ($choice -eq "3") {
                    # Transfer
                    $from = Read-Host "From Account"
                    $to = Read-Host "To Account"
                    $amt = Read-Host "Amount"
                    $body = @{sourceAccount = $from; targetAccount = $to; amount = $amt } | ConvertTo-Json
                    $res = Invoke-RestMethod -Uri "$PAYMENT_GATEWAY_URL" -Method Post -ContentType "application/json" -Headers @{"X-User-Id" = $user.id } -Body $body
                    Wait-ForPaymentStatus -TransactionId $res -UserId $user.id
                }
            }
            elseif ($user.role -eq "OPERATIONS_CLERK") {
                if ($choice -eq "1") {
                    # Deposit
                    $to = Read-Host "Target Account"
                    $amt = Read-Host "Amount"
                    $body = @{targetAccount = $to; amount = $amt } | ConvertTo-Json
                    $res = Invoke-RestMethod -Uri "$PAYMENT_GATEWAY_URL/deposit" -Method Post -ContentType "application/json" -Headers @{"X-User-Id" = $user.id } -Body $body
                    Wait-ForPaymentStatus -TransactionId $res -UserId $user.id
                }
            }
            elseif ($user.role -eq "ADMIN") {
                if ($choice -eq "1") {
                    # Create Account
                    $owner = Read-Host "Owner Name"
                    $bal = Read-Host "Initial Balance"
                    $uid = Read-Host "User ID for Owner"
                    $body = @{ownerName = $owner; initialBalance = $bal; userId = $uid } | ConvertTo-Json
                    $res = Invoke-RestMethod -Uri "$BUSINESS_TRANSACTION_URL/accounts" -Method Post -ContentType "application/json" -Headers @{"X-User-Id" = $user.id } -Body $body
                    Write-Host "Account Created: $($res.accountNumber)" -ForegroundColor Green
                }
                elseif ($choice -eq "2") {
                    # View All
                    $accs = Invoke-RestMethod -Uri "$BUSINESS_FUNCTION_URL/accounts" -Headers @{"X-User-Id" = $user.id }
                    $accs | Format-Table
                }
                elseif ($choice -eq "3") {
                    # Lock Funds
                    $accNum = Read-Host "Account Number"
                    $amt = Read-Host "Amount to Lock"
                    $body = @{amount = $amt } | ConvertTo-Json
                    $res = Invoke-RestMethod -Uri "$TECHNICAL_SERVICE_URL/$accNum/lock" -Method Post -ContentType "application/json" -Headers @{"X-User-Id" = $user.id } -Body $body
                    Write-Host $res -ForegroundColor Green
                }
            }
        }
        catch {
            Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        }
        Write-Host "`n"
    }
}
