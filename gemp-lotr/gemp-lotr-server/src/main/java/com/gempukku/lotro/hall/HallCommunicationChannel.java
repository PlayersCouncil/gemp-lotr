package com.gempukku.lotro.hall;

import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.game.Player;
import com.gempukku.polling.LongPollableResource;
import com.gempukku.polling.WaitingRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;

import java.time.Duration;
import java.util.*;

public class HallCommunicationChannel implements LongPollableResource {
    private final int _channelNumber;
    private long _lastConsumed;
    private String _lastMotd;
    private Map<String, Map<String, String>> _tournamentQueuePropsOnClient = new LinkedHashMap<>();
    private Map<String, Map<String, String>> _tournamentPropsOnClient = new LinkedHashMap<>();
    private Map<String, Map<String, String>> _tablePropsOnClient = new LinkedHashMap<>();
    private Set<String> _playedGames = new HashSet<>();
    private volatile boolean _changed;
    private volatile WaitingRequest _waitingRequest;

    public HallCommunicationChannel(int channelNumber) {
        _channelNumber = channelNumber;
    }

    @Override
    public synchronized void deregisterRequest(WaitingRequest waitingRequest) {
        _waitingRequest = null;
    }

    @Override
    public synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (_changed)
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    public synchronized void hallChanged() {
        _changed = true;
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public long getLastAccessed() {
        return _lastConsumed;
    }

    public void processCommunicationChannel(HallServer hallServer, final Player player, final HallChannelVisitor hallChannelVisitor) {
        updateLastAccess();

        hallChannelVisitor.channelNumber(_channelNumber);
        final MutableObject newMotd = new MutableObject();

        final Map<String, Map<String, String>> tournamentQueuesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tablesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tournamentsOnServer = new LinkedHashMap<>();
        final Set<String> playedGamesOnServer = new HashSet<>();

        hallServer.processHall(player,
                new HallInfoVisitor() {
                    @Override
                    public void serverTime(String time) {
                        hallChannelVisitor.serverTime(time);
                    }

                    @Override
                    public void motd(String motd) {
                        newMotd.setValue(motd);
                    }

                    @Override
                    public void visitTable(String tableId, String gameId, boolean watchable, TableStatus status, String statusDescription,
                                           String formatName, String tournamentName, String userDesc, List<String> playerIds, boolean playing,
                                           boolean isPrivate, boolean isInviteOnly, String winner) {
                        Map<String, String> props = new HashMap<>();
                        props.put("gameId", gameId);
                        props.put("watchable", String.valueOf(watchable));
                        props.put("status", String.valueOf(status));
                        props.put("statusDescription", statusDescription);
                        props.put("format", formatName);
                        props.put("userDescription", userDesc);
                        props.put("isPrivate", String.valueOf(isPrivate));
                        props.put("isInviteOnly", String.valueOf(isInviteOnly));
                        props.put("tournament", tournamentName);
                        props.put("players", StringUtils.join(playerIds, ","));
                        props.put("playing", String.valueOf(playing));
                        if (winner != null)
                            props.put("winner", winner);

                        tablesOnServer.put(tableId, props);
                    }

                    @Override
                    public void visitTournamentQueue(String tournamentQueueKey, int cost, String collectionName, String formatName, String type, String tournamentQueueName,
                                                     String tournamentPrizes, String pairingDescription, String startCondition, int playerCount, String playerList, boolean playerSignedUp,
                                                     boolean joinable, boolean startable, int readyCheckSecsRemaining, boolean confirmedReadyCheck, boolean wc, String draftCode,
                                                     boolean recurring, boolean scheduled, boolean displayInWaitingTables) {
                        Map<String, String> props = new HashMap<>();
                        props.put("cost", String.valueOf(cost));
                        props.put("collection", collectionName);
                        props.put("format", formatName);
                        props.put("type", type);
                        props.put("queue", tournamentQueueName);
                        props.put("playerCount", String.valueOf(playerCount));
                        props.put("playerList", playerList);
                        props.put("prizes", tournamentPrizes);
                        props.put("system", pairingDescription);
                        props.put("start", startCondition);
                        props.put("signedUp", String.valueOf(playerSignedUp));
                        props.put("joinable", String.valueOf(joinable));
                        props.put("startable", String.valueOf(startable));
                        props.put("readyCheckSecsRemaining", String.valueOf(readyCheckSecsRemaining));
                        props.put("confirmedReadyCheck", String.valueOf(confirmedReadyCheck));
                        props.put("wc", String.valueOf(wc));
                        if (draftCode != null) {
                            props.put("draftCode", draftCode);
                        }
                        props.put("recurring", String.valueOf(recurring));
                        props.put("scheduled", String.valueOf(scheduled));
                        props.put("displayInWaitingTables", String.valueOf(displayInWaitingTables));

                        tournamentQueuesOnServer.put(tournamentQueueKey, props);
                    }

                    @Override
                    public void visitTournament(String tournamentKey, String collectionName, String formatName, String tournamentName, String type, String pairingDescription,
                                                String tournamentStage, int round, int playerCount, String playerList, boolean playerInCompetition, boolean abandoned, boolean joinable,
                                                long secsRemaining, boolean wc) {
                        Map<String, String> props = new HashMap<>();
                        props.put("collection", collectionName);
                        props.put("format", formatName);
                        props.put("name", tournamentName);
                        props.put("system", pairingDescription);
                        props.put("type", type);
                        props.put("stage", tournamentStage);
                        props.put("round", String.valueOf(round));
                        props.put("playerCount", String.valueOf(playerCount));
                        props.put("playerList", playerList);
                        props.put("signedUp", String.valueOf(playerInCompetition));
                        props.put("abandoned", String.valueOf(abandoned));
                        props.put("joinable", String.valueOf(joinable));
                        props.put("wc", String.valueOf(wc));
                        if (secsRemaining >= 0) {
                            props.put("timeRemaining", DateUtils.HumanDuration(Duration.ofSeconds(secsRemaining)));
                        }

                        tournamentsOnServer.put(tournamentKey, props);
                    }

                    @Override
                    public void runningPlayerGame(String gameId) {
                        playedGamesOnServer.add(gameId);
                    }
                });

        notifyAboutTournamentQueues(hallChannelVisitor, tournamentQueuesOnServer);
        _tournamentQueuePropsOnClient = tournamentQueuesOnServer;

        notifyAboutTournaments(hallChannelVisitor, tournamentsOnServer);
        _tournamentPropsOnClient = tournamentsOnServer;

        notifyAboutTables(hallChannelVisitor, tablesOnServer);
        _tablePropsOnClient = tablesOnServer;

        if (newMotd.getValue() != null && !newMotd.getValue().equals(_lastMotd)) {
            String newMotdStr = (String) newMotd.getValue();
            hallChannelVisitor.motdChanged(newMotdStr);
            _lastMotd = newMotdStr;
        }

        for (String gameId : playedGamesOnServer) {
            if (!_playedGames.contains(gameId))
                hallChannelVisitor.newPlayerGame(gameId);
        }
        _playedGames = playedGamesOnServer;

        _changed = false;
    }

    private void notifyAboutTables(HallChannelVisitor hallChannelVisitor, Map<String, Map<String, String>> tablesOnServer) {
        for (Map.Entry<String, Map<String, String>> tableOnClient : _tablePropsOnClient.entrySet()) {
            String tableId = tableOnClient.getKey();
            Map<String, String> tableProps = tableOnClient.getValue();
            Map<String, String> tableLatestProps = tablesOnServer.get(tableId);
            if (tableLatestProps != null) {
                if (!tableProps.equals(tableLatestProps))
                    hallChannelVisitor.updateTable(tableId, tableLatestProps);
            } else {
                hallChannelVisitor.removeTable(tableId);
            }
        }

        for (Map.Entry<String, Map<String, String>> tableOnServer : tablesOnServer.entrySet())
            if (!_tablePropsOnClient.containsKey(tableOnServer.getKey()))
                hallChannelVisitor.addTable(tableOnServer.getKey(), tableOnServer.getValue());
    }

    private void notifyAboutTournamentQueues(HallChannelVisitor hallChannelVisitor, Map<String, Map<String, String>> tournamentQueuesOnServer) {
        for (Map.Entry<String, Map<String, String>> tournamentQueueOnClient : _tournamentQueuePropsOnClient.entrySet()) {
            String tournamentQueueId = tournamentQueueOnClient.getKey();
            Map<String, String> tournamentProps = tournamentQueueOnClient.getValue();
            Map<String, String> tournamentLatestProps = tournamentQueuesOnServer.get(tournamentQueueId);
            if (tournamentLatestProps != null) {
                if (!tournamentProps.equals(tournamentLatestProps))
                    hallChannelVisitor.updateTournamentQueue(tournamentQueueId, tournamentLatestProps);
            } else {
                hallChannelVisitor.removeTournamentQueue(tournamentQueueId);
            }
        }

        for (Map.Entry<String, Map<String, String>> tournamentQueueOnServer : tournamentQueuesOnServer.entrySet())
            if (!_tournamentQueuePropsOnClient.containsKey(tournamentQueueOnServer.getKey()))
                hallChannelVisitor.addTournamentQueue(tournamentQueueOnServer.getKey(), tournamentQueueOnServer.getValue());
    }

    private void notifyAboutTournaments(HallChannelVisitor hallChannelVisitor, Map<String, Map<String, String>> tournamentsOnServer) {
        for (Map.Entry<String, Map<String, String>> tournamentOnClient : _tournamentPropsOnClient.entrySet()) {
            String tournamentId = tournamentOnClient.getKey();
            Map<String, String> tournamentProps = tournamentOnClient.getValue();
            Map<String, String> tournamentLatestProps = tournamentsOnServer.get(tournamentId);
            if (tournamentLatestProps != null) {
                if (!tournamentProps.equals(tournamentLatestProps))
                    hallChannelVisitor.updateTournament(tournamentId, tournamentLatestProps);
            } else {
                hallChannelVisitor.removeTournament(tournamentId);
            }
        }

        for (Map.Entry<String, Map<String, String>> tournamentQueueOnServer : tournamentsOnServer.entrySet())
            if (!_tournamentPropsOnClient.containsKey(tournamentQueueOnServer.getKey()))
                hallChannelVisitor.addTournament(tournamentQueueOnServer.getKey(), tournamentQueueOnServer.getValue());
    }
}
