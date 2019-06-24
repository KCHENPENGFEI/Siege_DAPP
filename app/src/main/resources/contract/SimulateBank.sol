
contract SimulateBank{

    address owner;

    bytes32 bankName;

    uint bankNum;

    bool isInvalid;

    mapping(address => uint) public accounts;

    constructor( bytes32 _bankName,uint _bankNum,bool _isInvalid) public {

        bankName = _bankName;

        bankNum = _bankNum;

        isInvalid = _isInvalid;

        owner = msg.sender;

    }

    function issue(address addr,uint number) public returns (bool){

        if(msg.sender==owner){

            accounts[addr] = accounts[addr] + number;

            return true;

        }

        return false;

    }

    function transfer(address addr1,address addr2,uint amount) public returns (bool){

        if(accounts[addr1] >= amount){

            accounts[addr1] = accounts[addr1] - amount;

            accounts[addr2] = accounts[addr2] + amount;

            return true;

        }

        return false;

    }

    function getAccountBalance(address addr) public view returns(uint){

        return accounts[addr];

    }

    function newFunc() public pure returns(bool){
        return true;
    }

}