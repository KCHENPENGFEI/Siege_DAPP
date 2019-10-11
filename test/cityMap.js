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

var account4 = {
    address: 'DB29A4D020DA75E3AE5BDCC2D843846D011E8A4E',
    publicKey:
   '044EE8C0A8BE9F1A457239C6A7962AC23A8C91FE2CA4704D35FF719E6788EA46B76213B83D20CD841EA1C8EAA6FC108CA92E09C022F8D65FCCB41B5EC5B3D98895',
    privateKey:
   '44CA4352307A62B3758249CD2A0AC50EFAD07080CDD7669C4E15C52DE7ED97F2',
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

websocket1 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/73");
// sleep(1000);
websocket2 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/73");
// sleep(1000);
websocket3 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/73");
// sleep(1000);
websocket4 = new WebSocket("ws://localhost:8088/WebSocket/cityMap/73");
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