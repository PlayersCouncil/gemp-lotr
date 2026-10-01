var dialogsSized = new Array();

function openSizeDialog(dialog) {
    var sizedDialog = function () {
        for (var i = 0; i < dialogsSized.length; i++)
            if (dialogsSized[i] == dialog)
                return true;
        return false;
    };

    if (!sizedDialog(dialog)) {
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var dialogWidth = windowWidth * 0.8;
        var dialogHeight = windowHeight * 0.8;

        dialogsSized.push(dialog);
        dialog.dialog({width:dialogWidth, height:dialogHeight});
    }
    dialog.dialog("open");
}

// ==== tabs-account: shared table, time and error helpers ====

/**
 * Builds a <table> from data, escaping every value.
 *
 *   renderTable(columns, rows, {sortable, emptyText, className, sort: {key, dir}})  -> jQuery <table>
 *
 * columns: [{key, title, render, sortValue, sortable, className, headerTitle}]
 *   key        property of each row shown when there is no render function
 *   render     function (row, rowIndex) returning a string (inserted as text, so escaped), a DOM node or a jQuery
 *              object (inserted as is: build it with .text()/.attr()); null/undefined shows as empty
 *   sortValue  function (row) giving the value to sort on (default: row[key]); numbers sort numerically,
 *              strings ignoring case with numbers inside them in numeric order; empty values always sort last
 *   sortable   per-column override of options.sortable
 * options.sortable  headers become buttons that sort ascending, then descending (aria-sort on the <th>)
 * options.emptyText shown in a single full-width row when there are no rows (default "Nothing to show.")
 * options.sort      initial sort, e.g. {key: "count", dir: "desc"}
 */
function renderTable(columns, rows, options) {
    options = options || {};
    columns = columns || [];
    rows = (rows || []).slice();
    var table = $("<table class='gemp-table'></table>");
    if (options.className)
        table.addClass(options.className);
    var sortState = options.sort ? {key: options.sort.key, dir: options.sort.dir == "desc" ? "desc" : "asc"} : null;

    var isSortable = function (column) {
        return column.sortable !== undefined ? !!column.sortable : !!options.sortable;
    };
    var valueOf = function (column, row) {
        if (column.sortValue)
            return column.sortValue(row);
        return row == null ? null : row[column.key];
    };
    var compareValues = function (a, b) {
        var aEmpty = a === null || a === undefined || a === "" || (typeof a == "number" && isNaN(a));
        var bEmpty = b === null || b === undefined || b === "" || (typeof b == "number" && isNaN(b));
        if (aEmpty || bEmpty)
            return aEmpty === bEmpty ? 0 : (aEmpty ? 1 : -1);
        if (typeof a == "number" && typeof b == "number")
            return a - b;
        return String(a).localeCompare(String(b), undefined, {numeric: true, sensitivity: "base"});
    };

    var thead = $("<thead></thead>");
    var headRow = $("<tr></tr>");
    var tbody = $("<tbody></tbody>");
    thead.append(headRow);
    table.append(thead).append(tbody);

    var headers = [];
    $.each(columns, function (index, column) {
        var th = $("<th scope='col'></th>");
        if (column.className)
            th.addClass(column.className);
        if (column.headerTitle)
            th.attr("title", column.headerTitle);
        var title = column.title == null ? "" : String(column.title);
        if (isSortable(column)) {
            var button = $("<button type='button' class='gemp-table-sort'></button>").text(title)
                .append("<span class='gemp-table-sort-mark' aria-hidden='true'></span>");
            button.on("click", function () {
                var dir = sortState && sortState.key === column.key && sortState.dir === "asc" ? "desc" : "asc";
                sortState = {key: column.key, dir: dir};
                fillBody();
            });
            th.append(button);
        } else {
            th.text(title);
        }
        headers.push(th);
        headRow.append(th);
    });

    var fillBody = function () {
        tbody.empty();
        var shown = rows;
        if (sortState) {
            var sortColumn = null;
            $.each(columns, function (i, c) { if (c.key === sortState.key) sortColumn = c; });
            if (sortColumn) {
                var sign = sortState.dir === "desc" ? -1 : 1;
                shown = rows.slice().sort(function (a, b) {
                    var va = valueOf(sortColumn, a), vb = valueOf(sortColumn, b);
                    var result = compareValues(va, vb);
                    // empty values stay last whatever the direction
                    var aEmpty = va === null || va === undefined || va === "";
                    var bEmpty = vb === null || vb === undefined || vb === "";
                    if (aEmpty !== bEmpty)
                        return result;
                    return sign * result;
                });
            }
        }
        $.each(columns, function (i, column) {
            var th = headers[i];
            if (!isSortable(column))
                return;
            var mark = th.find(".gemp-table-sort-mark");
            if (sortState && sortState.key === column.key) {
                th.attr("aria-sort", sortState.dir === "asc" ? "ascending" : "descending");
                mark.text(sortState.dir === "asc" ? " ▲" : " ▼");
            } else {
                th.attr("aria-sort", "none");
                mark.text("");
            }
        });

        if (shown.length === 0) {
            var emptyCell = $("<td class='gemp-table-empty'></td>").attr("colspan", Math.max(1, columns.length))
                .text(options.emptyText == null ? "Nothing to show." : String(options.emptyText));
            tbody.append($("<tr></tr>").append(emptyCell));
            return;
        }
        $.each(shown, function (rowIndex, row) {
            var tr = $("<tr></tr>");
            if (options.rowClass) {
                var rowClass = options.rowClass(row);
                if (rowClass)
                    tr.addClass(rowClass);
            }
            $.each(columns, function (i, column) {
                var td = $("<td></td>");
                if (column.className)
                    td.addClass(column.className);
                var value = column.render ? column.render(row, rowIndex) : (row == null ? null : row[column.key]);
                if (value === null || value === undefined)
                    td.text("");
                else if (value instanceof $ || (value && value.nodeType))
                    td.append(value);
                else
                    td.text(String(value));
                tr.append(td);
            });
            tbody.append(tr);
        });
    };
    fillBody();
    return table;
}

/**
 * Formats a moment given in epoch milliseconds for display in the viewer's own time zone.
 *   formatTime(ms)                     -> "2026-09-24 14:05" (local)
 *   formatTime(ms, {relative: true})   -> "just now", "12 minutes ago", "in 3 hours", "yesterday"; beyond a week
 *                                         the absolute local date and time instead
 *   options.now   the "now" to compare with (epoch ms; default the current time)
 *   options.date  true = the date only
 * Returns "" for a missing or invalid value.
 */
function formatTime(epochMs, options) {
    options = options || {};
    var ms = typeof epochMs == "string" ? parseInt(epochMs, 10) : epochMs;
    if (ms === null || ms === undefined || typeof ms != "number" || isNaN(ms))
        return "";
    var date = new Date(ms);
    if (isNaN(date.getTime()))
        return "";

    if (options.relative) {
        var now = options.now != null ? options.now : Date.now();
        var diffSeconds = Math.round((ms - now) / 1000);
        var abs = Math.abs(diffSeconds);
        if (abs < 45)
            return "just now";
        var units = [["minute", 60, 3600], ["hour", 3600, 86400], ["day", 86400, 7 * 86400]];
        for (var i = 0; i < units.length; i++) {
            if (abs < units[i][2]) {
                var amount = Math.round(diffSeconds / units[i][1]);
                if (amount === 0)
                    amount = diffSeconds < 0 ? -1 : 1;
                return formatTime.relativeText(amount, units[i][0]);
            }
        }
    }

    var two = function (n) { return (n < 10 ? "0" : "") + n; };
    var text = date.getFullYear() + "-" + two(date.getMonth() + 1) + "-" + two(date.getDate());
    if (!options.date)
        text += " " + two(date.getHours()) + ":" + two(date.getMinutes());
    return text;
}

formatTime.relativeText = function (amount, unit) {
    try {
        if (typeof Intl !== "undefined" && Intl.RelativeTimeFormat)
            return new Intl.RelativeTimeFormat("en", {numeric: "auto"}).format(amount, unit);
    } catch (e) {
        // fall through to the plain English version
    }
    var n = Math.abs(amount);
    var words = n + " " + unit + (n == 1 ? "" : "s");
    return amount < 0 ? words + " ago" : "in " + words;
};

/** A length of time in milliseconds as "45 s", "12 min" or "1 h 05 min"; "" for a missing or negative value. */
function formatDuration(ms) {
    if (ms === null || ms === undefined || isNaN(ms) || ms < 0)
        return "";
    var seconds = Math.round(ms / 1000);
    if (seconds < 60)
        return seconds + " s";
    var minutes = Math.round(seconds / 60);
    if (minutes < 60)
        return minutes + " min";
    var hours = Math.floor(minutes / 60);
    var rest = minutes % 60;
    return hours + " h " + (rest < 10 ? "0" : "") + rest + " min";
}

/**
 * An error map (for the communication wrappers) that just says what went wrong, in plain words, inside target
 * (a jQuery element; the message replaces its content), or in an alert when there is no target.  For pages such as
 * Help that are not part of the hall's own flows and should not show its tournament / session dialogs.
 *   what: what was being loaded, e.g. "the format definitions"
 */
function neutralErrorMap(target, what) {
    var subject = what || "this page";
    var show = function (text) {
        if (target && target.length) {
            target.empty().append($("<p class='load-error'></p>").text(text));
        } else {
            alert(text);
        }
    };
    var unavailable = function () {
        show("Could not load " + subject + " right now. Please try again in a few minutes.");
    };
    return {
        "0": function () { show("Could not reach the server to load " + subject + ". Check your connection and reload the page."); },
        "400": function () { show("Could not load " + subject + " (the server did not understand the request)."); },
        "401": function () { show("You are not logged in. Log in again to see " + subject + "."); },
        "403": function () { show("You do not have access to " + subject + "."); },
        "404": function () { show(subject.charAt(0).toUpperCase() + subject.substring(1) + " could not be found."); },
        "500": unavailable,
        "502": unavailable,
        "503": unavailable,
        "504": unavailable
    };
}
// ==== end tabs-account ====
