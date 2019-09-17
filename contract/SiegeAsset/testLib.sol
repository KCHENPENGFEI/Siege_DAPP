pragma solidity ^0.5.0;

import "./lib.sol";

contract testLib {
	function test() public pure returns(uint256) {
		uint256 a = 1;
		uint256 b = 2;
		return (lib.plus(a, b));
	}
}