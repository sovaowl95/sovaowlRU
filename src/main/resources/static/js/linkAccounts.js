function connect(service) {
    if (service.indexOf(twitch) === 0) {
        authTwitch();
    } else if (service.indexOf(google) === 0) {
        authGoogle();
    } else if (service.indexOf(gg) === 0) {
        authGG();
    } else if (service.indexOf(vk) === 0){
        authVK();
    }
}

function disconnect(service) {
    if (service.indexOf(twitch) === 0) {
        disconnectTwitch()
    } else if (service.indexOf(google) === 0) {
        disconnectGoogle();
    } else if (service.indexOf(gg) === 0) {
        disconnectGG();
    }else if (service.indexOf(vk) === 0) {
        disconnectVK();
    }
}

function disconnectTwitch() {
    let reg = new XMLHttpRequest();
    reg.open("POST", "/api/auth/twitch/revoke", true);
    reg.addEventListener("load", function () {
        if (reg.status === 200) {
            window.location.reload();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    reg.setRequestHeader("X-CSRF-Token", csrfToken);
    reg.send();
}

function disconnectGoogle() {
    let reg = new XMLHttpRequest();
    reg.open("POST", "/api/auth/google/revoke", true);
    reg.addEventListener("load", function () {
        if (reg.status === 200) {
            window.location.reload();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    reg.setRequestHeader("X-CSRF-Token", csrfToken);
    reg.send();
}

function disconnectGG() {
    let reg = new XMLHttpRequest();
    reg.open("POST", "/api/auth/gg/revoke", true);
    reg.addEventListener("load", function () {
        if (reg.status === 200) {
            window.location.reload();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    reg.setRequestHeader("X-CSRF-Token", csrfToken);
    reg.send();
}


function disconnectVK() {
    let reg = new XMLHttpRequest();
    reg.open("POST", "/api/auth/vk/revoke", true);
    reg.addEventListener("load", function () {
        if (reg.status === 200) {
            window.location.reload();
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    reg.setRequestHeader("X-CSRF-Token", csrfToken);
    reg.send();
}