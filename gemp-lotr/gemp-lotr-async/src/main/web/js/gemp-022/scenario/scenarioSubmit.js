/**
 * The "are you sure" for submitting a scenario for review, with what to check first and what submitting does.  The
 * same in the Game Hall (My Account > My Scenarios) and in the authoring tool (its Scenario tab), so it is built here
 * with nothing but jQuery: the two pages have different jQuery UI themes, and neither is needed.
 */
var ScenarioSubmit = {
    GUIDELINES: [
        "Play it through yourself first, as a player would. The server plays it through too: one that does not play through cannot be submitted.",
        "Give it a clear title and a summary that says what the player will do or learn, and put your name in Author.",
        "Keep the messages friendly, on topic and accurate: what they say about cards and rules has to be true of the game as Gemp plays it.",
        "A tutorial teaches one thing at a time; a puzzle has a solution a player can find from what they are shown."
    ],

    WHAT_HAPPENS: [
        "It is locked: you cannot change, rename or delete it until an admin has approved or rejected it. A draft of it open in the authoring tool is closed. To keep working on it in the meantime, copy it in My Scenarios.",
        "An admin plays it, and either approves it or sends it back with the changes they would like, which you will see in My Scenarios.",
        "Approved, it becomes a public scenario, offered to every player. If it is a version of a public scenario (copied from one), it replaces that one.",
        "A public scenario is part of the game: the admins may change it, disable it or take it down, and it may be kept with the game's own scenarios. It stays credited to whoever its Author says."
    ],

    /**
     * Asks.  onYes is called if the player says yes; nothing is called if they do not.
     * @param title What the scenario is called, for the heading.
     */
    ask: function (title, onYes) {
        $(".scenario-submit, .scenario-submit-shade").remove();
        var shade = $("<div class='scenario-submit-shade'></div>").appendTo("body");
        var box = $("<div class='scenario-submit' role='dialog' aria-modal='true'></div>").appendTo("body");
        var put = function () {
            shade.remove();
            box.remove();
            $(document).unbind("keydown.scenarioSubmit");
        };
        box.append($("<h2></h2>").text("Submit \u201c" + (title || "this scenario") + "\u201d for review?"));
        box.append($("<h3></h3>").text("Before you submit"));
        var guide = $("<ul></ul>").appendTo(box);
        $.each(this.GUIDELINES, function (i, line) {
            guide.append($("<li></li>").text(line));
        });
        box.append($("<h3></h3>").text("What submitting does"));
        var then = $("<ul></ul>").appendTo(box);
        $.each(this.WHAT_HAPPENS, function (i, line) {
            then.append($("<li></li>").text(line));
        });
        box.append($("<p class='scenario-submit-ask'></p>").text("Are you sure you want to submit it?"));
        var buttons = $("<div class='scenario-submit-buttons'></div>").appendTo(box);
        var yes = $("<button type='button' class='scenario-submit-yes'></button>").text("Submit for review").click(function () {
            put();
            onYes();
        });
        var no = $("<button type='button' class='scenario-submit-no'></button>").text("Cancel").click(put);
        buttons.append(yes).append(no);
        shade.click(put);
        $(document).bind("keydown.scenarioSubmit", function (event) {
            if (event.keyCode == 27)
                put();
        });
        no.focus();
    }
};
