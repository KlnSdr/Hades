function doLogout() {
    fetch("{{CONTEXT}}/rest/users/logout", {
        method: "GET",
    }).then((response) => {
        if (response.status === 200) {
            location.assign("{{CONTEXT}}/hades/login/");
        } else {
            alert("Logout failed");
        }
    });
}
