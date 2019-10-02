var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;

function testStart() {
    console.log('=================Siege Test Start=================')
}

// function responsePrint(http) {
//     if (http.readyState === 4 && http.status === 200) {
//         console.log(http.responseTexts);
//     }
// }

function registerAccount() {
    // var s = false;
    // return new Promise(() => {
    //     const registerHttp = new XMLHttpRequest();
    //     const registerUrl = 'http://localhost:8088/register';
    //     registerHttp.open("POST", registerUrl, true);
    //     registerHttp.setRequestHeader("Content-Type", "application/json");
    //     var data = JSON.stringify({
    //         "register": true
    //     });
    //     registerHttp.send(data);
    //     registerHttp.onreadystatechange = function () {
    //         if (registerHttp.readyState === 4 && registerHttp.status === 200) {
    //             console.log(registerHttp.responseText);
    //             // return registerHttp.responseText;
    //             s = true;
    //         }
    //     };
    // });
    const registerHttp = new XMLHttpRequest();
    const registerUrl = 'http://localhost:8088/register';
    registerHttp.open("POST", registerUrl, false);
    registerHttp.setRequestHeader("Content-Type", "application/json");
    var data = JSON.stringify({
        "register": true
    });
    registerHttp.send(data);
    if (registerHttp.status === 200) {
        // console.log(registerHttp.responseText);
    }
    return registerHttp.responseText;
    // registerHttp.send(data);

    // registerHttp.onreadystatechange = function () {
    //     if (registerHttp.readyState === 4 && registerHttp.status === 200) {
    //         console.log(registerHttp.responseText);
    //         // return registerHttp.responseText;
    //     }
    // };
    // return registerHttp.responseText;
}
// function test() {
//     return new Promise((resolve) => {
//         var result = registerAccount();
//         var a = function() {
//             return new Promise((resolve) => {
//                 var b = registerAccount();
//                 console.log(b);
//                 resolve(b);
//             });
//         }
//         var c = a();
//         console.log(result, c);
//         resolve(result);
//     });
// }

function login(account) {
    const loginHttp = new XMLHttpRequest();
    const loginUrl = 'http://localhost:8088/login';
    loginHttp.open("POST", loginUrl, false);
    loginHttp.setRequestHeader("Content-Type", "application/json");
    loginHttp.send(JSON.stringify(account));
    if (loginHttp.status === 200) {
        // console.log(loginHttp.responseText);
    }
    return loginHttp.responseText;
}

async function main() {
    testStart();

    console.log('正在进行注册...');
    var account1 = JSON.parse(registerAccount())["account"];
    var account2 = JSON.parse(registerAccount())["account"];
    var account3 = JSON.parse(registerAccount())["account"];
    var account4 = JSON.parse(registerAccount())["account"];
    console.log('账号1: ', account1);
    console.log('账号2: ', account2);
    console.log('账号3: ', account3);
    console.log('账号4: ', account4);

    console.log('正在登陆验证...');
    var loginResult1 = JSON.parse(login(account1))["status"];
    var loginResult2 = JSON.parse(login(account2))["status"];
    var loginResult3 = JSON.parse(login(account3))["status"];
    var loginResult4 = JSON.parse(login(account4))["status"];
    if (loginResult1 === 'success' && loginResult2 === 'success' && loginResult3 === 'success' && loginResult4 === 'success') {
        console.log('玩家均已登陆成功');
    }
}


main();