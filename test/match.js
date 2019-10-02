var WebSocket = require('ws');
var account1 = {
    address: '10176C670C1A92104FA1EC98B9E29F5721D25642',
    publicKey:
   '04F9AC133FF89DF907E145CC16693D754347ECE4FFE3B365427D52F3E10F05E6CFFF17BFE91F320F3F436F34C0590501F92483A0BBF71DEF8E9DB40F31EF105472',
   privateKey:
   '334975E21556D5851131F3E9C7BA2A55A822218F0D6D4B3D5F072B820B832D54',
   privateKeyEncrypted: false
};

var account2 = {
    address: 'BCC935EA0C5A3E39509A641B688E44742CE9FE9A',
    publicKey:
   '0492CD5ECB3DB6CF2EFACE1E19A3E69BAAB3B28881FD1EFF84E95508FBCEF17BBCD8279A24EEEB02F7013CF602234D102E1C0687435F598265ED28D1E6A373D98B',
   privateKey:
   '0081E2123850888F9ED4BFD0D2716F12BAAA9EC9CA625EBCE0F230DCBC23528169',
   privateKeyEncrypted: false
};

var account3 = {
    address: '1DBE0A1322FC34636EF04EA96EA1BF192ED3278C',
    publicKey:
   '04DAA88C4EA9DEA3EBFA6720DE9318D1EEFD35987E0AFE637CEFDA062492AAD0CAC4D7B08159CF12166E984DA001F933DDE5FEEEEEE182F6AAB6BA4CE0E51406A6',
    privateKey:
   '5EF9AF56B6091EAB0919259C241D88D8752FAEEE6023EF15A1D73B4899B8635E',
    privateKeyEncrypted: false
};

var account4 = {
    address: 'B706BF5D9822C71EDAF8810AEE551EBC2C1514A3',
    publicKey:
   '041BF6B31FC9A390C2BAF8CA93553BB3144A573408C9474B858AF9915255771B33F665DE05B6521957F74F4E95E6AB27100380B49D6FCF71535D837217B1A613D8',
    privateKey:
   '2656F67E2ACE04B2C4EBE1081A267B678FF1BFCC0CEF7C4DEFCBE40C745B5B5C',
    privateKeyEncrypted: false
};

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
var match4 = {
    match: true,
    address: account4['address'],
    signature: JSON.stringify(account4)
};

var websocket1 = null;
var websocket2 = null;
var websocket3 = null;
var websocket4 = null;

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
websocket3 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
websocket4 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");

websocket1.onerror = function() {
    console.log('account1 error');
}
websocket2.onerror = function() {
    console.log('account2 error');
}
websocket3.onerror = function() {
    console.log('account3 error');
}
websocket4.onerror = function() {
    console.log('account4 error');
}

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
websocket4.on('open', function() {
    console.log('account4 connected');
    websocket4.send(JSON.stringify(match4));
});

websocket1.onclose = function() {
    console.log('account1 offline');
}
websocket2.onclose = function() {
    console.log('account2 offline');
}
websocket3.onclose = function() {
    console.log('account3 offline');
}
websocket4.onclose = function() {
    console.log('account4 offline');
}

websocket1.onmessage = function(event) {
    console.log('account1', event.data);
}
websocket2.onmessage = function(event) {
    console.log('account2', event.data);
}
websocket3.onmessage = function(event) {
    console.log('account3', event.data);
}
websocket4.onmessage = function(event) {
    console.log('account4', event.data);
}


// websocket.onerror = function() {
//     console.log('error');
// };
// websocket.close();