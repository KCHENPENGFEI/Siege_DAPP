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
// websocket2.on('open', function() {
//     console.log('account2 connected, time: ', new Date().toLocaleTimeString());
//     websocket2.send(JSON.stringify(first2));
// });
websocket3.on('open', function() {
    console.log('account3 connected, time: ', new Date().toLocaleTimeString());
    websocket3.send(JSON.stringify(first3));
});
// websocket4.on('open', function() {
//     console.log('account4 connected, time: ', new Date().toLocaleTimeString());
//     websocket4.send(JSON.stringify(first4));
// });

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
// websocket2.onmessage = function(event) {
//     var msg = JSON.parse(event.data);
//     console.log('account2: ', msg, 'time: ', new Date().toLocaleTimeString());
//     // 在特定时间买士兵1
//     if (msg["stage"] === 'buySoldiers' && msg["timer"] === 113) {
//         websocket2.send(JSON.stringify({
//             first: false,
//             address: account2["address"],
//             operation: 'buySoldiers',
//             type: [1],
//             price: 10,
//             quantity: 1,
//             signature: JSON.stringify(account2)
//         }));
//     }
//     // 在特定时间继续购买士兵2, 2, 5, 4
//     if (msg["stage"] === 'buySoldiers' && msg["timer"] === 109) {
//         websocket2.send(JSON.stringify({
//             first: false,
//             address: account2["address"],
//             operation: 'buySoldiers',
//             type: [2, 2, 5, 4],
//             price: 85,
//             quantity: 4,
//             signature: JSON.stringify(account2)
//         }));
//     }
//     // 在特定时间departure
//     if (msg["stage"] === 'buySoldiers' && msg["timer"] === 93) {
//         websocket2.send(JSON.stringify({
//             first: false,
//             address: account2["address"],
//             operation: 'departure',
//             type: [1, 2, 2, 5, 4],
//             price: 95,
//             quantity: 5
//         }));
//     }
//     // 出牌
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 1 && msg["timer"] === 15) {
//             websocket2.send(JSON.stringify({
//                 first: false,
//                 address: account2["address"],
//                 operation: 'pickSoldier',
//                 soldier: 2
//             }));
//         }
//     }
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 2 && msg["timer"] === 15) {
//             websocket2.send(JSON.stringify({
//                 first: false,
//                 address: account2["address"],
//                 operation: 'pickSoldier',
//                 soldier: 1
//             }));
//         }
//     }
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 3 && msg["timer"] === 15) {
//             websocket2.send(JSON.stringify({
//                 first: false,
//                 address: account2["address"],
//                 operation: 'pickSoldier',
//                 soldier: 2
//             }));
//         }
//     }
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 4 && msg["timer"] === 15) {
//             websocket2.send(JSON.stringify({
//                 first: false,
//                 address: account2["address"],
//                 operation: 'pickSoldier',
//                 soldier: 4
//             }));
//         }
//     }
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 5 && msg["timer"] === 15) {
//             websocket2.send(JSON.stringify({
//                 first: false,
//                 address: account2["address"],
//                 operation: 'pickSoldier',
//                 soldier: 5
//             }));
//         }
//     }
// }
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
// websocket4.onmessage = function(event) {
//     var msg = JSON.parse(event.data);
//     console.log('account4: ', msg, 'time: ', new Date().toLocaleTimeString());
//     // 在特定时间买士兵2, 4
//     if (msg["stage"] === 'buySoldiers' && msg["timer"] === 111) {
//         websocket4.send(JSON.stringify({
//             first: false,
//             address: account4["address"],
//             operation: 'buySoldiers',
//             type: [2, 4],
//             price: 40,
//             quantity: 2,
//             signature: JSON.stringify(account4)
//         }));
//     }
//     // 在特定时间继续购买士兵4, 3
//     if (msg["stage"] === 'buySoldiers' && msg["timer"] === 104) {
//         websocket4.send(JSON.stringify({
//             first: false,
//             address: account4["address"],
//             operation: 'buySoldiers',
//             type: [4, 3],
//             price: 45,
//             quantity: 2,
//             signature: JSON.stringify(account4)
//         }));
//     }
//     // 在特定时间departure
//     if (msg["stage"] === 'buySoldiers' && msg["timer"] === 93) {
//         websocket4.send(JSON.stringify({
//             first: false,
//             address: account4["address"],
//             operation: 'departure',
//             type: [2, 4, 4, 3],
//             price: 85,
//             quantity: 4
//         }));
//     }
//     // 出牌
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 1 && msg["timer"] === 10) {
//             websocket4.send(JSON.stringify({
//                 first: false,
//                 address: account4["address"],
//                 operation: 'pickSoldier',
//                 soldier: 3
//             }));
//         }
//     }
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 2 && msg["timer"] === 10) {
//             websocket4.send(JSON.stringify({
//                 first: false,
//                 address: account4["address"],
//                 operation: 'pickSoldier',
//                 soldier: 4
//             }));
//         }
//     }
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 3 && msg["timer"] === 10) {
//             websocket4.send(JSON.stringify({
//                 first: false,
//                 address: account4["address"],
//                 operation: 'pickSoldier',
//                 soldier: 4
//             }));
//         }
//     }
//     if (!msg["isOver"]) {
//         if (msg["stage"] === 'battle' && msg["positive"] === true && msg["round"] === 4 && msg["timer"] === 10) {
//             websocket4.send(JSON.stringify({
//                 first: false,
//                 address: account4["address"],
//                 operation: 'pickSoldier',
//                 soldier: 2
//             }));
//         }
//     }
// }