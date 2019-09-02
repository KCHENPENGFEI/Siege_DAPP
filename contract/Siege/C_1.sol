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

  	Purchase a;

  	function intiPurchase(uint256 index) public {

  		Purchase storage pur = a;
  		Payment memory p = Payment({
  			maker: msg.sender,
  			id: 0
  		});

  		pur.productId = 1;
  		pur.complete = true;
  		pur.payments.push(p);

  		purchase[index] = pur;
  	}
}