function openSystemInfo() {
    document.title = "Hades - System Information";

    const centerContainer = document.getElementById("centerContainer");
    centerContainer.innerHTML = "";

    getSystemInfo();
}

function getSystemInfo() {
    const centerContainer = document.getElementById("centerContainer");
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