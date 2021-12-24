$(document).ready(() => {
    $("#new-repository").button().on("click", () => {
        window.location.href = '/api/page/repository';
    });
});