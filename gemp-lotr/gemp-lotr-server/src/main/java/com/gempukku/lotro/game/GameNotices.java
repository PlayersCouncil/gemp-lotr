package com.gempukku.lotro.game;

import com.gempukku.polling.LongPollableResource;
import com.gempukku.polling.WaitingRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Things the people at a game are told that are not part of the game itself: somebody has asked for the game to be
 * taken back, the request was settled, the table has changed behind their back and has to be fetched again.
 *
 * They travel apart from the game's own events because a client that is showing its player a decision does not ask
 * for game events until the decision is answered.  That is why a concession used not to be seen until the other
 * player had answered whatever they were looking at.  Notices are asked for all the time, decision or no decision, and
 * never carry anything of the game's state: at most they say "go and fetch it".
 *
 * Each person has a queue of their own, numbered from 1.  A client asks for everything after the last number it saw.
 */
public class GameNotices {
    public static final String UNDO_REQUESTED = "undoRequested";
    public static final String UNDO_RESOLVED = "undoResolved";
    public static final String RESYNC = "resync";

    public record Notice(int number, String type, Map<String, String> fields) { }

    private static final int KEPT = 50;

    private class Queue {
        final List<Notice> notices = new ArrayList<>();
        final List<WaitingRequest> waiting = new ArrayList<>();
        int next = 1;
    }

    private final Map<String, Queue> _queues = new HashMap<>();

    private Queue queue(String person) {
        return _queues.computeIfAbsent(person, name -> new Queue());
    }

    /** Has a queue kept for this person from now on (anybody who has asked for notices, or sits at the game). */
    public synchronized void know(String person) {
        queue(person);
    }

    public synchronized void send(String person, String type, Map<String, String> fields) {
        Queue queue = queue(person);
        queue.notices.add(new Notice(queue.next++, type, new LinkedHashMap<>(fields)));
        while (queue.notices.size() > KEPT)
            queue.notices.remove(0);
        List<WaitingRequest> waiting = new ArrayList<>(queue.waiting);
        queue.waiting.clear();
        for (WaitingRequest request : waiting)
            request.processRequest();
    }

    /** Everybody who has a queue. */
    public synchronized List<String> people() {
        return new ArrayList<>(_queues.keySet());
    }

    /** To everybody who has a queue. */
    public synchronized void sendAll(String type, Map<String, String> fields) {
        for (String person : new ArrayList<>(_queues.keySet()))
            send(person, type, fields);
    }

    /**
     * @return This person's notices numbered above that one, soonest first.
     */
    public synchronized List<Notice> after(String person, int number) {
        List<Notice> result = new ArrayList<>();
        for (Notice notice : queue(person).notices)
            if (notice.number() > number)
                result.add(notice);
        return result;
    }

    /**
     * @return Something to wait on for this person's next notice numbered above that one.
     */
    public LongPollableResource waitingAfter(String person, int number) {
        return new LongPollableResource() {
            @Override
            public boolean registerRequest(WaitingRequest waitingRequest) {
                synchronized (GameNotices.this) {
                    if (!after(person, number).isEmpty())
                        return true;
                    queue(person).waiting.add(waitingRequest);
                    return false;
                }
            }

            @Override
            public void deregisterRequest(WaitingRequest waitingRequest) {
                synchronized (GameNotices.this) {
                    queue(person).waiting.remove(waitingRequest);
                }
            }
        };
    }
}
