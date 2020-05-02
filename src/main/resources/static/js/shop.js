function show(num) {
    arr = document.getElementsByClassName('content');
    row = document.getElementsByClassName('row')[0].getElementsByTagName('a');
    for (let i = 0; i < arr.length; i++) {
        if (i + 1 === num) {
            arr[i].style.display = 'block';
            row[i].style.borderBottom = '3px solid white';
        } else {
            arr[i].style.display = 'none';
            row[i].style.border = 'none';
        }
    }
}

function buyPremium() {
    let request = new XMLHttpRequest();
    request.open("POST", "/shop/buy/premium", true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            let content = document.getElementsByClassName('premium')[0];
            let wrapBody = content.getElementsByClassName('wrapBody')[0];
            let bottom = wrapBody.getElementsByClassName('bottom')[0];
            let right = bottom.getElementsByClassName('right')[0];
            right.innerHTML = '<img src="/img/success.png">';
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function buySmile(id) {
    let request = new XMLHttpRequest();
    request.open("POST", "/shop/buy/smile/" + id, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            let wrapBody = document.getElementById(id);
            let bottom = wrapBody.getElementsByClassName('bottom')[0];
            let right = bottom.getElementsByClassName('right')[0];
            right.innerHTML = '<img src="/img/success.png">';
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function buyLevel() {
    var request = new XMLHttpRequest();
    request.open("POST", "/shop/buy/level", true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            let content = document.getElementsByClassName('rank')[0];
            let wrapBody = content.getElementsByClassName('wrapBody')[0];
            let top = wrapBody.getElementsByClassName('top')[0];
            let level = top.innerHTML.split(" ")[0];
            let lvlWord = top.innerHTML.split(" ")[1];
            let expWord = top.innerHTML.split(" ")[3];
            top.innerHTML = ++level + ' ' + lvlWord + ' (' + 0 + ' ' + expWord;
            let levelWrap = content.getElementsByClassName('levelWrap')[0];
            let levelWrap1 = levelWrap.getElementsByClassName('levelWrap')[0];
            let levelWrap2 = levelWrap.getElementsByClassName('levelWrap')[1];
            let lvl1 = levelWrap1.getElementsByTagName('div')[0];
            let exp1 = levelWrap2.getElementsByTagName('div')[0];
            expWord = exp1.innerHTML.split(' ')[0];
            lvl1.innerHTML = lvlWord + ' ' + --level;
            exp1.innerHTML = expWord + ' 0/' + level * 100;
            wrapBody = content.getElementsByClassName('wrapBody')[0];
            let bottom = wrapBody.getElementsByClassName('bottom')[0];
            let right = bottom.getElementsByClassName('right')[0];
            if (document.getElementById("buyLevelPic") !== null) return;
            right.innerHTML = right.innerHTML + '<img id="buyLevelPic" src="/img/success.png">';
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function buyStyle(id) {
    let request = new XMLHttpRequest();
    request.open("POST", "/shop/buy/style/" + id, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            let styles = document.getElementsByClassName('text')[0];
            let wrapBody = styles.getElementsByClassName(id)[0];
            let bottom = wrapBody.getElementsByClassName('bottom')[0];
            let right = bottom.getElementsByClassName('right')[0];
            right.innerHTML = '<img src="/img/success.png">';
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}