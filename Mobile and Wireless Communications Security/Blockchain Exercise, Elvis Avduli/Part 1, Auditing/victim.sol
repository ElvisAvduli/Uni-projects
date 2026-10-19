pragma solidity ^0.4.17;

contract N0t_Victim {
  mapping (address => uint) public balances;

function deposit() public payable {
        balances[msg.sender] += msg.value;
    }

    function withdraw(uint value) public {
        uint bal = balances[msg.sender];
        require(bal >= value);
        balances[msg.sender] -= value;
        msg.sender.transfer(value);
    }
}
