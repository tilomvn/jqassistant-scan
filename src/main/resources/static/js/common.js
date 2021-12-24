var openDialog = (url, isPopup) => {
  if (isPopup) {
      window.open(url, '_blank', 'left=150,top=150,width=645,height=605,modal=yes');
  } else {
      window.open(url, '_blank');
  }
};

$(window).resize(() => {
    clearTimeout(window.resizedFinished);
    window.resizedFinished = setTimeout(() => { resizeBody(); }, 250);
});