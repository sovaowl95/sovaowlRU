function doLogout() {
    let req = new XMLHttpRequest();
    req.open("POST", "/logout", true);
    req.addEventListener("load", function (ev) {
        if (req.status === 200) {
            window.location.replace("/");
        } else {
            document.getElementsByClassName('button')[1].style.border = ".5px solid red";
        }
    });
    let json = {'session': getCookie('session')};
    let myJson = JSON.stringify(json);
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    req.setRequestHeader("X-CSRF-Token", csrfToken);
    req.send(myJson);
}



function getCookie(name) {
    let matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([.$?*|{}()\[\]\\\/+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}