function mySendRequest(link, body, func) {
    let request = new XMLHttpRequest();
    request.open("POST", link, true);
    request.addEventListener("load", function () {
        func(request);
    });
    let csrfToken = document
        .getElementsByName('_csrf_value')[0]
        .getAttribute('content');
    request.setRequestHeader("X-CSRF-Token", csrfToken);
    if (body === null || body === undefined) {
        request.send();
    } else {
        request.send(body);
    }
}