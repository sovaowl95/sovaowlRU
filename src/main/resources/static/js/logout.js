function doLogout() {
    var req = new XMLHttpRequest();
    req.open("POST", "/logout", true);
    req.addEventListener("load", function (ev) {
        if (req.status === 200) {
            window.location.replace("/");
        } else {
            console.log("ELSE");
            //todo:
            //document.getElementsByClassName('button')[0].style.border = ".5px solid red";
        }
    });
    json = {'session':getCookie('session')};
    myJson = JSON.stringify(json);
    var csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    req.setRequestHeader("X-CSRF-Token", csrfToken);
    req.send(myJson);
}



function getCookie(name) {
    var matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([.$?*|{}()\[\]\\\/+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}