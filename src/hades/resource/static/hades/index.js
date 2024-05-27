function startup() {
    displayLandingPage();
    displayCurrentUserDisplayName().then(() => {
    });
}

function displayLandingPage() {
    document.title = "Hades";

    const centerContainer = document.getElementById("centerContainer");
    centerContainer.innerHTML = "";

    const div = document.createElement("div");

    const h1Welcome = document.createElement("h1");
    h1Welcome.appendChild(document.createTextNode("Welcome to Hades"));
    div.appendChild(h1Welcome);

    const pDescription = document.createElement("p");
    pDescription.classList.add("description");
    pDescription.appendChild(document.createTextNode("The perfect solution tailored for small-scale projects that require simplified user and permission management without the overhead of a full-fledged IAM system. Designed with simplicity in mind, Hades offers a lightweight yet robust platform to effortlessly handle user access and permissions for your projects. Say goodbye to complexity and hello to streamlined management with Hades."));
    div.appendChild(pDescription);

    div.appendChild(document.createElement("br"));

    const divActions = document.createElement("div");

    const menuActionsImages = [
        { image:"icon_user.png", alt: "Users", click: () => openUsersTab() },
        { image:"icon_group.png", alt: "Groups", click: () => openGroupsTab() },
        { image:"icon_dbExplorer.png", alt: "Database Explorer", click: () => openDBExplorer() }
    ];

    menuActionsImages.forEach(action => {
        const img = document.createElement("img");
        img.classList.add("actionIcon");
        img.src = "{{CONTEXT}}/hades/img/" + action.image;
        img.alt = action.alt;
        img.onclick = action.click;
        divActions.appendChild(img);
    });

    div.appendChild(divActions);

    div.appendChild(document.createElement("br"));

    const divBaseTechnology = document.createElement("div");

    const h2Tech = document.createElement("h2");
    h2Tech.appendChild(document.createTextNode("Technologies"));
    divBaseTechnology.appendChild(h2Tech);

    divBaseTechnology.classList.add("baseTechnology");

    ["Hades", "thot", "dobby"].forEach((technology) => {
        const card = document.createElement("div");
        card.classList.add("technology", `card_${technology}`);
        card.onclick = () => {
            window.open(`https://github.com/klnsdr/${technology}`, "_blank");
        };

        const logo = document.createElement("img");
        logo.classList.add("logo");
        logo.src = `{{CONTEXT}}/hades/img/logo_${technology}.png`;
        logo.alt = technology;
        card.appendChild(logo);

        const cardTitle = document.createElement("h2");
        cardTitle.classList.add("technologyTitle");
        cardTitle.appendChild(document.createTextNode(technology));
        card.appendChild(cardTitle);
        divBaseTechnology.appendChild(card);
    });

    div.appendChild(divBaseTechnology);

    centerContainer.appendChild(div);
}

async function displayCurrentUserDisplayName() {
    fetch("{{CONTEXT}}/rest/users/loginUserInfo", {
        method: "GET", headers: {
            "Content-Type": "application/json",
        }
    }).then((response) => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error("HTTP error, status = " + response.status);
        }
    }).then((data) => {
        document.getElementById("lblCurrentUser").innerText = `(${data["displayName"]})`;
    }).catch((error) => {
        alert(error.message);
    });
}

function openUsersTab() {
    document.title = "Hades - Users";

    const centerContainer = document.getElementById("centerContainer");
    centerContainer.innerHTML = "";

    const containerOutUsers = document.createElement("div");
    containerOutUsers.id = "containerOutUsers";
    centerContainer.appendChild(containerOutUsers);

    const outUsers = document.createElement("table");
    outUsers.classList.add("tblUsers");
    outUsers.id = "outUsers";
    containerOutUsers.appendChild(outUsers);

    const hr = document.createElement("hr");
    centerContainer.appendChild(hr);

    const outUserActions = document.createElement("div");
    outUserActions.id = "outUserActions";
    centerContainer.appendChild(outUserActions);

    const outUserDetails = document.createElement("div");
    outUserDetails.id = "outUserDetails";
    centerContainer.appendChild(outUserDetails);

    loadUsers();
}

function openGroupsTab() {
    document.title = "Hades - Groups";

    const centerContainer = document.getElementById("centerContainer");
    centerContainer.innerHTML = "";

    const containerOutGroups = document.createElement("div");
    containerOutGroups.id = "containerOutUsers";
    centerContainer.appendChild(containerOutGroups);

    const outGroups = document.createElement("table");
    outGroups.classList.add("tblUsers");
    outGroups.id = "outGroups";
    containerOutGroups.appendChild(outGroups);

    const bttnNewGroup = document.createElement("button");
    bttnNewGroup.appendChild(document.createTextNode("+"));
    bttnNewGroup.onclick = () => createNewGroup();
    containerOutGroups.appendChild(bttnNewGroup);

    const hr = document.createElement("hr");
    centerContainer.appendChild(hr);

    const outGroupActions = document.createElement("div");
    outGroupActions.id = "outGroupActions";
    centerContainer.appendChild(outGroupActions);

    const containerGroupDetails = document.createElement("div");
    containerGroupDetails.id = "containerGroupDetails";
    centerContainer.appendChild(containerGroupDetails);

    loadAllGroups();
}

function createNewGroup() {
    const container = document.createElement("div");

    const lableName = document.createElement("label");
    lableName.innerText = "Name:";
    container.appendChild(lableName);

    const inputName = document.createElement("input");
    container.appendChild(inputName);

    const bttnSave = document.createElement("button");
    bttnSave.innerText = "save";
    bttnSave.onclick = () => {
        saveNewGroup(inputName.value);
        closePopup(bttnSave);
    };
    container.appendChild(bttnSave);

    openPopup(container);
}

function saveNewGroup(groupName) {
    fetch("{{CONTEXT}}/rest/groups", {
        method: "POST", headers: {
            "Content-Type": "application/json",
        }, body: JSON.stringify({name: groupName}),
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            loadAllGroups();
        })
        .catch((error) => {
            alert(error.message);
        });
}

function loadAllGroups() {
    fetch("{{CONTEXT}}/rest/groups/all", {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => response.json())
        .then((data) => {
            const groups = data["groups"].sort((a, b) => a["name"].localeCompare(b["name"]));

            const tblGroups = document.getElementById("outGroups");
            tblGroups.innerHTML = "";

            groups.forEach((group) => {
                const tr = document.createElement("tr");

                const tdName = document.createElement("td");
                tdName.appendChild(document.createTextNode(group["name"]));
                tr.appendChild(tdName);

                const tdBttn = document.createElement("td");
                const bttn = document.createElement("button");
                bttn.appendChild(document.createTextNode("edit"));
                bttn.onclick = () => openGroupDetails(group["id"]);
                tdBttn.appendChild(bttn);
                tr.appendChild(tdBttn);

                tblGroups.appendChild(tr);
            });
        });
}

function populateGroupActionBar(groupId) {
    const container = document.getElementById("outGroupActions");
    container.innerHTML = "";

    const actionBar = document.createElement("div");
    actionBar.classList.add("actionBar");

    const bttnAddNew = document.createElement("button");
    bttnAddNew.appendChild(document.createTextNode("add Permission"));
    bttnAddNew.onclick = () => openAddPermissionGroup(groupId);
    actionBar.appendChild(bttnAddNew);

    container.appendChild(actionBar);
}

function openAddPermissionGroup(groupId) {
    const output = document.createElement("div");

    const lblRoute = document.createElement("label");
    lblRoute.appendChild(document.createTextNode("Route:"));
    output.appendChild(lblRoute);

    const selectRoute = generateRouteSelect();
    output.appendChild(selectRoute);

    const bttn = document.createElement("button");
    bttn.appendChild(document.createTextNode("add"));
    bttn.onclick = () => {
        addPermissionGroup(groupId, selectRoute.value);
        closePopup(bttn);
    };
    output.appendChild(bttn);

    openPopup(output);
}

function addPermissionGroup(groupId, route) {
    const permission = {
        route: route, get: 0, post: 0, put: 0, delete: 0,
    };
    saveGroupPermission(groupId, permission, () => {
        openGroupDetails(groupId);
    }, (error) => {
        alert(error.message);
    });
}

function openGroupDetails(groupId) {
    const container = document.getElementById("containerGroupDetails");
    container.innerHTML = "";
    container.innerText = "Loading group details...";

    fetch(`{{CONTEXT}}/rest/groups/id/${groupId}`, {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => response.json())
        .then((data) => {
            container.innerHTML = "";
            populateGroupActionBar(data["id"]);

            const table = document.createElement("table");

            const tr1 = document.createElement("tr");
            const td1 = document.createElement("td");
            const txtID = document.createElement("p");
            txtID.innerText = "ID";
            td1.appendChild(txtID);
            const td2 = document.createElement("td");
            const txtIdData = document.createElement("p");
            txtIdData.innerText = data["id"];
            td2.appendChild(txtIdData);
            tr1.appendChild(td1);
            tr1.appendChild(td2);
            table.appendChild(tr1);

            const tr3 = document.createElement("tr");
            const td5 = document.createElement("td");
            const txtDisplayName = document.createElement("p");
            txtDisplayName.innerText = "Name";
            td5.appendChild(txtDisplayName);
            const td6 = document.createElement("td");
            const txtDisplayNameData = document.createElement("p");
            txtDisplayNameData.innerText = data["name"];
            td6.appendChild(txtDisplayNameData);
            tr3.appendChild(td5);
            tr3.appendChild(td6);
            table.appendChild(tr3);

            container.appendChild(table);

            displayGroupPermissions(groupId, data["permissions"].sort((a, b) => a["route"].localeCompare(b["route"])), container);
        });
}

function displayGroupPermissions(groupId, permissions, outputContainer) {
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

    permissions.forEach((permission) => table.appendChild(groupPermissionToTableRow(groupId, permission)));

    outputContainer.appendChild(table);
}

function groupPermissionToTableRow(groupId, permission) {
    const tr = document.createElement("tr");
    const td1 = document.createElement("td");
    const txtRoute = document.createElement("p");
    txtRoute.innerText = permission["route"];
    td1.appendChild(txtRoute);
    const td2 = document.createElement("td");
    td2.appendChild(createCheckboxForGroupPermission(groupId, permission["GET"]));
    const td3 = document.createElement("td");
    td3.appendChild(createCheckboxForGroupPermission(groupId, permission["POST"]));
    const td4 = document.createElement("td");
    td4.appendChild(createCheckboxForGroupPermission(groupId, permission["PUT"]));
    const td5 = document.createElement("td");
    td5.appendChild(createCheckboxForGroupPermission(groupId, permission["DELETE"]));
    tr.appendChild(td1);
    tr.appendChild(td2);
    tr.appendChild(td3);
    tr.appendChild(td4);
    tr.appendChild(td5);
    tr.appendChild(createDeleteGroupPermissionButton(groupId));

    return tr;
}

function createCheckboxForGroupPermission(groupId, permissionValue) {
    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.checked = permissionValue === 1;
    checkbox.onclick = () => updateGroupPermission(groupId, checkbox);
    return checkbox;
}

function updateGroupPermission(groupId, sender) {
    const row = sender.parentElement.parentElement;

    const route = row.children[0].innerText;
    const get = row.children[1].children[0].checked ? 1 : 0;
    const post = row.children[2].children[0].checked ? 1 : 0;
    const put = row.children[3].children[0].checked ? 1 : 0;
    const del = row.children[4].children[0].checked ? 1 : 0;

    const permission = {
        route: route, get: get, post: post, put: put, delete: del,
    };

    saveGroupPermission(groupId, permission, () => {
        sender.parentElement.parentElement.classList.add("pulseGreen");
        setTimeout(() => {
            sender.parentElement.parentElement.classList.remove("pulseGreen");
        }, 750);
    }, (error) => {
        alert(error.message);
        sender.checked = !sender.checked;
    });
}

function saveGroupPermission(groupId, permission, onSuccess = () => {
}, onError = (error) => {
}) {
    fetch(`{{CONTEXT}}/rest/groups/id/${groupId}/permission`, {
        method: "PUT", headers: {
            "Content-Type": "application/json",
        }, body: JSON.stringify(permission),
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            onSuccess();
        })
        .catch((error) => {
            onError(error);
        });
}

function createDeleteGroupPermissionButton(groupId) {
    const td = document.createElement("td");
    const bttn = document.createElement("button");
    bttn.appendChild(document.createTextNode("X"));
    bttn.onclick = () => deleteGroupPermission(bttn, groupId);
    td.appendChild(bttn);
    return td;
}

function deleteGroupPermission(sender, groupId) {
    const route = sender.parentElement.parentElement.children[0].innerText;
    fetch(`{{CONTEXT}}/rest/groups/group/${groupId}/permission/${encodeURIComponent(route)}`, {
        method: "DELETE", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            openGroupDetails(groupId);
        })
        .catch((error) => {
            alert(error.message);
        });
}

function loadUsers() {
    fetch("{{CONTEXT}}/rest/users/all", {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => response.json())
        .then((data) => {
            const users = data["users"].sort((a, b) => a["displayName"].localeCompare(b["displayName"]));
            const tblUsers = document.getElementById("outUsers");
            tblUsers.innerHTML = "";

            users.forEach((user) => {
                const tr = document.createElement("tr");

                const tdName = document.createElement("td");
                tdName.appendChild(document.createTextNode(user["displayName"]));
                tr.appendChild(tdName);

                const tdBttn = document.createElement("td");
                const bttn = document.createElement("button");
                bttn.appendChild(document.createTextNode("edit"));
                bttn.onclick = () => openUserDetails(user["id"]);
                tdBttn.appendChild(bttn);
                tr.appendChild(tdBttn);

                tblUsers.appendChild(tr);
            });
        });
}

function openUserDetails(userId) {
    const output = document.getElementById("outUserDetails");
    output.innerHTML = "";

    populateActionBar(userId);

    const containerUserInfo = document.createElement("div");
    containerUserInfo.innerText = "Loading user info...";

    const containerUserGroups = document.createElement("div");
    containerUserGroups.innerText = "Loading user info...";

    const containerPermissions = document.createElement("div");
    containerPermissions.innerText = "Loading permissions...";

    output.appendChild(containerUserInfo);
    output.appendChild(containerUserGroups);
    output.appendChild(containerPermissions);

    loadUserInfo(userId, containerUserInfo).then(() => {
    });
    loadUserGroups(userId, containerUserGroups).then(() => {
    });
    loadUserPermissions(userId, containerPermissions).then(() => {
    });
}

function populateActionBar(userId) {
    const container = document.getElementById("outUserActions");
    container.innerHTML = "";

    const actionBar = document.createElement("div");
    actionBar.classList.add("actionBar");

    const bttnAddNew = document.createElement("button");
    bttnAddNew.appendChild(document.createTextNode("add Permission"));
    bttnAddNew.onclick = () => openAddPermission(userId);
    actionBar.appendChild(bttnAddNew);

    const bttnAdd2Group = document.createElement("button");
    bttnAdd2Group.appendChild(document.createTextNode("add to Group"));
    bttnAdd2Group.onclick = () => openAddToGroup(userId);
    actionBar.appendChild(bttnAdd2Group);

    container.appendChild(actionBar);
}

async function loadUserInfo(userId, outputContainer) {
    fetch(`{{CONTEXT}}/rest/users/id/${userId}`, {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            return response.json();
        })
        .then((data) => {
            outputContainer.innerHTML = "";
            const mail = data["mail"];
            const displayName = data["displayName"];
            const id = data["id"];

            const table = document.createElement("table");

            const tr1 = document.createElement("tr");
            const td1 = document.createElement("td");
            const txtID = document.createElement("p");
            txtID.innerText = "ID";
            td1.appendChild(txtID);
            const td2 = document.createElement("td");
            const txtIdData = document.createElement("p");
            txtIdData.innerText = id;
            td2.appendChild(txtIdData);
            tr1.appendChild(td1);
            tr1.appendChild(td2);
            table.appendChild(tr1);

            const tr2 = document.createElement("tr");
            const td3 = document.createElement("td");
            const txtMail = document.createElement("p");
            txtMail.innerText = "Mail";
            const td4 = document.createElement("td");
            const txtMailData = document.createElement("p");
            txtMailData.innerText = mail;
            td4.appendChild(txtMailData);
            td3.appendChild(txtMail);
            tr2.appendChild(td3);
            tr2.appendChild(td4);
            table.appendChild(tr2);

            const tr3 = document.createElement("tr");
            const td5 = document.createElement("td");
            const txtDisplayName = document.createElement("p");
            txtDisplayName.innerText = "Display Name";
            td5.appendChild(txtDisplayName);
            const td6 = document.createElement("td");
            const txtDisplayNameData = document.createElement("p");
            txtDisplayNameData.innerText = displayName;
            td6.appendChild(txtDisplayNameData);
            tr3.appendChild(td5);
            tr3.appendChild(td6);
            table.appendChild(tr3);

            outputContainer.appendChild(table);
        })
        .catch((error) => {
            outputContainer.innerHTML = "";
            outputContainer.appendChild(document.createTextNode(error.message));
        });
}

async function loadUserGroups(userId, outputContainer) {
    fetch(`{{CONTEXT}}/rest/groups/user/${userId}`, {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            return response.json();
        })
        .then((data) => {
            outputContainer.innerHTML = "";
            const header = document.createElement("h3");
            header.appendChild(document.createTextNode("Groups"));
            outputContainer.appendChild(header);

            const ul = document.createElement("ul");
            ul.classList.add("ulGroups");
            outputContainer.appendChild(ul);

            data["groups"]
                .sort((a, b) => a["name"].localeCompare(b["name"]))
                .forEach((group) => {
                    const li = document.createElement("li");
                    li.appendChild(document.createTextNode(group["name"]));
                    ul.appendChild(li);
                });
        })
        .catch((error) => {
            outputContainer.innerHTML = "";
            outputContainer.appendChild(document.createTextNode(error.message));
        });
}

async function loadUserPermissions(userId, outputContainer) {
    fetch(`{{CONTEXT}}/rest/permission/user/${userId}`, {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            return response.json();
        })
        .then((data) => {
            outputContainer.innerHTML = "";

            const header = document.createElement("h3");
            header.appendChild(document.createTextNode("Permissions"));
            outputContainer.appendChild(header);

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

            permissions
                .sort((a, b) => a["route"].localeCompare(b["route"]))
                .forEach((permission) => table.appendChild(permissionToTableRow(userId, permission)));

            outputContainer.appendChild(table);
        })
        .catch((error) => {
            outputContainer.innerHTML = "";
            outputContainer.appendChild(document.createTextNode(error.message));
        });
}

function permissionToTableRow(userId, permission) {
    const tr = document.createElement("tr");
    const td1 = document.createElement("td");
    const txtRoute = document.createElement("p");
    txtRoute.innerText = permission["route"];
    td1.appendChild(txtRoute);
    const td2 = document.createElement("td");
    td2.appendChild(createCheckboxForPermission(userId, permission["GET"]));
    const td3 = document.createElement("td");
    td3.appendChild(createCheckboxForPermission(userId, permission["POST"]));
    const td4 = document.createElement("td");
    td4.appendChild(createCheckboxForPermission(userId, permission["PUT"]));
    const td5 = document.createElement("td");
    td5.appendChild(createCheckboxForPermission(userId, permission["DELETE"]));
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
    bttn.appendChild(document.createTextNode("X"));
    bttn.onclick = () => deletePermission(bttn, userId);
    td.appendChild(bttn);
    return td;
}

function deletePermission(sender, userId) {
    const route = sender.parentElement.parentElement.children[0].innerText;
    fetch(`{{CONTEXT}}/rest/permission/user/${userId}/route/${encodeURIComponent(route)}`, {
        method: "DELETE", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            openUserDetails(userId);
        })
        .catch((error) => {
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
        route: route, get: get, post: post, put: put, delete: del,
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

function generateRouteSelect() {
    const select = document.createElement("select");
    fetch("{{CONTEXT}}/rest/permission/checked-routes", {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => response.json())
        .then((data) => {
            data["routes"]
                .sort((a, b) => a.localeCompare(b))
                .forEach((route) => {
                    const option = document.createElement("option");
                    option.value = route;
                    option.appendChild(document.createTextNode(route));
                    select.appendChild(option);
                });
        });
    return select;
}

function generateGroupSelect() {
    const select = document.createElement("select");
    fetch("{{CONTEXT}}/rest/groups/all", {
        method: "GET", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => response.json())
        .then((data) => {
            data["groups"]
                .sort((a, b) => a["name"].localeCompare(b["name"]))
                .forEach((group) => {
                    const option = document.createElement("option");
                    option.value = group["id"];
                    option.appendChild(document.createTextNode(group["name"]));
                    select.appendChild(option);
                });
        });
    return select;
}

function openAddPermission(userId) {
    const output = document.createElement("div");

    const lblRoute = document.createElement("label");
    lblRoute.appendChild(document.createTextNode("Route:"));
    output.appendChild(lblRoute);

    const selectRoute = generateRouteSelect();
    output.appendChild(selectRoute);

    const bttn = document.createElement("button");
    bttn.appendChild(document.createTextNode("add"));
    bttn.onclick = () => {
        addPermission(userId, selectRoute.value);
        closePopup(bttn);
    };
    output.appendChild(bttn);

    openPopup(output);
}

function openAddToGroup(userId) {
    const output = document.createElement("div");

    const lblGroup = document.createElement("label");
    lblGroup.appendChild(document.createTextNode("Group:"));
    output.appendChild(lblGroup);

    const selectGroup = generateGroupSelect();
    output.appendChild(selectGroup);

    const bttn = document.createElement("button");
    bttn.appendChild(document.createTextNode("add"));
    bttn.onclick = () => {
        addToGroup(userId, selectGroup.value);
        closePopup(bttn);
    };
    output.appendChild(bttn);

    openPopup(output);
}

function addToGroup(userId, group) {
    fetch(`{{CONTEXT}}/rest/groups/user/${userId}/group/${group}`, {
        method: "POST", headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            openUserDetails(userId);
        })
        .catch((error) => {
            alert(error.message);
        });
}

function addPermission(userId, route) {
    const permission = {
        route: route, get: 0, post: 0, put: 0, delete: 0,
    };
    savePermission(userId, permission, () => {
        openUserDetails(userId);
    }, (error) => {
        alert(error.message);
    });
}

function savePermission(userId, permission, onSuccess = () => {
}, onError = (error) => {
}) {
    fetch(`{{CONTEXT}}/rest/permission/user/${userId}`, {
        method: "POST", headers: {
            "Content-Type": "application/json",
        }, body: JSON.stringify(permission),
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("HTTP error, status = " + response.status);
            }
            onSuccess();
        })
        .catch((error) => {
            onError(error);
        });
}

function openPopup(contentBody) {
    const background = document.createElement("div");
    background.setAttribute("X-Type", "popupBackground");
    background.classList.add("popupBackground");
    background.onclick = (event) => {
        if (event.target === background) {
            closePopup(background);
        }
    };

    const popup = document.createElement("div");
    popup.classList.add("popup");

    const close = document.createElement("button");
    close.appendChild(document.createTextNode("X"));
    close.onclick = () => background.remove();
    popup.appendChild(close);

    const content = document.createElement("div");
    content.appendChild(contentBody);
    popup.appendChild(content);

    background.appendChild(popup);
    document.body.appendChild(background);
}

function closePopup(self) {
    if (self === document.body || self === null || self === undefined) {
        return;
    }

    if (self.getAttribute("X-Type") === "popupBackground") {
        self.remove();
    } else {
        closePopup(self.parentElement);
    }
}
