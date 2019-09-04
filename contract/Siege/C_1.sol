pragma solidity ^0.5.0;

contract C {
	struct Payment {
    	address maker;
    	uint256 id;
	}

  	struct Purchase {
    	uint256 productId;
    	bool complete;
    	Payment[5] payments;
  	}

  	mapping (uint256 => Purchase) purchase;

  	Purchase a;

  	function intiPurchase(uint256 index) public {

  		// Purchase storage pur = a;
  		Payment memory p = Payment({
  			maker: msg.sender,
  			id: 0
  		});

  		// pur.productId = 1;
  		// pur.complete = true;
  		// pur.payments[0] = p;
    //   pur.payments[1] = p;
    //   pur.payments[2] = p;
    //   pur.payments[3] = p;
    //   pur.payments[4] = p;

  		// purchase[index] = pur;
      purchase[index].productId = 1;
      purchase[index].complete = true;
      purchase[index].payments[0] = p;
  	}
}