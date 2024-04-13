package com.gempukku.lotro.common;

import com.sun.source.doctree.IndexTree;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
            return ZonedDateTime.of(start_date, ZoneId.of("UTC"));
        }

        public ZonedDateTime GetUTCEndDate() {
            return ZonedDateTime.of(end_date, ZoneId.of("UTC"));
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
            return ZonedDateTime.of(created_date, ZoneId.of("UTC"));
        }

        public ZonedDateTime GetUTCModifiedDate() {
            return ZonedDateTime.of(modified_date, ZoneId.of("UTC"));
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
        public LocalDateTime start_date;
        public ZonedDateTime GetUTCStartDate() {
            return ZonedDateTime.of(start_date, ZoneId.of("UTC"));
        }
        public String draft_type; //45
        public String name; //255
        public String format; //255
        public String collection; //255
        public String stage; //45
        public int round;
        public boolean manual_kickoff;
        public String pairing; //45
        public String prizes; //45


    }

    public static class ScheduledTournament {
        public int id;
        public String tournament_id; //45
        public String name; //255
        public String format; //45
        public LocalDateTime start_date;

        public ZonedDateTime GetUTCStartDate() {
            return ZonedDateTime.of(start_date, ZoneId.of("UTC"));
        }

        public int cost;
        public String playoff; //45
        public String tiebreaker; //45
        public String prizes; //45
        public int minimum_players;
        public boolean manual_kickoff;
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
}
