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

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/31");
// sleep(1000);
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/31");
// sleep(1000);
websocket3 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/31");
// sleep(1000);
// websocket4 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/22");
// sleep(1000);

var data1 = {
    address: account1['address'],
    cityId: 1,
    timer: 0,
    bonus: 0,
    rate: 0
};
var data2 = {
    address: account2['address'],
    cityId: 3,
    timer: 0,
    bonus: 0,
    rate: 0
};
var data3 = {
    address: account3['address'],
    cityId: 0,
    timer: 0,
    bonus: 0,
    rate: 0
};
// var data4 = {
//     address: account4['address'],
//     cityId: 2,
//     timer: 0,
//     bonus: 0,
//     rate: 0
// };
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

websocket1.onmessage = function(event) {
    // 玩家1有城池
    var msg = JSON.parse(event.data);
    console.log('account1: ', msg, 'time: ', new Date().toLocaleTimeString());
    if (msg["stage"] === 'running') {
        data1["timer"] = msg["timer"];
    }
    if (msg["cityBonus"] !== undefined) {
        data1["bonus"] = msg["cityBonus"][3]["producedBonus"];
    }
    if (msg["produceRate"] !== undefined) {
        data1["rate"] = msg["produceRate"];
    }
    // 在特定时间去占领城池4
    if (msg['timer'] === 3597) {
        websocket1.send(JSON.stringify({
            first: false,
            address: account1['address'],
            operation: 'occupy',
            cityId: 4,
            price: 6.0,
            signature: JSON.stringify(account1)
        }));
    }
    // 在特定时间去攻击玩家3，城池4
    // if (msg["timer"] === 3580) {
    //     websocket1.send(JSON.stringify({
    //         first: false,
    //         address: account1["address"],
    //         operation: 'attack',
    //         cityId: 4,
    //         target: account3['address']
    //     }));
    // }
    // 在特定时间离开城池4
    // if (msg["timer"] === 3593) {
    //     websocket1.send(JSON.stringify({
    //         first: false,
    //         address: account1['address'],
    //         operation: 'leave',
    //         cityId: 4,
    //         bonus: data1.bonus
    //     }))
    // }
    // 被玩家3攻击，60s内做出相应
    if (msg["timer"] === 3592) {
        // 离开
        // websocket1.send(JSON.stringify({
        //     first: false,
        //     address: account1["address"],
        //     operation: 'defense',
        //     cityId: 4,
        //     target: account3["address"],
        //     choice: 0,
        //     bonus: data1.bonus
        // }));
        // 防御
        websocket1.send(JSON.stringify({
            first: false,
            address: account1['address'],
            operation: 'defense',
            cityId: 4,
            target: account3["address"],
            choice: 1,
            bonus: data1.bonus
        }));
    }
}
websocket2.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account2: ', msg, 'time: ', new Date().toLocaleTimeString());
    if (msg["stage"] === 'running') {
        data2["timer"] = msg["timer"];
    }
    if (msg["cityBonus"] !== undefined) {
        data2["bonus"] = msg["cityBonus"][2]["producedBonus"];
    }
    if (msg["produceRate"] !== undefined) {
        data2["rate"] = msg["produceRate"];
    }
    // 在特定时间去占领城池3
    if (msg['timer'] === 3598) {
        websocket2.send(JSON.stringify({
            first: false,
            address: account2['address'],
            operation: 'occupy',
            cityId: 3,
            price: 6.0,
            signature: JSON.stringify(account2)
        }));
    }
    // console.log(data2);
    // 在特定时间离开城池3
    // if (msg["timer"] === 3595) {
    //     websocket2.send(JSON.stringify({
    //         first: false,
    //         address: account2['address'],
    //         operation: 'leave',
    //         cityId: 3,
    //         bonus: data2.bonus
    //     }))
    // }
    // 在特定时间进行防御
    if (msg["timer"] === 3590) {
        websocket2.send(JSON.stringify({
            first: false,
            address: account2["address"],
            operation: 'defense',
            cityId: 3,
            target: account4['address'],
            choice: 1,
            bonus: data2.bonus
        }));
    }
}
websocket3.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account3: ', msg, 'time: ', new Date().toLocaleTimeString());
    if (msg["stage"] === 'running') {
        data3["timer"] = msg["timer"];
    }
    if (msg["cityBonus"] !== undefined) {
        data3["bonus"] = msg["cityBonus"][2]["producedBonus"];
    }
    if (msg["produceRate"] !== undefined) {
        data3["rate"] = msg["produceRate"];
    }
    // 在特定时间去攻击玩家1，城池4
    if (msg["timer"] === 3595) {
        websocket3.send(JSON.stringify({
            first: false,
            address: account3["address"],
            operation: 'attack',
            cityId: 4,
            target: account1['address']
        }));
    }
    // 被玩家1攻击，60s内做出相应
    // if (msg["timer"] === 3575) {
    //     // 离开
    //     websocket3.send(JSON.stringify({
    //         first: false,
    //         address: account3["address"],
    //         operation: 'defense',
    //         cityId: 4,
    //         target: account1["address"],
    //         choice: 0,
    //         bonus: data1.bonus
    //     }));
    // }
}
// websocket4.onmessage = function(event) {
//     // 玩家4有城池
//     var msg = JSON.parse(event.data);
//     console.log('account4: ', msg, 'time: ', new Date().toLocaleTimeString());
//     if (msg["stage"] === 'running') {
//         data4["timer"] = msg["timer"];
//     }
//     if (msg["cityBonus"] !== undefined) {
//         data4["bonus"] = msg["cityBonus"][3]["producedBonus"];
//     }
//     if (msg["produceRate"] !== undefined) {
//         data4["rate"] = msg["produceRate"];
//     }
//     // 在特定时间离开城池2
//     // if (msg["timer"] === 3585) {
//     //     websocket4.send(JSON.stringify({
//     //         first: false,
//     //         address: account4['address'],
//     //         operation: 'leave',
//     //         cityId: data4.cityId,
//     //         bonus: data4.bonus
//     //     }))
//     // }
//     // 在特定时间去攻击玩家2，城池3
//     if (msg["timer"] === 3594) {
//         websocket4.send(JSON.stringify({
//             first: false,
//             address: account4["address"],
//             operation: 'attack',
//             cityId: 3,
//             target: account2["address"]
//         }));
//     }
// }