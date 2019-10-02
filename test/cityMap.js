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
var first4 = {
    first: true,
    address: account4['address']
};

var websocket1 = null;
var websocket2 = null;
var websocket3 = null;
var websocket4 = null;
function sleep(ms) {
    var start = Date.now(), 
    now = start; 
    while (now - start < ms) { 
        now = Date.now(); 
    } 
}

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/21");
// sleep(1000);
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/21");
// sleep(1000);
websocket3 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/21");
// sleep(1000);
websocket4 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/21");
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
var data4 = {
    address: account4['address'],
    cityId: 2,
    timer: 0,
    bonus: 0,
    rate: 0
};
websocket1.onerror = function() {
    console.log('account1 error');
};
websocket2.onerror = function() {
    console.log('account2 error');
};
websocket3.onerror = function() {
    console.log('account3 error');
};
websocket4.onerror = function() {
    console.log('account4 error');
};

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
websocket4.on('open', function() {
    console.log('account4 connected, time: ', new Date().toLocaleTimeString());
    websocket4.send(JSON.stringify(first4));
});

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
websocket4.onmessage = function(event) {
    // 玩家4有城池
    var msg = JSON.parse(event.data);
    console.log('account4: ', msg, 'time: ', new Date().toLocaleTimeString());
    if (msg["stage"] === 'running') {
        data4["timer"] = msg["timer"];
    }
    if (msg["cityBonus"] !== undefined) {
        data4["bonus"] = msg["cityBonus"][3]["producedBonus"];
    }
    if (msg["produceRate"] !== undefined) {
        data4["rate"] = msg["produceRate"];
    }
    // 在特定时间离开城池2
    // if (msg["timer"] === 3585) {
    //     websocket4.send(JSON.stringify({
    //         first: false,
    //         address: account4['address'],
    //         operation: 'leave',
    //         cityId: data4.cityId,
    //         bonus: data4.bonus
    //     }))
    // }
    // 在特定时间去攻击玩家2，城池3
    if (msg["timer"] === 3594) {
        websocket4.send(JSON.stringify({
            first: false,
            address: account4["address"],
            operation: 'attack',
            cityId: 3,
            target: account2["address"]
        }));
    }
}