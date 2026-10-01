package com.gempukku.lotro.league;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A recurring league definition: a shared LeagueParams template, an ordered list of events that each override part
 * of it, and the cadence at which the server turns the next event into a real league.
 * <p>
 * The template and each event's overrides are kept as JSON objects rather than LeagueParams instances so that an
 * event can override any subset of fields (including primitives such as cost) - the merge is done key by key.
 */
public class LeagueSchedule {
    public static final String DEFAULT_NAME_PATTERN = "{series} - {event}";

    /**
     * One entry in the rotation.  {@code overrides} is a JSON object whose keys replace the template's keys; a
     * missing or empty object means "use the template as-is".
     */
    public record Event(String name, JSONObject overrides) {
        public Event {
            if (overrides == null)
                overrides = new JSONObject();
        }
    }

    private final int _id;
    private final String _name;
    private final League.LeagueType _type;
    private final JSONObject _template;
    private final List<Event> _events;
    private final String _namePattern;
    private final LocalDate _nextEventDate;
    private final int _nextEventIndex;
    private final double _intervalMonths;
    private final int _leadDays;
    private final boolean _active;
    private final Integer _lastCreatedLeagueId;
    private final LocalDateTime _lastRun;
    private final String _lastError;

    public LeagueSchedule(DBDefs.LeagueSchedule row) {
        _id = row.id;
        _name = row.name;
        _type = League.LeagueType.parse(row.league_type);
        _template = parseObject(row.template, "template");
        _events = parseEvents(row.events);
        _namePattern = StringUtils.isBlank(row.name_pattern) ? DEFAULT_NAME_PATTERN : row.name_pattern;
        _nextEventDate = row.next_event_date;
        _nextEventIndex = row.next_event_index;
        _intervalMonths = row.interval_months;
        _leadDays = row.lead_days;
        _active = row.active;
        _lastCreatedLeagueId = row.last_created_league_id;
        _lastRun = row.last_run;
        _lastError = row.last_error;
    }

    public static JSONObject parseObject(String json, String what) {
        if (StringUtils.isBlank(json))
            return new JSONObject();
        try {
            JSONObject parsed = JSON.parseObject(json);
            return parsed == null ? new JSONObject() : parsed;
        } catch (Exception exp) {
            throw new IllegalArgumentException("League schedule " + what + " is not a JSON object: " + exp.getMessage(), exp);
        }
    }

    public static List<Event> parseEvents(String json) {
        var events = new ArrayList<Event>();
        if (StringUtils.isBlank(json))
            return events;
        JSONArray array;
        try {
            array = JSON.parseArray(json);
        } catch (Exception exp) {
            throw new IllegalArgumentException("League schedule events are not a JSON array: " + exp.getMessage(), exp);
        }
        if (array == null)
            return events;
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj == null)
                throw new IllegalArgumentException("League schedule event " + (i + 1) + " is not a JSON object.");
            String name = obj.getString("name");
            JSONObject overrides = obj.getJSONObject("params");
            events.add(new Event(name == null ? "" : name, overrides));
        }
        return events;
    }

    public static String serializeEvents(List<Event> events) {
        var array = new JSONArray();
        for (Event event : events) {
            var obj = new JSONObject();
            obj.put("name", event.name());
            obj.put("params", event.overrides());
            array.add(obj);
        }
        return array.toJSONString();
    }

    /**
     * Builds the LeagueParams for one event: template with the event's overrides applied key by key.  The name,
     * start and code are left for the scheduler to fill in.
     * <p>
     * A race schedule randomises its path per instance unless the definition says otherwise: a template that does
     * not mention {@code raceRandomizeEachInstance} gets it turned on, so a schedule written before the flag
     * existed - or one whose template carries no path at all - produces a freshly rolled race every time instead of
     * the same one forever (or, for a pathless template, a validation failure).
     */
    public LeagueParams buildParams(int eventIndex) {
        var merged = new JSONObject(_template);
        if (eventIndex >= 0 && eventIndex < _events.size())
            merged.putAll(_events.get(eventIndex).overrides());
        merged.remove("name");
        merged.remove("start");
        merged.remove("code");
        boolean randomizeSpecified = merged.containsKey("raceRandomizeEachInstance");
        LeagueParams params = JsonUtils.Convert(merged.toJSONString(), LeagueParams.class);
        if (params == null)
            throw new IllegalArgumentException("League schedule template could not be read as league parameters.");
        params.code = 0;
        if (_type == League.LeagueType.RTMD && !randomizeSpecified)
            params.raceRandomizeEachInstance = true;
        return params;
    }

    public int getId() { return _id; }
    public String getName() { return _name; }
    public League.LeagueType getType() { return _type; }
    public JSONObject getTemplate() { return _template; }
    public List<Event> getEvents() { return Collections.unmodifiableList(_events); }
    public String getNamePattern() { return _namePattern; }
    public LocalDate getNextEventDate() { return _nextEventDate; }
    public int getNextEventIndex() { return _nextEventIndex; }
    public double getIntervalMonths() { return _intervalMonths; }
    public int getLeadDays() { return _leadDays; }
    public boolean isActive() { return _active; }
    public Integer getLastCreatedLeagueId() { return _lastCreatedLeagueId; }
    public LocalDateTime getLastRun() { return _lastRun; }
    public String getLastError() { return _lastError; }
}
