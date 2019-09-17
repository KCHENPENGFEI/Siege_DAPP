pragma solidity ^0.5.0;

library lib{

	function plus(uint256 a, uint256 b) public pure returns(uint256 c) {
		c = a + b;
		return c;
	}
}