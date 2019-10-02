var express = require('express');
var test = require('./registerAndLogin')
var app = express();

app.get('/', function(res, rep) {
    rep.send('Hello, word!');
    // function testStart() {
    //     console.log('=================Siege Test Start=================')
    // }
    
    // function register4Accounts() {
    //     const registerHttp = new XMLHttpRequest();
    //     const registerUrl = 'http://localhost:8088/register';
    //     registerHttp.open("POST", registerUrl, true);
    //     registerHttp.setRequestHeader("Content-Type", "application/json");
    //     var data = JSON.stringify({
    //         "register": true
    //     });
    //     registerHttp.send(data);
    
    //     if (registerHttp.readyState == 4 && registerHttp.status == 200) {
    //         console.log(registerHttp.responseText);
    //     }
    // }
    
    
    // testStart();
    // register4Accounts();    
    console.log("kj");
});

app.listen(3000);