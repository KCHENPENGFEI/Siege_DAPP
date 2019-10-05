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

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/battle/21/1DBE0A1322FC34636EF04EA96EA1BF192ED3278C&&10176C670C1A92104FA1EC98B9E29F5721D25642&&4");
// sleep(1000);
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/battle/21/B706BF5D9822C71EDAF8810AEE551EBC2C1514A3&&BCC935EA0C5A3E39509A641B688E44742CE9FE9A&&3");
// sleep(1000);
websocket3 = new WebSocket("ws://localhost:8088/WebSocket/battle/21/1DBE0A1322FC34636EF04EA96EA1BF192ED3278C&&10176C670C1A92104FA1EC98B9E29F5721D25642&&4");
// sleep(1000);
websocket4 = new WebSocket("ws://localhost:8088/WebSocket/battle/21/B706BF5D9822C71EDAF8810AEE551EBC2C1514A3&&BCC935EA0C5A3E39509A641B688E44742CE9FE9A&&3");
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
    var msg = JSON.parse(event.data);
    console.log('account1: ', msg, 'time: ', new Date().toLocaleTimeString());
    // 在特定时间买士兵3
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 110) {
        websocket1.send(JSON.stringify({
            first: false,
            address: account1["address"],
            operation: 'buySoldiers',
            type: [3],
            price: 20,
            quantity: 1,
            signature: JSON.stringify(account1)
        }));
    }
    // 在特定时间继续购买士兵1，5，2，1, 2
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 105) {
        websocket1.send(JSON.stringify({
            first: false,
            address: account1["address"],
            operation: 'buySoldiers',
            type: [1, 5, 1, 2, 2],
            price: 80,
            quantity: 5,
            signature: JSON.stringify(account1)
        }));
    }
    // 在特定时间departure
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 100) {
        websocket1.send(JSON.stringify({
            first: false,
            address: account1["address"],
            operation: 'departure',
            type: [3, 1, 5, 1, 2, 2],
            price: 100,
            quantity: 6
        }));
    }
    // 出牌
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 1 && msg["timer"] === 18) {
            websocket1.send(JSON.stringify({
                first: false,
                address: account1["address"],
                operation: 'pickSoldier',
                soldier: 3
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 2 && msg["timer"] === 18) {
            websocket1.send(JSON.stringify({
                first: false,
                address: account1["address"],
                operation: 'pickSoldier',
                soldier: 5
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 3 && msg["timer"] === 18) {
            websocket1.send(JSON.stringify({
                first: false,
                address: account1["address"],
                operation: 'pickSoldier',
                soldier: 2
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 4 && msg["timer"] === 18) {
            websocket1.send(JSON.stringify({
                first: false,
                address: account1["address"],
                operation: 'pickSoldier',
                soldier: 1
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 5 && msg["timer"] === 18) {
            websocket1.send(JSON.stringify({
                first: false,
                address: account1["address"],
                operation: 'pickSoldier',
                soldier: 2
            }));
        }
    }
}
websocket2.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account2: ', msg, 'time: ', new Date().toLocaleTimeString());
    // 在特定时间买士兵1
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 113) {
        websocket2.send(JSON.stringify({
            first: false,
            address: account2["address"],
            operation: 'buySoldiers',
            type: [2],
            price: 15,
            quantity: 1,
            signature: JSON.stringify(account2)
        }));
    }
    // 在特定时间继续购买士兵2, 2, 5, 4
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 109) {
        websocket2.send(JSON.stringify({
            first: false,
            address: account2["address"],
            operation: 'buySoldiers',
            type: [2, 1, 5, 4],
            price: 80,
            quantity: 4,
            signature: JSON.stringify(account2)
        }));
    }
    // 在特定时间departure
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 93) {
        websocket2.send(JSON.stringify({
            first: false,
            address: account2["address"],
            operation: 'departure',
            type: [2, 1, 2, 5, 4],
            price: 95,
            quantity: 5
        }));
    }
    // 出牌
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 1 && msg["timer"] === 15) {
            websocket2.send(JSON.stringify({
                first: false,
                address: account2["address"],
                operation: 'pickSoldier',
                soldier: 4
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 2 && msg["timer"] === 15) {
            websocket2.send(JSON.stringify({
                first: false,
                address: account2["address"],
                operation: 'pickSoldier',
                soldier: 5
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 3 && msg["timer"] === 15) {
            websocket2.send(JSON.stringify({
                first: false,
                address: account2["address"],
                operation: 'pickSoldier',
                soldier: 1
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 4 && msg["timer"] === 15) {
            websocket2.send(JSON.stringify({
                first: false,
                address: account2["address"],
                operation: 'pickSoldier',
                soldier: 2
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 5 && msg["timer"] === 15) {
            websocket2.send(JSON.stringify({
                first: false,
                address: account2["address"],
                operation: 'pickSoldier',
                soldier: 2
            }));
        }
    }
}
websocket3.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account3: ', msg, 'time: ', new Date().toLocaleTimeString());
    // 在特定时间买士兵5
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 116) {
        websocket3.send(JSON.stringify({
            first: false,
            address: account3["address"],
            operation: 'buySoldiers',
            type: [5],
            price: 30,
            quantity: 1,
            signature: JSON.stringify(account3)
        }));
    }
    // 在特定时间继续购买士兵1, 3, 4, 1
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 108) {
        websocket3.send(JSON.stringify({
            first: false,
            address: account3["address"],
            operation: 'buySoldiers',
            type: [1, 3, 4, 1],
            price: 65,
            quantity: 4,
            signature: JSON.stringify(account3)
        }));
    }
    // 在特定时间departure
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 95) {
        websocket3.send(JSON.stringify({
            first: false,
            address: account3["address"],
            operation: 'departure',
            type: [5, 1, 3, 4, 1],
            price: 95,
            quantity: 5
        }));
    }
    // 出牌
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 1 && msg["timer"] === 14) {
            websocket3.send(JSON.stringify({
                first: false,
                address: account3["address"],
                operation: 'pickSoldier',
                soldier: 5
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 2 && msg["timer"] === 14) {
            websocket3.send(JSON.stringify({
                first: false,
                address: account3["address"],
                operation: 'pickSoldier',
                soldier: 1
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 3 && msg["timer"] === 14) {
            websocket3.send(JSON.stringify({
                first: false,
                address: account3["address"],
                operation: 'pickSoldier',
                soldier: 3
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 4 && msg["timer"] === 14) {
            websocket3.send(JSON.stringify({
                first: false,
                address: account3["address"],
                operation: 'pickSoldier',
                soldier: 4
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 5 && msg["timer"] === 14) {
            websocket3.send(JSON.stringify({
                first: false,
                address: account3["address"],
                operation: 'pickSoldier',
                soldier: 1
            }));
        }
    }
}
websocket4.onmessage = function(event) {
    var msg = JSON.parse(event.data);
    console.log('account4: ', msg, 'time: ', new Date().toLocaleTimeString());
    // 在特定时间买士兵2, 4
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 111) {
        websocket4.send(JSON.stringify({
            first: false,
            address: account4["address"],
            operation: 'buySoldiers',
            type: [2, 4],
            price: 40,
            quantity: 2,
            signature: JSON.stringify(account4)
        }));
    }
    // 在特定时间继续购买士兵4, 3
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 104) {
        websocket4.send(JSON.stringify({
            first: false,
            address: account4["address"],
            operation: 'buySoldiers',
            type: [4, 3, 2],
            price: 60,
            quantity: 3,
            signature: JSON.stringify(account4)
        }));
    }
    // 在特定时间departure
    if (msg["stage"] === 'buySoldiers' && msg["timer"] === 93) {
        websocket4.send(JSON.stringify({
            first: false,
            address: account4["address"],
            operation: 'departure',
            type: [2, 4, 4, 3, 2],
            price: 100,
            quantity: 5
        }));
    }
    // 出牌
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 1 && msg["timer"] === 10) {
            websocket4.send(JSON.stringify({
                first: false,
                address: account4["address"],
                operation: 'pickSoldier',
                soldier: 4
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 2 && msg["timer"] === 10) {
            websocket4.send(JSON.stringify({
                first: false,
                address: account4["address"],
                operation: 'pickSoldier',
                soldier: 4
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 3 && msg["timer"] === 10) {
            websocket4.send(JSON.stringify({
                first: false,
                address: account4["address"],
                operation: 'pickSoldier',
                soldier: 3
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 4 && msg["timer"] === 10) {
            websocket4.send(JSON.stringify({
                first: false,
                address: account4["address"],
                operation: 'pickSoldier',
                soldier: 2
            }));
        }
    }
    if (!msg["isOver"]) {
        if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 5 && msg["timer"] === 10) {
            websocket4.send(JSON.stringify({
                first: false,
                address: account4["address"],
                operation: 'pickSoldier',
                soldier: 2
            }));
        }
    }
}