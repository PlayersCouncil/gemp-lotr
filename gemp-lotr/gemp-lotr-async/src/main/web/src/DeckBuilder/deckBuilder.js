$(document).ready(
    function () {
        var ui = new GempLotrDeckBuildingUI();

        $('body').layout({
            applyDefaultStyles:true,
            onresize:function () {
                ui.layoutUI(true);
            },
            east__minSize:400,
            east__maxSize:"50%"
        });

        $(".ui-layout-pane").css({"background-color":"#000000"});

        $(window).resize(function () {
            ui.layoutUI(true);
        });

        ui.layoutUI(true);
    });