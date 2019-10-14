pragma solidity ^0.4.15;

contract test {
    uint256 a = 1;
    uint256 b = 2;

    function addBoth(uint256 item1, uint256 item2) public returns(uint256) {
        return(item1 + item2);
    }
}