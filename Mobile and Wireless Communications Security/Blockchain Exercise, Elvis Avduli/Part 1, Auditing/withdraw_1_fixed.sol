pragma solidity 0.4.17;

contract withdrawBalance {
    mapping (address => uint) balance;

    function setBalance() public payable {
        balance[msg.sender] += msg.value;
    }
   
    function withdraw() public returns (uint) {
        uint amount = balance[msg.sender];
        require(amount > 0);
        balance[msg.sender] = 0;
        msg.sender.transfer(amount);
        return amount;
    }
}