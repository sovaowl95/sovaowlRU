function send() {
    inp = document.getElementsByTagName('input')[0];
    let text = document.getElementsByTagName('textarea')[0];

    let request = new XMLHttpRequest();
    request.open("POST", "/feedback", true);
    request.addEventListener("load", function () {
        if (request.status === 200) {
            document.getElementsByTagName('button')[0].style.border = "solid green 1px";
        } else {
            document.getElementsByTagName('button')[0].style.border = "solid red 1px";
        }
    });
    let csrfToken = document.getElementsByName('_csrf_value')[0].getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    let json = {'theme': inp.value, 'message': text.value};
    let myJson = JSON.stringify(json);
    request.send(myJson);
}