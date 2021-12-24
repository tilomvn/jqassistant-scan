var SSH = 'SSH';
var HTTP = 'HTTP';

var requestModel = {
    branch: null,
    repoUrl: null,
    authenticationType: SSH,
    userName: null,
    password: null,
    passPhase: null
};

var tips;
var allFields;
var authType = SSH;
var branch;
var repoUrl;
var userName;
var password;
var passPhase;

var invalidRepoUrl = (text) => {
    let reg = /\s/g;
    let reg1 = /\'/g;
    let reg2 = /\"/g;
    let reg3 = /./;
    return reg.test(text) || reg1.test(text) || reg2.test(text) || !reg3.test(text);
};

var validBranch = (text) => {
    let reg = /[a-zA-z0-9]+$/;
    return reg.test(text);
};

var validUsername = (text) => {
    let reg = /[a-zA-z0-9]+$/;
    return reg.test(text);
};

var validPassword = (text) => {
    let reg = /./;
    return reg.test(text);
};

var updateTips = (text) => {
    tips.text(text).addClass("ui-state-highlight");
    setTimeout(() => { tips.removeClass("ui-state-highlight", 1500); }, 500);
};

var dialogResult, backgroundWaiting, responseData = {};

var initForm = () => {
    tips = $(".validateTips");

    branch = $("#branch");
    repoUrl = $("#repoUrl");
    passPhase = $("#passPhase");
    userName = $("#userName");
    password = $("#password");

    allFields = $([]).add(branch).add(repoUrl).add(userName).add(password).add(passPhase);

    let dialog = $("#registrationForm").dialog({
      autoOpen: true,
      height: 550,
      width: 550,
      modal: false,
      closeOnEscape: false,
      dialogClass: "no-close",
      buttons: [{
           text: "Create",
           icon: "ui-icon-plus",
           click: function() {
              allFields.removeClass("ui-state-error");
              if (!validBranch(branch.val())) {
                  branch.addClass("ui-state-error");
                  updateTips("Branch must contain characters in [a-zA-Z0-9]");
                  return false;
              }
              if (invalidRepoUrl(repoUrl.val())) {
                  repoUrl.addClass("ui-state-error");
                  updateTips("Repository must contain any characters except [', \" space]");
                  return false;
              }
              if (authType == HTTP) {
                  if (!validUsername(userName.val())) {
                      userName.addClass("ui-state-error");
                      updateTips("Username must contain characters in [a-zA-Z0-9]");
                      return false;
                  }
                  if (!validPassword(password.val())) {
                      password.addClass("ui-state-error");
                      updateTips("Password must contain at least one character");
                      return false;
                  }
              }

              requestModel.branch = branch.val();
              requestModel.repoUrl = repoUrl.val();
              requestModel.authenticationType = authType;
              requestModel.userName = userName.val();
              requestModel.password = password.val();
              requestModel.passPhase = passPhase.val();

              let reqBody = JSON.stringify(requestModel);

              backgroundWaiting.dialog("open");

              $.ajax({
                  type: "POST",
                  data: reqBody,
                  url: '/api/repository/add',
                  contentType: "application/json",
                  dataType: "json",
                  success: data => {
                      backgroundWaiting.dialog("close");
                      responseData = data;
                      dialogResult.dialog("open");
                  },
                  error: data => {
                      backgroundWaiting.dialog("close");
                      dialogResult.dialog("open");
                  }
              });
         }
      }],
      close: function() { return false; }
    });
    dialog.dialog("option", "title", "New Repository");

    $("input[type=radio]").checkboxradio();
    $("input[type=radio]").on("click", (e) => {
        authType = $(e.target).val();
        if (authType == HTTP) { $('.httpAuthentication').show(); $('.sshPassPhase').hide(); }
        else { $('.httpAuthentication').hide(); $('.sshPassPhase').show(); }
    });

    dialogResult = $("#registrationResult").dialog({
      autoOpen: false,
      height: "auto",
      width: 700,
      modal: true,
      buttons: {},
      open: function(e, ui) {
          let displayMessage = "Registration Successfully";
          if (responseData.status == "SERVER_ERROR") {
              displayMessage = "Internal Server Error. Please contact Sgh Team";
          } else if (responseData.status == "EXISTED") {
              displayMessage = "Already Registered";
          } else {
              if (responseData.transportType == SSH) {
                  displayMessage = responseData.publicSshKey.substring(0, 70) + "...";
                  let title = $("<p>").html("Please use the below key to add to your repository's SSH key in order to allow us to clone. Once you close this dialog, use can not get key again.");
                  $("#message").before(title);
                  dialogResult.dialog("option", "buttons", [{
                                                               text: "Copy Key",
                                                               icon: "ui-icon-copy",
                                                               click: function() {
                                                                    clipboard.writeText(responseData.publicSshKey);
                                                                    dialogResult.dialog("close");
                                                               }
                                                           }]);
              }
          }
          $("#message").html(displayMessage);
      },
      close: function() { responseData = {}; dialogResult.dialog("option", "buttons", []); $("#message").prev().remove(); $("#message").html(""); }
    });
    dialogResult.dialog("option", "title", "Registration Result");

    backgroundWaiting = $("#backgroundWaiting").dialog({
        autoOpen: false,
        height: "auto",
        width: "auto",
        modal: true,
        classes: {
            "ui-dialog": "none",
            "ui-dialog-titlebar": "noneDisplay"
        }
    });
};

var resizeBody = () => {
    //let top = $('#divBody').height() * 45 / 100;
    //$($('#divBody div')).css('margin-top', top + 'px');
};

$(document).ready(() => {
    initForm();
    resizeBody();
});
