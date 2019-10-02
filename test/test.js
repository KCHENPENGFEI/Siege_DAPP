function sleep(ms) {
    var start = Date.now(), 
    now = start; 
    while (now - start < ms) { 
        now = Date.now(); 
    } 
}


// async function testAsync() {
//     return "hello async";
// }

// const result = testAsync();
// console.log(result);

// testAsync().then(v => {
//     console.log(v);    // 输出 hello async
// });

function getSomething() {
    return "something";
}

async function testAsync() {
    return new Promise((resolve) => {
        setTimeout(resolve(), 2000);
    });
}

async function test() {
    const v1 = await getSomething();
    console.log(1111);
    const v2 = await testAsync();
    console.log(v1, v2);
}

test();