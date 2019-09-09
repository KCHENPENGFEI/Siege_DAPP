pragma solidity ^0.5.0;

interface InterfaceAsset {

    event TransferCoin(address indexed _from, address indexed _to, uint256 _value);
    event TransferNFT(address indexed _from, address indexed _to, uint256 _uuid);
    event ApproveForAll(address indexed _owner, address indexed _operator, bool _approved);
    event ApproveForCoin(address indexed _owner, address indexed _operator, uint256 _value, string _symbol, bool _approved);
    event ApproveForNFTs(address indexed _owner, address indexed _operator, uint256[] _uuids, bool[] _approved);


    /**
        @notice Create a fungible(coin) or non-fungible(nft) token type.
        @param _issuer  Issuer address
        @param _value   Token amount
        @param _symbol  Token symbol
        @param _type    Token type `0` Coin; `1` NFT
    */
    function create(address _issuer, uint256 _value, string calldata _symbol, uint8 _type) external;

    /**
        @notice Issue fungible token (coin).
        @param _to      Target address
        @param _value   Token amount
        @param _symbol  Token symbol
    */
    function issueCoin(address _to, uint256 _value, string calldata _symbol) external;

    /**
        @notice Issue non-fungible token (nft).
        @param _to      Target address
        @param _symbol  Token symbol
        @param _uris    Universal resource identifier of tokens
    */
    function issueNFTs(address _to, string calldata _symbol, byte[256][] calldata _uris) external;

    /**
        @notice Append issuer public keys.
        @param _symbol      Token symbol
        @param _signers     Addresses corresponding to the nft's signature
    */
    function addSigners(string calldata _symbol, address[] calldata _signers) external;

    /**
        @notice Append signature of NFT for inter-contract transferring.
        @param _uuids   Batch of token global id
        @param v        Signature part V
        @param r        Signature part R
        @param s        Signature part S
    */
    function signNFTs( uint256[] calldata _uuids, byte[] calldata v, bytes32[] calldata r, bytes32[] calldata s) external;

    /**
        @notice Transfer fungible token (coin).
        @param _from    Source address
        @param _to      Target address
        @param _value   Transfer amount
        @param _symbol  Token symbol
        @param _data    Additional data
    */
    function transfer(address _from, address _to, uint256 _value, string calldata _symbol, bytes calldata _data) external;

    /**
        @notice Transfer batch non-fungible token (nft).
        @param _from    Source address
        @param _to      Target address
        @param _uuids   Global ID of tokens
        @param _data    Additional data
    */
    function transferNFTs(address _from, address _to, uint256[] calldata _uuids, bytes calldata _data) external;

    /**
        @notice Enable or disable approval for a third party ("operator") to manage the certain tokens.
        @param _operator    Address to add to the set of authorized operators
        @param _value       Token quantity
        @param _symbol      Token symbol
        @param _approved    True if the operator is approved, false to revoke approval
    */
    // function approveCoin(address _operator, uint256 _value, string calldata _symbol, bool _approved) external;

    /**
        @notice Enable or disable approval for a third party ("operator") to manage the certain tokens.
        @param _operator    Address to add to the set of authorized operators
        @param _uuids       Global ID of tokens
        @param _approved    Flag list. True if the operator is approved, false to revoke approval
    */
    // function approveNFTs(address _operator, uint256[] calldata _uuids, bool[] calldata _approved) external;

    /**
        Enable or disable approval for a third party ("operator") to manage all of caller's tokens.
        @param _operator Address to add to the set of authorized operators
        @param _approved True if the operator is approved, false to revoke approval
    */
    // function approve(address _operator, bool _approved) external;

    /**
        Queries the approval status of an operator for a given owner and uuids.
        @param _owner       Owner of tokens
        @param _operator    Address to add to the set of authorized operators
        @return             True if the operator is approved, false if not
    */
    // function isApprovedForNFTs(address _owner, address _operator, uint256[] calldata _uuid) external returns (bool);

    /**
        Queries the approval status of an operator for a given owner and uuids.
        @param _owner       Owner of tokens
        @param _operator    Address to add to the set of authorized operators
        @param _symbol      Token symbol
        @return             True if the operator is approved, false if not
    */
    // function isApprovedForCoin(address _owner, address _operator, string calldata _symbol) external returns(bool);
    /**
        Queries the approval status of an operator for a given owner.
        @param _owner       The owner of the tokens
        @param _operator    Address of authorized operator
        @return             True if the operator is approved, false if not
    */
    // function isApprovedForAll(address _owner, address _operator) external view returns (bool);

    /** Getter interface */

    /**
        Get balance of a given token for a given address.
        @param _owner   Onwer of tokens @apram _symbol Token symbol
        @param _symbol  Token symbol
    */
    function balanceOf(address _owner, string calldata _symbol) external view returns (uint256);

    /**
        Get supply of a given token for a given symbol and external flag.
        @param _symbol  Token symbol
        @param _ext     External flag. `0` means tokens issued by self, `1` means tokens issued by other contracts.
     */
    function supplyOf(string calldata _symbol, uint8 _ext) external view returns(uint256);

    /**
        Returns whether nft exist in contract or not.
        @param _uuid  Token global id
     */
    function nftExist(uint256 _uuid) external view returns(bool);
}
