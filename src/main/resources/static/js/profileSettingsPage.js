function addIcon(el) {
    mySendRequest("/profile/settings/icon/add?name=" + el.id, null, function (request) {
        if (request.status === 200) {
            el.classList.remove("selectable");
            el.classList.add("selected");
        }
    });
}

function removeIcon(el) {
    mySendRequest("/profile/settings/icon/remove?name=" + el.id, null, function (request) {
        if (request.status === 200) {
            el.classList.remove("selected");
            el.classList.add("selectable");
        }
    });
}

function toggleIcon(el) {
    if (el.classList.contains("selectable")) {
        addIcon(el);
    } else {
        removeIcon(el);
    }
}

function clearSelectedIcons() {
    mySendRequest("/profile/settings/icon/clear", null, function (request) {
        if (request.status === 200) {
            location.reload();
        }
    });
}

function revalidateSelectedIcons() {
    mySendRequest("/profile/settings/icon/recalculate", null, function (request) {
        if (request.status === 200) {
            location.reload();
        }
    });
}