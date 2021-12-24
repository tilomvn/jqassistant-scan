var resizeBody = () => {
    let top = $('#divBody').height() * 45 / 100;
    $($('#divBody div')).css('margin-top', top + 'px');
};

$(document).ready(() => {
    resizeBody();
});
