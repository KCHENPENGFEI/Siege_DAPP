var WebSocket = require('ws');
var account1 = {
    address: '67596A6EEBD01F11D23423D78C3A863D715B92D4',
    publicKey:
   '04BDD6D20F5E4EF09E9AA21F432A019A9D8B6145DFC92899FCE1FBEA2E13F3425174F3C5EA732F6829F3CDEABD58E9BDCAF5AF845683F22826B9DB81CB01518EDD',
   privateKey:
   '2F076999C1DB8E8C4F062D59F070FDFEBF9C279B42966C9781C3B4CF5B9BACFD',
   privateKeyEncrypted: false
};

var account2 = {
    address: '604ADD4882C08C9FAA36710DE893C29EFC8C5A1C',
    publicKey:
   '041ED14433974D3F57A509CA0D5DDEBE6C05A85597045A41C346C291C265AADBFE688CA9013875EC89F84B98986A72F32A8BD02ECC8374A6E70AE57531B8089588',
   privateKey:
   '49CEEB0F916665F3638EBD8997C7F96C23CC455A1E3F2A76941A3844FCB76558',
   privateKeyEncrypted: false
};

// var account3 = {
//     address: 'DA838155CB7ED94BBAEA6F05558498C4B04C6CA9',
//     publicKey:
//    '0416501C188C29D7CEDC866980392394C812DD9807C6369E78456EC293CDEC9C21A5977957D84C40655CEC19BD0D2F1C3A2324F23DFACE612BCD2DD1A03BC43B6A',
//     privateKey:
//    '00B7D149225FDA64C3BE77A9B03569FF728DE2D3D623617D9CCE42107EDB2203DA',
//     privateKeyEncrypted: false
// };

// var account4 = {
//     address: 'DB29A4D020DA75E3AE5BDCC2D843846D011E8A4E',
//     publicKey:
//    '044EE8C0A8BE9F1A457239C6A7962AC23A8C91FE2CA4704D35FF719E6788EA46B76213B83D20CD841EA1C8EAA6FC108CA92E09C022F8D65FCCB41B5EC5B3D98895',
//     privateKey:
//    '44CA4352307A62B3758249CD2A0AC50EFAD07080CDD7669C4E15C52DE7ED97F2',
//     privateKeyEncrypted: false
// };

// var account5 = {
//     address: '4AA62B307B89BED2A713ECAF57A494F81AEC8BDE',
//     publicKey:
//    '04C6267D97B66897B6EB82166234DF1F73F6E5E08489EAC8716BD2A01B9200D6FAA12EA498652C1F2A7C861918A2DC816BFA0FEE930D99E2F79635EC988F1616DD',
//     privateKey:
//    '1D212EF65B79423E6F7B21D2488FCB3BD8AD34C7FB476F87E00278C53020DCD9',
//     privateKeyEncrypted: false
// };

var first1 = {
    first: true,
    address: account1['address']
};
var first2 = {
    first: true,
    address: account2['address']
};
// var first3 = {
//     first: true,
//     address: account3['address']
// };
// var first4 = {
//     first: true,
//     address: account4['address']
// };
// var first5 = {
//     first: true,
//     address: account5['address']
// };

var match1 = {
    first: false,
    match: true,
    address: account1['address'],
    signature: JSON.stringify(account1)
};
var match2 = {
    first: false,
    match: true,
    address: account2['address'],
    signature: JSON.stringify(account2)
};
// var match3 = {
//     first: false,
//     match: true,
//     address: account3['address'],
//     signature: JSON.stringify(account3)
// };
// var match4 = {
//     first: false,
//     match: true,
//     address: account4['address'],
//     signature: JSON.stringify(account4)
// };
// var match5 = {
//     first: false,
//     match: true,
//     address: account5['address'],
//     signature: JSON.stringify(account5)
// };

var websocket1 = null;
var websocket2 = null;
var websocket3 = null;
var websocket4 = null;
var websocket5 = null;

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
// websocket3 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
// websocket4 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");
// websocket5 = new WebSocket("ws://localhost:8088/WebSocket/playersMatch");

websocket1.onerror = function() {
    console.log('account1 error');
}
websocket2.onerror = function() {
    console.log('account2 error');
}
// websocket3.onerror = function() {
//     console.log('account3 error');
// }
// websocket4.onerror = function() {
//     console.log('account4 error');
// }
// websocket5.onerror = function() {
//     console.log('account5 error');
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
// websocket3.on('open', function() {
//     console.log('account3 connected');
//     websocket3.send(JSON.stringify(first3));
// });
// websocket4.on('open', function() {
//     console.log('account4 connected');
//     websocket4.send(JSON.stringify(first4));
// });
// websocket5.on('open', function() {
//     console.log('account5 connected');
//     websocket5.send(JSON.stringify(first5));
// });

websocket1.onclose = function() {
    console.log('account1 offline');
}
websocket2.onclose = function() {
    console.log('account2 offline');
}
// websocket3.onclose = function() {
//     console.log('account3 offline');
// }
// websocket4.onclose = function() {
//     console.log('account4 offline');
// }
// websocket5.onclose = function() {
//     console.log('account5 offline');
// }

websocket1.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account1: ', event.data, 'time: ', new Date().toLocaleTimeString());
    // if (msg['stage'] === 'startGame' && msg['gameId'] === 0) {
    //     websocket1.send(JSON.stringify(match1));
    // }
}
websocket2.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account2: ', event.data, 'time: ', new Date().toLocaleTimeString());
    // if (msg['stage'] === 'startGame' && msg['gameId'] === 0) {
    //     websocket2.send(JSON.stringify(match2));
    // }
}
// websocket3.onmessage = function(event) {
//     var msg = JSON.parse(event.data);
//     console.log('account3: ', event.data, 'time: ', new Date().toLocaleTimeString());
//     if (msg['stage'] === 'startGame' && msg['gameId'] === 0) {
//         websocket3.send(JSON.stringify(match3));
//     }
// }
// websocket4.onmessage = function(event) {
//     var msg = JSON.parse(event.data);
//     console.log('account4: ', event.data, 'time: ', new Date().toLocaleTimeString());
//     if (msg['stage'] === 'startGame' && msg['gameId'] === 0) {
//         websocket4.send(JSON.stringify(match4));
//     }
// }
// websocket5.onmessage = function(event) {
//     var msg = JSON.parse(event.data);
//     console.log('account5: ', event.data, 'time: ', new Date().toLocaleTimeString());
//     // if (msg['stage'] === 'startGame' && msg['gameId'] === 0) {
//     //     websocket5.send(JSON.stringify(match5));
//     // }
// }


// websocket.onerror = function() {
//     console.log('error');
// };
// websocket.close();