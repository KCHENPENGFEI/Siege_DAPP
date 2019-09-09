pragma solidity ^0.5.0;

import "./strings.sol";

library Utils{

    using strings for *;

    // function bytesToString(bytes memory x, uint length) public pure returns (string memory) {
    //     bytes memory str = new bytes(length);
    //     for(uint i = 0; i<length;i++){
    //         str[i] = x[i];
    //     }
    //     return string(str);
    // }

    function substring(string memory str, uint startIndex, uint endIndex)
        public 
        pure 
        returns (string memory) 
    {
        bytes memory strBytes = bytes(str);
        bytes memory result = new bytes(endIndex-startIndex);
        for(uint i = startIndex; i < endIndex; i++) {
            result[i-startIndex] = strBytes[i];
        }
        return string(result);
    }

    function bytesToString(bytes memory x, uint length) public pure returns (string memory) {
        bytes memory bytesString = new bytes(length);
        uint charCount = 0;
        for (uint j = 0; j < 32; j++) {
            byte char = x[j];
            if (char != 0) {
                bytesString[charCount] = char;
                charCount++;
            }
        }
        bytes memory bytesStringTrimmed = new bytes(charCount);
        for (uint j = 0; j < charCount; j++) {
            bytesStringTrimmed[j] = bytesString[j];
        }
        return string(bytesStringTrimmed);
    }

    function bytes32ToString(bytes32 x) 
        public
        pure 
        returns (string memory) 
    {
        bytes memory b = new bytes(32);
        for(uint i=0;i<32;i++){
                b[i] = x[i];
        }
        return bytesToString(b,32);
    }

    function bytes256ToString(byte[256] memory x) 
        public
        pure 
        returns (string memory) 
    {
        bytes memory b = new bytes(256);
        for(uint i=0;i<256;i++){
                b[i] = x[i];
        }
        return bytesToString(b,256);
    }

    function stringToBytes32(string memory source) public pure returns (bytes32 result)  {
        bytes memory tempEmptyStringTest = bytes(source);
        if (tempEmptyStringTest.length == 0) {
            return 0x0;
        }
        assembly {
            result := mload(add(source, 32))
        }
    }

    function stringToBytes256(string memory source) 
        public
        pure
        returns (byte[256] memory result) 
    {
        uint length = bytes(source).length;
        for(uint i=0;i<length/32;i++){
            bytes32 b32 = stringToBytes32(substring(source, i*8, i*8+32));
            for(uint j=0;j<32;j++){
                result[i*32+j] = b32[j];
            }
        }
        bytes32 b32 = stringToBytes32(substring(source, length/32*32, length));
        uint i=0;
        for(uint k=length/32*32; k<length; k++){
                result[k] = b32[i];
                i++;
        }
    }


    function verifyNftSign(
        address _signer,
        uint256 _uuid, 
        string memory _uri,
        string memory _symbol,
        byte v, bytes32 r, bytes32 s
        )
        public pure returns (bool){
        bytes32 params = keccak256(abi.encodePacked(_uuid, _uri, _symbol));
        string memory prefix = "\x19Ethereum Signed Message:\n32";
        address rec = ecrecover(keccak256(abi.encodePacked(prefix, params)), uint8(v), r, s);
        return rec == _signer;
    }

    function emptySign(byte v, bytes32 r, bytes32 s) public pure returns(bool){
        return v == byte(0) && r == bytes32(0) && s == bytes32(0);
    }

    function rmUUIDInArr(uint256[] storage array, uint256 el) public returns(uint256[] memory) {
        bool flag = false;
        for (uint i = 0; i< array.length; i++){
            if(array[i]==el){
                flag = true;
            }
            if(flag && i != array.length - 1){
                array[i] = array[i+1];
            }
        }
        if(flag){
            delete array[array.length-1];
            array.length--;
        }
        return array;
    }

    /**
     * Converts an unsigned integer to the ASCII string equivalent value
     * 
     * @param _base The unsigned integer to be converted to a string
     * @return string The resulting ASCII string value
     */
    function uintToString(uint _base)
        public
        pure
        returns (string memory) {
        uint base = _base;
        bytes memory _tmp = new bytes(32);
        uint i;
        for(i = 0;base > 0;i++) {
            _tmp[i] = byte(uint8((base % 10) + 48));
            base /= 10;
        }
        bytes memory _real = new bytes(i--);
        for(uint j = 0; j < _real.length; j++) {
            _real[j] = _tmp[i--];
        }
        return string(_real);
    }
}