pragma solidity ^0.4.25;

contract testContract {
	uint256 item = 1000;

	function printItem() public view returns(uint256) {
		return(item);
	}
}