#!/bin/bash

# User types their balance
read -p "Please enter your card balance: " balance

# User menu
echo "Choose an action:"
echo "B - Check Balance"
echo "W - Withdraw"
echo "D - Deposit"
read -p "Enter your choice (B/W/D): " action

case $action in
    "B" | "b")
        # Check Balance
        echo "Your balance is: $balance €"
        ;;
    "W" | "w")
        # Withdraw
        read -p "Enter the amount you want to withdraw: " withdrawal_amount
        if (( $(echo "$withdrawal_amount > $balance" | bc -l) )); then
            echo "Transaction cannot be completed: The amount exceeds the available balance."
        elif (( $(echo "$withdrawal_amount > 1000" | bc -l) )); then
            echo "Transaction cannot be completed: The amount exceeds the maximum allowed withdrawal limit (1000 €)."
        else
            balance=$(echo "$balance - $withdrawal_amount" | bc -l)
            echo "Transaction completed. Your new balance is: $balance €"
        fi
        ;;
    "D" | "d")
        # Deposit
        read -p "Enter the amount you want to deposit: " deposit_amount
        balance=$(echo "$balance + $deposit_amount" | bc -l)
        echo "Transaction completed. Your new balance is: $balance €"
        ;;
    *)
        echo "Invalid choice. Please try again."
        ;;
esac
