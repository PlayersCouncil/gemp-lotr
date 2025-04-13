$(document).ready(
    function () {
        var ui = new GempLotrTableDraftUI();

        $('body').layout({
            applyDefaultStyles:true,
            onresize:function () {
                ui.layoutUI(true);
            },
            north__minSize:"30%"
        });

        $(".ui-layout-pane").css({"background-color":"#000000"});

        $(window).resize(function () {
            ui.layoutUI(true);
        });

        ui.layoutUI(true);
    });