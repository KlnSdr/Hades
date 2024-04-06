function doLogout() {
    fetch("/rest/users/logout", {
        method: "GET",
    }).then((response) => {
        if (response.status === 200) {
            location.assign("/hades/login/");
        } else {
            alert("Logout failed");
        }
    });
}
