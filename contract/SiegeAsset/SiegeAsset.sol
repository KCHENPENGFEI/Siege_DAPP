pragma solidity ^0.5.0;

import "./InterfaceAsset.sol";
import "./SafeMath.sol";
import "./Utils.sol";
import "./strings.sol";
import "./Address.sol";

contract SiegeAsset is InterfaceAsset {
	using SafeMath for uint256;
	using Address for address;
	using strings for *;

	// Game asset record
	struct gameAsset {
		string symbol;
		uint256 issued;
		uint256 supply;
		uint256 maxSupply;
		address issuer;
		bool infinite;
		address[] signers;
		uint8 typ;
	}

	// extern game asset record
	struct extGameAsset {
		string symbol;
		uint256 supply;
	}

	// Game nft record
	struct gameNFT {
		uint256 uuid;
		string uri;
		string symbol;
		address owner;
		// signature fieled
		byte v;
		bytes32 r;
		bytes32 s;
	}

	// symbol => gameAsset
	mapping (string => gameAsset) assets;

	// symbol => extGameAsset
	mapping (string => extGameAsset) extAssets;

	// uuid => gameNFT
	mapping (uint256 => gameNFT) NFTs;

	// issuer => symbol (in Siege, may not be used)
	mapping (address => string[]) issues;

	// symbol => uuid[]
	mapping (string => uint256[]) uuidBySymbol;

	// owner => uuid[]
	mapping (address => uint256[]) uuidByOwner;

	// owner => symbol => balance
	mapping (address => mapping (string => uint256)) balance;

	// owner => (operator => approved)
    mapping (address => mapping(address => bool)) internal operatorApproval;
    // owner => (operator => (symbol => value))
    mapping (address => mapping(address => mapping(string => uint256))) internal operatorApprovalForCoin;
    // owner => (operator => (uuid => approved))
    mapping (address => mapping(address => mapping(uint256 => bool))) internal operatorApprovalForUUID;

    // event
    event TransferCoin(address indexed _from, address indexed _to, uint256 _value);
    event TransferNFT(address indexed _from, address indexed _to, uint256 _uuid);

    event ApproveForAll(address indexed _owner, address indexed _operator, bool _approved);
    event ApproveForCoin(address indexed _owner, address indexed _operator, uint256 _value, string _symbol, bool _approved);
    event ApproveForNFTs(address indexed _owner, address indexed _operator, uint256[] _uuids, bool[] _approved);

    address siegeAddr;

    uint64 internal _tokenId = 1;

    constructor() public {
        siegeAddr = msg.sender;
    }

    modifier onlyRoot() {
    	require(msg.sender == siegeAddr, "only contrct operator can do this action");
    	_;
    }

    modifier onlyIssuer(string memory symbol) {
    	require(msg.sender == assets[symbol].issuer, "only asset's issuer can do this action");
    	_;
    }

    /**
        @notice Create a fungible(coin) or non-fungible(nft) token type.
        @param _issuer  Issuer address
        @param _value   Token amount
        @param _symbol  Token symbol
        @param _type    Token type
    */
    function create(address _issuer, uint256 _value, string calldata _symbol, uint8 _type) external onlyRoot() {
    	require(assets[_symbol].issuer == address(0x0), "Token has existed");
    	// init the assets table
    	assets[_symbol] = gameAsset({
    		symbol: _symbol,
			issued: 0,
			supply: 0,
			maxSupply: _value,
			issuer: _issuer,
			infinite: _value == 0,
			signers: new address[](0),
			typ: _type
    	});
    	issues[_issuer].push(_symbol);
    }

    /**
        @notice Issue fungible token (coin).
        @param _to      Target address
        @param _value   Token amount
        @param _symbol  Token symbol
    */
    function issueCoin(address _to, uint256 _value, string calldata _symbol) external onlyIssuer(_symbol) {
    	gameAsset storage gt = assets[_symbol];
    	require(_to != address(0x0), "_to must be non-zero");
    	require(_value != 0, "amount should not be zero");
    	require(gt.issuer != address(0x0), "asset not exist");
    	require(gt.typ == 0, "not coin, but NFT");

    	addSupply(_symbol, _value);
    	addBalance(_to, _value, _symbol);
    }

    /**
        @notice Issue non-fungible token (nft).
        @param _to      Target address
        @param _symbol  Token symbol
        @param _uris    Universal resource identifier of tokens
    */
    function issueNFTs(address _to, string calldata _symbol, byte[256][] calldata _uris) external onlyIssuer(_symbol) {
    	gameAsset storage gt = assets[_symbol];
    	require(_uris.length > 0, "uris should not be empty");
    	require(_to != address(0x0), "_to must be non-zero");
    	require(gt.issuer != address(0x0), "asset not exist");
    	require(gt.typ == 1, "not nft, but coin");
    	for (uint256 i = 0; i < _uris.length; ++i) {
    		bytes memory b = new bytes(256);
    		for (uint256 j = 0; j < 256; ++j) {
    			b[j] = _uris[i][j];
    		}
    		string memory uri = Utils.bytesToString(b, b.length);
    		mint(_to, 0, _symbol, uri, byte(0), bytes32(0), bytes32(0));
    	}
    	uint256 quantity = _uris.length;
    	addSupply(_symbol, quantity);
    	addBalance(_to, quantity, _symbol);
    }

    /**
        @notice Append issuer public keys.
        @param _symbol      Token symbol
        @param _signers     Addresses corresponding to the nft's signature
    */
    function addSigners(string calldata _symbol, address[] calldata _signers) external onlyIssuer(_symbol) {
    	require(_signers.length > 0, "signer list should not be empty");
    	gameAsset storage gt = assets[_symbol];

    	for(uint256 i = 0; i < _signers.length; ++i) {
    		gt.signers.push(_signers[i]);
    	}
    }

    /**
        @notice Append signature of NFT for inter-contract transferring.
        @param _uuids   Batch of token global id
        @param v        Signature part V
        @param r        Signature part R
        @param s        Signature part S
    */
    function signNFTs( uint256[] calldata _uuids, byte[] calldata v, bytes32[] calldata r, bytes32[] calldata s) external {
    	require(_uuids.length > 0, "nft batch should not be empty");
    	for (uint256 i = 0; i < _uuids.length; ++i) {
    		gameNFT memory nft = NFTs[_uuids[i]];
    		gameAsset memory gt = assets[nft.symbol];
    		require(msg.sender == gt.issuer, "only asset issuer can do this action");
    		require(gt.signers.length > 0, "signer list is empty");
    		bool valid = false;
    		for (uint256 j = 0; j < gt.signers.length; ++j) {
    			if (addSign(gt.signers[j], nft.uuid, nft.symbol, v[i], r[i], s[i])) {
    				valid = true;
    				break;
    			}
    		}
    		require(valid, "invalid signature");
    	}
    }

    /**
        @notice Transfer fungible token (coin).
        @param _from    Source address
        @param _to      Target address
        @param _value   Transfer amount
        @param _symbol  Token symbol
        @param _data    Additional data
    */
    function transfer(address _from, address _to, uint256 _value, string calldata _symbol, bytes calldata _data) external {
    	require(_to != address(0x0), "_to must be non-zero");
    	require(_from == msg.sender || operatorApproval[_from][msg.sender] || operatorApprovalForCoin[_from][msg.sender][_symbol] >= _value,
    		"Need operator approval for 3rd party transfers");
    	require(_data.length <= 256, "memo should be less than 256 bytes");
    	subBalance(_from, _value, _symbol);
    	addBalance(_to, _value, _symbol);

    	emit TransferCoin(_from, _to, _value);
    }

    /**
        @notice Transfer batch non-fungible token (nft).
        @param _from    Source address
        @param _to      Target address
        @param _uuids   Global ID of tokens
        @param _data    Additional data
    */
    function transferNFTs(address _from, address _to, uint256[] calldata _uuids, bytes calldata _data) external {
    	require(_to != address(0x0), "_to must be non-zero");
    	require(_uuids.length > 0, "nft batch should not be empty");
    	require(_data.length <= 256, "memo should be less than 256 bytes");

    	for (uint256 i = 0; i < _uuids.length; ++i) {
    		uint256 uuid = _uuids[i];
    		require(
    			_from == msg.sender
    			|| operatorApproval[_from][msg.sender]
    			|| (operatorApprovalForUUID[_from][msg.sender][uuid] && uuid != 0),
    			"Need operator approval for 3rd party transfers");
    		gameNFT storage nft = NFTs[uuid];
    		require(nft.owner == _from, "nft not belongs to _from");
    		nft.owner = _to;
    		addBalance(_to, 1, nft.symbol);
    		subBalance(_from, 1, nft.symbol);

    		// update uuidByOwner
    		uint256[] storage ownerUUIDS = uuidByOwner[_from];
    		Utils.rmUUIDInArr(ownerUUIDS, uuid);
    		uuidByOwner[_from] = ownerUUIDS;
    		uuidByOwner[_to].push(uuid);

    		emit TransferNFT(_from, _to, uuid);
    	}
    }

    /**
        Get balance of a given token for a given address.
        @param _owner   Onwer of tokens @apram _symbol Token symbol
        @param _symbol  Token symbol
    */
    function balanceOf(address _owner, string calldata _symbol) external view returns (uint256) {
    	return balance[_owner][_symbol];
    }

    /**
        Get supply of a given token for a given symbol and external flag.
        @param _symbol  Token symbol
        @param _ext     External flag. `0` means tokens issued by self, `1` means tokens issued by other contracts.
     */
    function supplyOf(string calldata _symbol, uint8 _ext) external view returns(uint256) {
    	require(_ext == 0 || _ext == 1, "invalid `_ext` param");
    	if (_ext == 0) {
    		return assets[_symbol].supply;
    	}
    	else {
    		return extAssets[_symbol].supply;
    	}
    }

    /**
        Returns whether nft exist in contract or not.
        @param _uuid  Token global id
     */
    function nftExist(uint256 _uuid) external view returns(bool) {
    	return NFTs[_uuid].uuid != 0;
    }

    /**
        @notice Enable or disable approval for a third party ("operator") to manage the certain tokens.
        @param _operator    Address to add to the set of authorized operators
        @param _value       Token quantity
        @param _symbol      Token symbol
        @param _approved    True if the operator is approved, false to revoke approval
    */
    function approveCoin(address _operator, uint256 _value, string calldata _symbol, bool _approved) external {
    	uint256 oldVal = operatorApprovalForCoin[msg.sender][_operator][_symbol];
    	uint256 newVal = 0;
    	if (_approved) {
    		newVal = oldVal.add(_value);
    	}
    	else {
    		require(oldVal > _value, "approved value not enough");
    		newVal = oldVal.sub(_value);
    	}
    	operatorApprovalForCoin[msg.sender][_operator][_symbol] = newVal;

    	emit ApproveForCoin(msg.sender, _operator, _value, _symbol, _approved);
    }

    function addSupply(string memory _symbol, uint256 _value) internal view {
    	gameAsset memory gt = assets[_symbol];
    	require(gt.infinite || gt.supply + _value < gt.maxSupply, "supply more than maxSupply");
    	gt.supply = gt.supply.add(_value);
    }

    function addBalance(address _to, uint256 _value, string memory _symbol) internal {
    	balance[_to][_symbol] = balance[_to][_symbol].add(_value);
    }

    function subBalance(address _from, uint256 _value, string memory _symbol) internal {
    	require(balance[_from][_symbol] >= _value, "_from balance not enough");
    	balance[_from][_symbol] = balance[_from][_symbol].sub(_value);
    }

    function mint(address _owner, uint256 _uuid, string memory _symbol, string memory _uri, byte v, bytes32 r, bytes32 s) internal {
    	uint64 tid = _tokenId++;
    	uint256 uuid = _uuid;
    	if(uuid == 0){
            uuid = uint256(uint160(address(this))) << 96 | uint256(tid) << 32;
        }
        NFTs[uuid] = gameNFT({
        	uuid: uuid,
        	uri: _uri,
        	symbol: _symbol,
        	owner: _owner,
        	v: v,
        	r: r,
        	s: s
        });
        uuidBySymbol[_symbol].push(uuid);
        uuidByOwner[_owner].push(uuid);
    }

    function addSign(address _signer, uint256 _uuid, string memory _symbol, byte v, bytes32 r, bytes32 s) internal view returns(bool) {
    	gameNFT memory nft = NFTs[_uuid];
    	require(nft.symbol.toSlice().equals(_symbol.toSlice()), "tokne symbol not match");
    	if (Utils.verifyNftSign(_signer, _uuid, nft.uri, _symbol, v, r, s)) {
    		nft.v = v;
    		nft.r = r;
    		nft.s = s;
    		return true;
    	}
    	return false;
    }
}







