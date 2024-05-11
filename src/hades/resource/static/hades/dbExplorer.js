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
                bucketLink.innerHTML = bucket;
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
    bucketNameHeader.innerHTML = "Bucket: " + bucketName;
    bucketDetails.appendChild(bucketNameHeader);

    const bucketKeys = document.createElement("ul");
    bucketKeys.id = "bucketKeys";
    bucketDetails.appendChild(bucketKeys);

    loadBucketKeys(bucketName);
}

function loadBucketKeys(bucketName) {
    const bucketKeys = document.getElementById("bucketKeys");
    bucketKeys.innerHTML = "";

    fetch(`{{CONTEXT}}/dbExplorer/${bucketName}/keys`, {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    }).then(response => response.json())
        .then(data => {
            data["keys"].forEach(key => {
                const li = document.createElement("li");

                const keyLink = document.createElement("a");
                keyLink.className = "key";
                keyLink.innerHTML = key;
                keyLink.onclick = () => openValue(bucketName, key);

                li.appendChild(keyLink);
                bucketKeys.appendChild(li);
            });
        });
}

function openValue(bucketName, key) {
    fetch(`{{CONTEXT}}/dbExplorer/${bucketName}/key/${key}`, {
        method: "GET", headers: {
            "Content-Type": "application/json"
        }
    }).then(response => response.json())
        .then(data => {
            const bucketDetails = document.createElement("div");
            bucketDetails.id = "bucketDetails";
            bucketDetails.classList.add("bucketDetails");

            const keyHeader = document.createElement("h3");
            keyHeader.innerHTML = "Key: " + key;
            bucketDetails.appendChild(keyHeader);

            const value = document.createElement("pre");
            value.innerHTML = JSON.stringify(data, null, 2);
            bucketDetails.appendChild(value);

            openPopup(bucketDetails);
        });
}