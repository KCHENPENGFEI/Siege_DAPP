pragma solidity ^0.5.0;

contract C {
	struct Payment {
    	address maker;
    	uint256 id;
	}

  	struct Purchase {
    	uint256 productId;
    	bool complete;
    	Payment[] payments;
  	}

  	mapping (uint256 => Purchase) purchase;

  	function intiPurchase(uint256 index) public {

  		Payment[] memory p = new Payment[](0);
  		purchase[index] = Purchase({
  			productId: 1,
  			complete: true,
  			payments: p
  		});
  	}
}