function openDBExplorer() {
    document.title = "Hades - Database Explorer";

    const centerContainer = document.getElementById("centerContainer");
    centerContainer.innerHTML = "";

    const dbExplorer = document.createElement("div");
    dbExplorer.id = "dbExplorer";
    dbExplorer.className = "dbExplorer";
    centerContainer.appendChild(dbExplorer);

    const allBuckets = document.createElement("div");
    allBuckets.id = "allBuckets";
    dbExplorer.appendChild(allBuckets);

    dbExplorer.appendChild(document.createElement("hr"));

    const bucketDetails = document.createElement("div");
    bucketDetails.id = "bucketDetails";
    dbExplorer.appendChild(bucketDetails);

    loadAllBuckets();
}

function loadAllBuckets() {
    fetch("{{CONTEXT}}/dbExplorer/buckets", {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    }).then(response => response.json())
        .then(data => {
            const allBuckets = document.getElementById("allBuckets");
            allBuckets.innerHTML = "";
            const bucketList = document.createElement("ul");
            allBuckets.appendChild(bucketList);

            data["buckets"].forEach(bucket => {
                const li = document.createElement("li");

                const bucketLink = document.createElement("a");
                bucketLink.className = "bucket";
                bucketLink.innerText = bucket;
                bucketLink.onclick = () => openBucket(bucket);

                li.appendChild(bucketLink);
                bucketList.appendChild(li);
            });
        });
}

function openBucket(bucketName) {
    const bucketDetails = document.getElementById("bucketDetails");
    bucketDetails.innerHTML = "";

    const bucketNameHeader = document.createElement("h2");
    bucketNameHeader.innerText = "Bucket: " + bucketName;
    bucketDetails.appendChild(bucketNameHeader);

    const bucketKeys = document.createElement("table");
    bucketKeys.id = "bucketKeys";
    bucketDetails.appendChild(bucketKeys);

    loadBucketKeys(bucketName);
}

function loadBucketKeys(bucketName) {
    const bucketKeys = document.getElementById("bucketKeys");
    bucketKeys.innerHTML = "";

    fetch("{{CONTEXT}}/dbExplorer/keys", {
        method: "POST", headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({bucket: bucketName})
    }).then(response => response.json())
        .then(data => {
            data["keys"].forEach(key => {
                const tr = document.createElement("tr");

                const keyLink = document.createElement("a");
                keyLink.className = "key";
                keyLink.innerText = key;

                const tdLink = document.createElement("td");
                tdLink.appendChild(keyLink);
                tdLink.addEventListener("dblclick", () => openValue(bucketName, key));

                tr.appendChild(tdLink);

                const deleteButton = document.createElement("button");
                deleteButton.className = "dbExplorerdeleteButton";
                deleteButton.innerText = "X";
                deleteButton.onclick = () => doDeleteItem(bucketName, key);

                const tdButton = document.createElement("td");
                tdButton.appendChild(deleteButton);
                tr.appendChild(tdButton);

                bucketKeys.appendChild(tr);
            });
        });
}

function doDeleteItem(bucketName, key) {
    fetch("{{CONTEXT}}/dbExplorer/delete", {
        method: "POST", headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({bucket: bucketName, key: key})
    }).then(response => {
        if (response.ok) {
            loadBucketKeys(bucketName);
        } else {
            alert("Failed to delete item: " + response.statusText);
        }
    });
}

function openValue(bucketName, key) {
    fetch("{{CONTEXT}}/dbExplorer/read", {
        method: "POST", headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({bucket: bucketName, key: key})
    }).then(response => response.json())
        .then(data => {
            const bucketDetails = document.createElement("div");
            bucketDetails.id = "bucketDetails";
            bucketDetails.classList.add("bucketDetails");

            const keyHeader = document.createElement("h3");
            keyHeader.innerText = "Key: " + key;
            bucketDetails.appendChild(keyHeader);

            const value = document.createElement("pre");
            value.innerText = JSON.stringify(data, null, 2);
            bucketDetails.appendChild(value);

            openPopup(bucketDetails);
        });
}