var WebSocket = require('ws');
var account1 = {
    address: '9DF68053BB24B86D814BC334C0032C795311FE88',
    publicKey:
   '045EE9F81142D2275E5866771E8F8BAABAE96FD13A8070E9BB35BD553C4EB33B05EAF66BB3974E45155352B48B2CFBBEA81A21D41C4FCAF492504998B4E9647B01',
   privateKey:
   '5695D1E8A9CDAE259BAFCE3714AAD120AE2370EE944168EBA3904BA6F0364B5F',
   privateKeyEncrypted: false
};

var account2 = {
    address: '930267C8A6FA041D5D0A6AC9C3DF25B77E8DC8E9',
    publicKey:
   '04491412FCDA40AF9A628100735D01BBAA047C1F6EFC19D1850BED56730406F7BD3FD08718A64FE2B648DE0BFBF9B480DA379278FB49237EFB6341CE83C5523249',
   privateKey:
   '4176AD970B1D2C3711739E0226C48F15EEF8644383129A870DA0E084CBCF31C0',
   privateKeyEncrypted: false
};

var account3 = {
    address: 'FCAC913D7FCC96CFC4CB0DBEF0888AF97A262A6C',
    publicKey:
   '048B227177CA5CCACBDA1F29CF71A3E2DAE339BDA0175E63E9F51B7F8A9F9F72ADFE266FF6E17618288435FEFA235214F063A0595CBD831BC6A9553416699C8271',
    privateKey:
   '00ECCAE0C9DC2822C7CF676739A060EFB486117BECAC94996F33408A8B916A0F91',
    privateKeyEncrypted: false
};

// var account4 = {
//     address: 'B706BF5D9822C71EDAF8810AEE551EBC2C1514A3',
//     publicKey:
//    '041BF6B31FC9A390C2BAF8CA93553BB3144A573408C9474B858AF9915255771B33F665DE05B6521957F74F4E95E6AB27100380B49D6FCF71535D837217B1A613D8',
//     privateKey:
//    '2656F67E2ACE04B2C4EBE1081A267B678FF1BFCC0CEF7C4DEFCBE40C745B5B5C',
//     privateKeyEncrypted: false
// };

var match1 = {
    match: true,
    address: account1['address'],
    signature: JSON.stringify(account1)
};
var match2 = {
    match: true,
    address: account2['address'],
    signature: JSON.stringify(account2)
};
var match3 = {
    match: true,
    address: account3['address'],
    signature: JSON.stringify(account3)
};
// var match4 = {
//     match: true,
//     address: account4['address'],
//     signature: JSON.stringify(account4)
// };

var websocket1 = null;
var websocket2 = null;
var websocket3 = null;
// var websocket4 = null;

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
websocket3 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
// websocket4 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");

websocket1.onerror = function() {
    console.log('account1 error');
}
websocket2.onerror = function() {
    console.log('account2 error');
}
websocket3.onerror = function() {
    console.log('account3 error');
}
// websocket4.onerror = function() {
//     console.log('account4 error');
// }

// websocket1.onopen = function() {
//     console.log('account1 connected');
// }
// websocket2.onopen = function() {
//     console.log('account2 connected');
// }
// websocket3.onopen = function() {
//     console.log('account3 connected');
// }
// websocket4.onopen = function() {
//     console.log('account4 connected');
// }
websocket1.on('open', function() {
    console.log('account1 connected');
    websocket1.send(JSON.stringify(match1));
});
websocket2.on('open', function() {
    console.log('account2 connected');
    websocket2.send(JSON.stringify(match2));
});
websocket3.on('open', function() {
    console.log('account3 connected');
    websocket3.send(JSON.stringify(match3));
});
// websocket4.on('open', function() {
//     console.log('account4 connected');
//     websocket4.send(JSON.stringify(match4));
// });

websocket1.onclose = function() {
    console.log('account1 offline');
}
websocket2.onclose = function() {
    console.log('account2 offline');
}
websocket3.onclose = function() {
    console.log('account3 offline');
}
// websocket4.onclose = function() {
//     console.log('account4 offline');
// }

websocket1.onmessage = function(event) {
    console.log('account1', event.data);
}
websocket2.onmessage = function(event) {
    console.log('account2', event.data);
}
websocket3.onmessage = function(event) {
    console.log('account3', event.data);
}
// websocket4.onmessage = function(event) {
//     console.log('account4', event.data);
// }


// websocket.onerror = function() {
//     console.log('error');
// };
// websocket.close();