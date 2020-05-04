function createMultiStream() {
    mySendRequest("/ms/create", null, function (request) {
        console.log(request.response);
        if (request.status === 200) {
            window.location = request.response;
        } else {
            alert(request.response);
        }
    });
}

function deleteMultiStream() {
    mySendRequest("/ms/delete", null, function (request) {
        console.log(request.response);
        if (request.status === 200) {
            window.location = request.response;
        } else {
            alert(request.response);
        }
    });
}