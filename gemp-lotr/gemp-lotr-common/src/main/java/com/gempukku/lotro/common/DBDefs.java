package com.gempukku.lotro.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class DBDefs {

    public static class GameHistory {

        public int id;
        public String gameId;

        public String winner;
        public int winnerId;
        public String loser;
        public int loserId;

        public String win_reason;
        public String lose_reason;

        public String win_recording_id;
        public String lose_recording_id;

        public LocalDateTime  start_date;
        public LocalDateTime end_date;

        public ZonedDateTime GetUTCStartDate() {
            return DateUtils.ParseDate(start_date);
        }

        public ZonedDateTime GetUTCEndDate() {
            return DateUtils.ParseDate(end_date);
        }

        public String format_name;

        public String winner_deck_name;
        public String loser_deck_name;

        public String tournament;

        public int winner_site;
        public int loser_site;

        public String game_length_type;
        public int max_game_time;
        public int game_timeout;
        public int winner_clock_remaining;
        public int loser_clock_remaining;

        public int replay_version = -1;
    }

    public static class Collection {
        public int id;
        public int player_id;
        public String type;
        public String extra_info;
    }

    public static class CollectionEntry {
        public int collection_id;
        public int quantity;
        public String product_type;
        public String product_variant;
        public String product;
        public String source;
        public LocalDateTime created_date;
        public LocalDateTime modified_date;
        public ZonedDateTime GetUTCCreatedDate() {
            return ZonedDateTime.of(created_date, DateUtils.UTC);
        }

        public ZonedDateTime GetUTCModifiedDate() {
            return ZonedDateTime.of(modified_date, DateUtils.UTC);
        }
        public String notes;
    }

    public static class Player {
        public int id;
        public String name;
        public String password;
        public String type;
        public Integer last_login_reward;
        public Integer banned_until;
        public String create_ip;
        public String last_ip;

        public Date GetBannedUntilDate()
        {
            if(banned_until == null)
                return null;
            return new Date(banned_until);
        }
    }

    public static class FormatStats {
        public String Format;
        public int Count;
        public boolean Casual;
    }

    public static class PendingTournamentQueue {
        public int id;
        public int scheduled_tournament_id;
        public int player_id;
        public String deck_name; //45
        public String deck; //text
        public boolean dropped;
        public boolean checked_in;
    }

    public static class Tournament {
        public int id;
        public String tournament_id; //255
        public String name; //255
        public LocalDateTime start_date;
        public ZonedDateTime GetUTCStartDate() {
            return ZonedDateTime.of(start_date, DateUtils.UTC);
        }
        public String type; //45
        public String parameters; //5000

        public String stage; //45
        public int round;
    }

    public static class ScheduledTournament {

        //id, tournament_id, name, start_date, parameters, started
        public int id;
        public String tournament_id; //45
        public String name; //255
        public String format; //45
        public LocalDateTime start_date;

        public ZonedDateTime GetUTCStartDate() {
            return ZonedDateTime.of(start_date, DateUtils.UTC);
        }

        public String type; //45
        public String parameters; //5000
        public boolean started;
    }

    public static class TournamentMatch {
        public int id;
        public String tournament_id;
        public int round;
        public String player_one; //45
        public String player_two; //45
        public String winner; //45
    }

    public static class LeagueSchedule {
        public int id;
        public String name; //45
        public String league_type; //45
        public String template; //LeagueParams JSON
        public String events; //JSON array of {name, params}
        public String name_pattern; //255
        public LocalDate next_event_date;
        public int next_event_index;
        public double interval_months;
        public int lead_days;
        public boolean active;
        public Integer last_created_league_id;
        public LocalDateTime last_run;
        public String last_error; //1000
    }

    public static class League {
        public int id;
        public String name;
        public long code;
        public String type;
        public String parameters;
        public LocalDate start_date;
        public LocalDate end_date;
        public int status;
        public int cost;
        public Integer schedule_id;   // the league_schedule row that created this league, or null for a hand-made one

        public ZonedDateTime GetUTCStart() {
            return DateUtils.ParseDate(start_date);
        }
        public ZonedDateTime GetUTCEnd() {
            return DateUtils.ParseDate(end_date);
        }
    }

    /**
     * A holder of one product in a collection: one row of collection_entries joined to its collection and player.
     */
    public static class CollectionHolder {
        public int player_id;
        public String player_name;
        public String collection_type;
        public int quantity;
    }

    /**
     * A promised prize whose card does not exist yet.  Handed out as blueprint "404_&lt;id&gt;" until an admin
     * resolves it to a real card.
     */
    public static class PrizePlaceholder {
        public int id;
        public String label; //255
        public int count = 1;
        public String event_kind; //20: league | tournament | campaign | manual
        public String event_id; //45
        public String event_name; //255
        public Integer tier_index;
        public LocalDateTime created;
        public String created_by; //45
        public String resolved_blueprint; //45
        public LocalDateTime resolved_on;
        public String resolved_by; //45
        public String notes; //1000

        public boolean isResolved() {
            return resolved_blueprint != null;
        }
    }

    /**
     * Log of one prize tier awarded to one player.  Also the dedup record: the same (event, tier, player) is never
     * awarded twice.
     */
    public static class PrizeAward {
        public int id;
        public String event_kind; //20
        public String event_id; //45
        public String event_name; //255
        public Integer tier_index;
        public String tier_label; //255
        public String player; //45
        public String items; //text: one "<count>x <blueprint>" per line
        public LocalDateTime awarded_on;
    }

    public static class Transfer {
        public int id;
        public boolean notify;
        public String player; //45
        public String reason; //255
        public String collection; //255
        public int currency;
        public String contents; //text
        public String message; //text, nullable — human-readable note for the player
        public LocalDateTime date_recorded;
        public String direction; //45

        public ZonedDateTime GetUTCDateRecorded() {
            return DateUtils.ParseDate(date_recorded);
        }
    }

    public static class Announcement {
        public int id;
        public String title; //255
        public String content; //text
        public LocalDateTime start;
        public LocalDateTime until;

        public ZonedDateTime GetUTCStart() {
            return DateUtils.ParseDate(start);
        }
        public ZonedDateTime GetUTCUntil() {
            return DateUtils.ParseDate(until);
        }
    }

    public static class RtmdComparisonVote {
        public int id;
        public String blueprint_a;
        public String blueprint_b;
        public String winner;
        public String ip_address;
        public LocalDateTime voted_at;
    }

    public static class RtmdIdeaSubmission {
        public int id;
        public String idea_text;
        public String ip_address;
        public LocalDateTime submitted_at;
        public int upvotes;
        public int downvotes;
    }

    public static class RtmdIdeaVote {
        public int id;
        public int submission_id;
        public String ip_address;
        public int vote;
        public LocalDateTime voted_at;
    }
}
