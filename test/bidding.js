var WebSocket = require('ws');
var account1 = {
    address: 'AA805D8C9466787582A45DE9367273198793210A',
    publicKey:
   '0417B77764882A04C4CD1F90668ABD2A96E7B0D5BA59704AAB828BBBCCFDBC572A110BD815EF9AA1A2A5C1B6148E2E2F897E690F93A1E7BEF35489129DF741B8BB',
   privateKey:
   '67824260A47A80024E80ED72BA18FFD6502CAD2E9B8BA3693B5FE1C3C8B123DB',
   privateKeyEncrypted: false
};

var account2 = {
    address: 'DF395F014C02113D8D87BBEB916985A006FDB58B',
    publicKey:
   '040BDFF8EABA8CDA9A010CC39E07A6E8839586C73ADCE4F8ED2F940B6841BA331F5D5504A070F9BAB483B7E750753E03C101F62ED3525FBC9B1EB11CBEF3779DF7',
   privateKey:
   '00B5E3D608CCE35DD16E23DBADD3307BBDE50EAFBBF9D5AA43E2FFDF100CCB2465',
   privateKeyEncrypted: false
};

var account3 = {
    address: 'DA838155CB7ED94BBAEA6F05558498C4B04C6CA9',
    publicKey:
   '0416501C188C29D7CEDC866980392394C812DD9807C6369E78456EC293CDEC9C21A5977957D84C40655CEC19BD0D2F1C3A2324F23DFACE612BCD2DD1A03BC43B6A',
    privateKey:
   '00B7D149225FDA64C3BE77A9B03569FF728DE2D3D623617D9CCE42107EDB2203DA',
    privateKeyEncrypted: false
};

// var account4 = {
//     address: 'DB29A4D020DA75E3AE5BDCC2D843846D011E8A4E',
//     publicKey:
//    '044EE8C0A8BE9F1A457239C6A7962AC23A8C91FE2CA4704D35FF719E6788EA46B76213B83D20CD841EA1C8EAA6FC108CA92E09C022F8D65FCCB41B5EC5B3D98895',
//     privateKey:
//    '44CA4352307A62B3758249CD2A0AC50EFAD07080CDD7669C4E15C52DE7ED97F2',
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
var first3 = {
    first: true,
    address: account3['address']
};
// var first4 = {
//     first: true,
//     address: account4['address']
// };

var websocket1 = null;
var websocket2 = null;
var websocket3 = null;
// var websocket4 = null;
function sleep(ms) {
    var start = Date.now(), 
    now = start; 
    while (now - start < ms) { 
        now = Date.now(); 
    } 
}
// sleep(1000).then(() => {
//     websocket1 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
// });
// sleep(1000).then(() => {
//     websocket2 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
// });
// sleep(1000).then(() => {
//     websocket3 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
// });
// sleep(1000).then(() => {
//     websocket4 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
// });

// async function main() {
//     await sleep(1000);
//     websocket1 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
//     await sleep(1000);
//     websocket2 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
//     await sleep(1000);
//     websocket3 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
//     await sleep(1000);
//     websocket4 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
// }
// main();

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/bidding/72");
// sleep(1000);
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/bidding/72");
// sleep(1000);
websocket3 = new WebSocket("ws://localhost:8088/WebSocket/bidding/72");
// sleep(1000);
// websocket4 = new WebSocket("ws://localhost:8088/WebSocket/bidding/21");
// sleep(1000);

websocket1.onerror = function() {
    console.log('account1 error');
};
websocket2.onerror = function() {
    console.log('account2 error');
};
websocket3.onerror = function() {
    console.log('account3 error');
};
// websocket4.onerror = function() {
//     console.log('account4 error');
// };

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

websocket1.on('open', function() {
    console.log('account1 connected, time: ', new Date().toLocaleTimeString());
    websocket1.send(JSON.stringify(first1));
});
websocket2.on('open', function() {
    console.log('account2 connected, time: ', new Date().toLocaleTimeString());
    websocket2.send(JSON.stringify(first2));
});
websocket3.on('open', function() {
    console.log('account3 connected, time: ', new Date().toLocaleTimeString());
    websocket3.send(JSON.stringify(first3));
});
// websocket4.on('open', function() {
//     console.log('account4 connected, time: ', new Date().toLocaleTimeString());
//     websocket4.send(JSON.stringify(first4));
// });

// function a() {
//     console.log('account1 connected, time: ', new Date().toLocaleTimeString());
// }
// async function test() {
//     sleep(1000);
//     console.log('time: ', new Date().toLocaleTimeString());
//     websocket1.onopen = a;
//     // await websocket1.on('open', function() {
//     //     console.log('account1 connected, time: ', new Date().toLocaleTimeString());
//     //     websocket1.send(JSON.stringify(first1));
//     // });
//     // websocket1.on('open', a);
//     sleep(1000);
//     console.log('time: ', new Date().toLocaleTimeString());
//     websocket2.onopen = a;
//     // await websocket2.on('open', function() {
//     //     console.log('account2 connected, time: ', new Date().toLocaleTimeString());
//     //     websocket2.send(JSON.stringify(first2));
//     // });
//     // websocket2.on('open', a);
//     sleep(1000);
//     console.log('time: ', new Date().toLocaleTimeString());
//     websocket3.onopen = a;
//     // await websocket3.on('open', function() {
//     //     console.log('account3 connected, time: ', new Date().toLocaleTimeString());
//     //     websocket3.send(JSON.stringify(first3));
//     // });
//     // websocket3.on('open', a);
//     sleep(1000);
//     console.log('time: ', new Date().toLocaleTimeString());
//     websocket4.onopen = a;
//     // await websocket4.on('open', function() {
//     //     console.log('account4 connected, time: ', new Date().toLocaleTimeString());
//     //     websocket4.send(JSON.stringify(first4));
//     // });
//     // websocket4.on('open', a);
// }
// test();

// websocket1.onopen = function() {

// }
// websocket2.onopen = function() {

// }
// websocket3.onopen = function() {

// }
// websocket4.onopen = function() {

// }


websocket1.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account1: ', event.data, 'time: ', new Date().toLocaleTimeString());
    if (msg["stage"] === "bidding" && msg["round"] === 1 && msg["timer"] === 5) {
        // 第1轮剩余5秒
        websocket1.send(JSON.stringify({
            first: false,
            address: account1['address'],
            bidding: true, 
            price: 10.0
        }));
        // console.log("tttsssss");
    }
    if (msg["stage"] === "bidding" && msg["round"] === 5 && msg["timer"] === 2) {
        // 第5轮剩余2秒
        websocket1.send(JSON.stringify({
            first: false,
            address: account1['address'],
            bidding: true, 
            price: 14.0
        }));
    }
    if (msg["stage"] === "pay" && msg["timer"] === 10 && msg["deal"] > 0) {
        websocket1.send(JSON.stringify({
            first: false,
            address: account1["address"],
            bidding: false,
            price: msg["deal"],
            signature: JSON.stringify(account1)
        }));
    }
}
websocket2.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account2: ', event.data, 'time: ', new Date().toLocaleTimeString());
    if (msg["stage"] === "bidding" && msg["round"] === 3 && msg["timer"] === 6) {
        // 第3轮剩余6秒
        websocket1.send(JSON.stringify({
            first: false,
            address: account2['address'],
            bidding: true, 
            price: 12.8
        }));
    }
    if (msg["stage"] === "pay" && msg["timer"] === 10 && msg["deal"] > 0) {
        websocket2.send(JSON.stringify({
            first: false,
            address: account2["address"],
            bidding: false,
            price: msg["deal"],
            signature: JSON.stringify(account2)
        }));
    }
}
websocket3.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account3: ', event.data, 'time: ', new Date().toLocaleTimeString());
    if (msg["stage"] === "bidding" && msg["round"] === 3 && msg["timer"] === 6) {
        // 第3轮剩余6秒
        websocket1.send(JSON.stringify({
            first: false,
            address: account3['address'],
            bidding: true, 
            price: 12.8
        }));
    }
    if (msg["stage"] === "pay" && msg["timer"] === 10 && msg["deal"] > 0) {
        websocket3.send(JSON.stringify({
            first: false,
            address: account3["address"],
            bidding: false,
            price: msg["deal"],
            signature: JSON.stringify(account3)
        }));
    }
}
// websocket4.onmessage = function(event) {
//     var msg = JSON.parse(event.data);
//     console.log('account4: ', event.data, 'time: ', new Date().toLocaleTimeString());
//     if (msg["stage"] === "bidding" && msg["round"] === 4 && msg["timer"] === 6) {
//         // 第4轮剩余6秒
//         websocket1.send(JSON.stringify({
//             first: false,
//             address: account4['address'],
//             bidding: true, 
//             price: 13.89
//         }));
//     }
//     if (msg["stage"] === "pay" && msg["timer"] === 1 && msg["deal"] > 0) {
//         websocket4.send(JSON.stringify({
//             first: false,
//             address: account4["address"],
//             bidding: false,
//             price: msg["deal"],
//             signature: JSON.stringify(account4)
//         }));
//     }
// }