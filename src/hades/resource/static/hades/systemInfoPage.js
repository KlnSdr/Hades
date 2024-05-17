let configFileContent = null;

function openSystemInfo() {
    document.title = "Hades - System Information";

    const centerContainer = document.getElementById("centerContainer");
    centerContainer.innerHTML = "";

    const containerInfoTable = document.createElement("div");
    containerInfoTable.id = "containerInfoTable";
    centerContainer.appendChild(containerInfoTable);

    getSystemInfo();

    const bttnShowConfig = document.createElement("button");
    bttnShowConfig.id = "bttnShowConfig";
    bttnShowConfig.innerText = "View Configuration";
    bttnShowConfig.onclick = () => openShowConfig();
    centerContainer.appendChild(bttnShowConfig);
}

function openShowConfig() {
    if (configFileContent === null) {
        getConfig();
        return;
    }

    const configDetails = document.createElement("div");
    configDetails.id = "bucketDetails";
    configDetails.classList.add("bucketDetails");

    const keyHeader = document.createElement("h3");
    keyHeader.innerText = "application.json";
    configDetails.appendChild(keyHeader);

    const value = document.createElement("pre");
    value.innerText = JSON.stringify(configFileContent, null, 2).replaceAll("\\\\", "\\");
    configDetails.appendChild(value);

    openPopup(configDetails);
}

function getConfig() {
    fetch("{{CONTEXT}}/configFile", {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    }).then(response => response.json())
        .then(data => {
            configFileContent = data;
            openShowConfig();
        });
}

function getSystemInfo() {
    const centerContainer = document.getElementById("containerInfoTable");
    fetch("{{CONTEXT}}/systemInfo", {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    }).then(response => response.json())
        .then(data => {
            const systemInfo = document.createElement("div");
            systemInfo.id = "systemInfo";
            systemInfo.className = "systemInfo";
            centerContainer.appendChild(systemInfo);

            const systemInfoTable = document.createElement("table");
            systemInfoTable.id = "systemInfoTable";
            systemInfo.appendChild(systemInfoTable);

            const systemInfoTableBody = document.createElement("tbody");
            systemInfoTable.appendChild(systemInfoTableBody);

            Object.keys(data).sort().forEach(key => {
                const row = document.createElement("tr");
                systemInfoTableBody.appendChild(row);

                const keyCell = document.createElement("td");
                keyCell.innerText = key;
                row.appendChild(keyCell);

                const valueCell = document.createElement("td");
                valueCell.innerText = data[key];
                row.appendChild(valueCell);
            });
        })
}