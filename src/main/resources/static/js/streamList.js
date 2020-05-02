function createStream() {
    let request = new XMLHttpRequest();
    let link = "/createStream";
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            openStream(document.getElementById('profileDropDownNickname').innerText);
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    request.send();
}

function openStream(channel) {
    window.location.replace('/'+channel);
}