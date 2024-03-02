function startup() {
    loadUsers();
}

function loadUsers() {
    fetch("/rest/users/all", {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    }).then(response => response.json())
        .then(data => {
            const users = data["users"];
            const ulist = document.getElementById("outUsers");
            ulist.innerHTML = "";

            users.forEach(user => {
                const li = document.createElement("li");
                li.appendChild(document.createTextNode(user["displayName"]));
                const bttn = document.createElement("button");
                bttn.appendChild(document.createTextNode("edit"));
                bttn.onclick = () => openUserDetails(user["id"]);
                li.appendChild(bttn);

                ulist.appendChild(li);
            });
        });
}

function openUserDetails(userId) {
    const output = document.getElementById("outUserDetails");
    output.innerHTML = "";

    const containerUserInfo = document.createElement("div");
    containerUserInfo.innerText = "Loading user info...";

    const containerPermissions = document.createElement("div");
    containerPermissions.innerText = "Loading permissions...";

    output.appendChild(containerUserInfo);
    output.appendChild(containerPermissions);

    loadUserInfo(userId, containerUserInfo).then(() => {});
    loadUserPermissions(userId, containerPermissions).then(() => {});
}

async function loadUserInfo(userId, outputContainer) {
    fetch(`/rest/users/id/${userId}`, {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            return response.json();
        })
        .then(data => {
            outputContainer.innerHTML = "";
            const mail = data["mail"];
            const displayName = data["displayName"];
            const id = data["id"];

            const table = document.createElement("table");

            const tr1 = document.createElement("tr");
            const td1 = document.createElement("td");
            td1.appendChild(document.createTextNode("ID"));
            const td2 = document.createElement("td");
            td2.appendChild(document.createTextNode(id));
            tr1.appendChild(td1);
            tr1.appendChild(td2);
            table.appendChild(tr1);

            const tr2 = document.createElement("tr");
            const td3 = document.createElement("td");
            td3.appendChild(document.createTextNode("Mail"));
            const td4 = document.createElement("td");
            td4.appendChild(document.createTextNode(mail));
            tr2.appendChild(td3);
            tr2.appendChild(td4);
            table.appendChild(tr2);

            const tr3 = document.createElement("tr");
            const td5 = document.createElement("td");
            td5.appendChild(document.createTextNode("Display Name"));
            const td6 = document.createElement("td");
            td6.appendChild(document.createTextNode(displayName));
            tr3.appendChild(td5);
            tr3.appendChild(td6);
            table.appendChild(tr3);

            outputContainer.appendChild(table);
        }).catch(error => {
        outputContainer.innerHTML = "";
        outputContainer.appendChild(document.createTextNode(error.message));
    });
}

async function loadUserPermissions(userId, outputContainer) {
    fetch(`/rest/permission/user/${userId}`, {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            return response.json();
        })
        .then(data => {
            outputContainer.innerHTML = "";
            const permissions = data["permissions"];

            const table = document.createElement("table");
            // table header
            const trh = document.createElement("tr");
            const th1 = document.createElement("th");
            th1.appendChild(document.createTextNode("Route"));
            const th2 = document.createElement("th");
            th2.appendChild(document.createTextNode("GET"));
            const th3 = document.createElement("th");
            th3.appendChild(document.createTextNode("POST"));
            const th4 = document.createElement("th");
            th4.appendChild(document.createTextNode("PUT"));
            const th5 = document.createElement("th");
            th5.appendChild(document.createTextNode("DELETE"));
            const th6 = document.createElement("th");
            trh.appendChild(th1);
            trh.appendChild(th2);
            trh.appendChild(th3);
            trh.appendChild(th4);
            trh.appendChild(th5);
            trh.appendChild(th6);
            table.appendChild(trh);

            permissions.forEach(permission => table.appendChild(permissionToTableRow(userId, permission)));

            outputContainer.appendChild(table);

            const bttnAddNew = document.createElement("button");
            bttnAddNew.appendChild(document.createTextNode("+"));
            bttnAddNew.onclick = () => openAddPermission(userId);
            outputContainer.appendChild(bttnAddNew);
        }).catch(error => {
        outputContainer.innerHTML = "";
        outputContainer.appendChild(document.createTextNode(error.message));
    });
}

function permissionToTableRow(userId, permission) {
    const tr = document.createElement("tr");
    const td1 = document.createElement("td");
    td1.appendChild(document.createTextNode(permission["route"]));
    const td2 = document.createElement("td");
    td2.appendChild(createCheckboxForPermission(userId,permission["GET"]));
    const td3 = document.createElement("td");
    td3.appendChild(createCheckboxForPermission(userId,permission["POST"]));
    const td4 = document.createElement("td");
    td4.appendChild(createCheckboxForPermission(userId,permission["PUT"]));
    const td5 = document.createElement("td");
    td5.appendChild(createCheckboxForPermission(userId,permission["DELETE"]));
    tr.appendChild(td1);
    tr.appendChild(td2);
    tr.appendChild(td3);
    tr.appendChild(td4);
    tr.appendChild(td5);
    tr.appendChild(createDeleteButton(userId));

    return tr;
}

function createDeleteButton(userId) {
    const td = document.createElement("td");
    const bttn = document.createElement("button");
    bttn.appendChild(document.createTextNode("delete"));
    bttn.onclick = () => deletePermission(bttn, userId);
    td.appendChild(bttn);
    return td;
}

function deletePermission(sender, userId) {
    const route = sender.parentElement.parentElement.children[0].innerText;
    fetch(`/rest/permission/user/${userId}/route/${encodeURIComponent(route)}`, {
        method: "DELETE", headers: {
            "Content-Type": "application/json"
        }
    }).then(response => {
        if (!response.ok) {
            throw new Error("HTTP error, status = " + response.status);
        }
        openUserDetails(userId);
    }).catch(error => {
        alert(error.message);
    });
}

function createCheckboxForPermission(userId, permissionValue) {
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.checked = permissionValue === 1;
    checkbox.onclick = () => updatePermission(userId, checkbox);
    return checkbox;
}

function updatePermission(userId, sender) {
    const row = sender.parentElement.parentElement;

    const route = row.children[0].innerText;
    const get = row.children[1].children[0].checked ? 1 : 0;
    const post = row.children[2].children[0].checked ? 1 : 0;
    const put = row.children[3].children[0].checked ? 1 : 0;
    const del = row.children[4].children[0].checked ? 1 : 0;

    const permission = {
        route: route,
        get: get,
        post: post,
        put: put,
        delete: del
    };

    savePermission(userId, permission, () => {
        sender.parentElement.parentElement.classList.add("pulseGreen");
        setTimeout(() => {
            sender.parentElement.parentElement.classList.remove("pulseGreen");
        }, 750);
    }, (error) => {
        alert(error.message);
        sender.checked = !sender.checked;
    });
}

function openAddPermission(userId) {
    const output = document.getElementById("outAddNewPermission");
    output.innerHTML = "";

    const inputRoute = document.createElement("input");
    inputRoute.type = "text";
    inputRoute.placeholder = "Route";
    output.appendChild(inputRoute);

    const ul = document.createElement("ul");
    output.appendChild(ul);

    const methods = ["GET", "POST", "PUT", "DELETE"];
    methods.forEach(method => {
        const li = document.createElement("li");
        const label = document.createElement("label");
        label.appendChild(document.createTextNode(method));
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        label.appendChild(checkbox);
        li.appendChild(label);
        ul.appendChild(li);
    });

    const bttn = document.createElement("button");
    bttn.appendChild(document.createTextNode("add"));
    bttn.onclick = () => addPermission(userId, inputRoute.value, ul);
    output.appendChild(bttn);
}

function addPermission(userId, route, ul) {
    const permission = {
        route: route,
        get: ul.children[0].children[0].children[0].checked ? 1 : 0,
        post: ul.children[1].children[0].children[0].checked ? 1 : 0,
        put: ul.children[2].children[0].children[0].checked ? 1 : 0,
        delete: ul.children[3].children[0].children[0].checked ? 1 : 0
    };
    savePermission(userId, permission, () => {
        document.getElementById("outAddNewPermission").innerHTML = "";
        openUserDetails(userId);
    }, (error) => {
        alert(error.message);
    });
}

function savePermission(userId, permission, onSuccess = () => {}, onError = (error) => {}) {
    fetch(`/rest/permission/user/${userId}`, {
        method: "POST", headers: {
            "Content-Type": "application/json"
        }, body: JSON.stringify(permission)
    }).then(response => {
        if (!response.ok) {
            throw new Error("HTTP error, status = " + response.status);
        }
        onSuccess();
    }).catch(error => {
        onError(error);
    });
}
