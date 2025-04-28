package com.gempukku.lotro.hall;

import com.gempukku.lotro.AbstractServer;
import com.gempukku.lotro.PrivateInformationException;
import com.gempukku.lotro.SubscriptionConflictException;
import com.gempukku.lotro.SubscriptionExpiredException;
import com.gempukku.lotro.chat.ChatCommandCallback;
import com.gempukku.lotro.chat.ChatCommandErrorException;
import com.gempukku.lotro.chat.ChatRoomMediator;
import com.gempukku.lotro.chat.ChatServer;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.IgnoreDAO;
import com.gempukku.lotro.db.vo.CollectionType;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueSerieInfo;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.lotro.logic.vo.LotroDeck;
import com.gempukku.lotro.service.AdminService;
import com.gempukku.lotro.tournament.Tournament;
import com.gempukku.lotro.tournament.TournamentCallback;
import com.gempukku.lotro.tournament.TournamentQueue;
import com.gempukku.lotro.tournament.TournamentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HallServer extends AbstractServer {
    private static final int _playerTableInactivityPeriod = 1000 * 20; // 20 seconds

    private static final int _playerChatInactivityPeriod = 1000 * 60 * 5; // 5 minutes

    private final ChatServer _chatServer;
    private final LeagueService _leagueService;
    private final TournamentService _tournamentService;
    private final LotroCardBlueprintLibrary _library;
    private final LotroFormatLibrary _formatLibrary;
    private final CollectionsManager _collectionsManager;
    private final LotroServer _lotroServer;
    private final AdminService _adminService;

    private final CollectionType _defaultCollectionType = CollectionType.ALL_CARDS;

    private String _motd;

    private boolean _shutdown;

    private final ReadWriteLock _hallDataAccessLock = new ReentrantReadWriteLock(false);

    private final TableHolder tableHolder;

    private final Map<Player, HallCommunicationChannel> _playerChannelCommunication = new ConcurrentHashMap<>();
    private int _nextChannelNumber = 0;

    private final ChatRoomMediator _hallChat;
    private final GameResultListener _notifyHallListeners = new NotifyHallListenersGameResultListener();

    private static final Logger _log = LogManager.getLogger(HallServer.class);

    public HallServer(IgnoreDAO ignoreDAO, LotroServer lotroServer, ChatServer chatServer, LeagueService leagueService, TournamentService tournamentService, LotroCardBlueprintLibrary library,
                      LotroFormatLibrary formatLibrary, CollectionsManager collectionsManager, AdminService adminService) {
        _lotroServer = lotroServer;
        _chatServer = chatServer;
        _leagueService = leagueService;
        _tournamentService = tournamentService;
        _library = library;
        _formatLibrary = formatLibrary;
        _collectionsManager = collectionsManager;
        _adminService = adminService;

        tableHolder = new TableHolder(leagueService, tournamentService, ignoreDAO);

        _hallChat = _chatServer.createChatRoom("Game Hall", true, 300, true,
                "You're now in the Game Hall, use /help to get a list of available commands.<br>Don't forget to check out the new Discord chat integration! Click the 'Switch to Discord' button in the lower right ---->",
                null);
        _hallChat.addChatCommandCallback("ban",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException {
                        if (admin) {
                            _adminService.banUser(parameters.trim());
                        } else {
                            throw new ChatCommandErrorException("Only administrator can ban users");
                        }
                    }
                });
        _hallChat.addChatCommandCallback("banIp",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException {
                        if (admin) {
                            _adminService.banIp(parameters.trim());
                        } else {
                            throw new ChatCommandErrorException("Only administrator can ban users");
                        }
                    }
                });
        _hallChat.addChatCommandCallback("banIpRange",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) throws ChatCommandErrorException {
                        if (admin) {
                            _adminService.banIpPrefix(parameters.trim());
                        } else {
                            throw new ChatCommandErrorException("Only administrator can ban users");
                        }
                    }
                });
        _hallChat.addChatCommandCallback("ignore",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) {
                        final String playerName = parameters.trim();
                        if (playerName.length() >= 2 && playerName.length() <= 30) {
                            if (!from.equals(playerName) && ignoreDAO.addIgnoredUser(from, playerName)) {
                                _hallChat.sendToUser("System", from, "User " + playerName + " added to ignore list");
                            } else if (from.equals(playerName)) {
                                _hallChat.sendToUser(from, from, "You don't have any friends. Nobody likes you.");
                                _hallChat.sendToUser(from, from, "Not listening. Not listening!");
                                _hallChat.sendToUser(from, from, "You're a liar and a thief.");
                                _hallChat.sendToUser(from, from, "Nope.");
                                _hallChat.sendToUser(from, from, "Murderer!");
                                _hallChat.sendToUser(from, from, "Go away. Go away!");
                                _hallChat.sendToUser(from, from, "Hahahaha!");
                                _hallChat.sendToUser(from, from, "I hate you, I hate you.");
                                _hallChat.sendToUser(from, from, "Where would you be without me? Gollum, Gollum. I saved us. It was me. We survived because of me!");
                                _hallChat.sendToUser(from, from, "Not anymore.");
                                _hallChat.sendToUser(from, from, "What did you say?");
                                _hallChat.sendToUser(from, from, "Master looks after us now. We don't need you.");
                                _hallChat.sendToUser(from, from, "What?");
                                _hallChat.sendToUser(from, from, "Leave now and never come back.");
                                _hallChat.sendToUser(from, from, "No!");
                                _hallChat.sendToUser(from, from, "Leave now and never come back!");
                                _hallChat.sendToUser(from, from, "Argh!");
                                _hallChat.sendToUser(from, from, "Leave NOW and NEVER COME BACK!");
                                _hallChat.sendToUser(from, from, "...");
                                _hallChat.sendToUser(from, from, "We... We told him to go away! And away he goes, preciouss! Gone, gone, gone! Smeagol is free!");
                            } else {
                                _hallChat.sendToUser("System", from, "User " + playerName + " is already on your ignore list");
                            }
                        } else {
                            _hallChat.sendToUser("System", from, playerName + " is not a valid username");
                        }
                    }
                });
        _hallChat.addChatCommandCallback("unignore",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) {
                        final String playerName = parameters.trim();
                        if (playerName.length() >= 2 && playerName.length() <= 10) {
                            if (ignoreDAO.removeIgnoredUser(from, playerName)) {
                                _hallChat.sendToUser("System", from, "User " + playerName + " removed from ignore list");
                            } else {
                                _hallChat.sendToUser("System", from, "User " + playerName + " wasn't on your ignore list. Try ignoring them first.");
                            }
                        } else {
                            _hallChat.sendToUser("System", from, playerName + " is not a valid username");
                        }
                    }
                });
        _hallChat.addChatCommandCallback("listIgnores",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) {
                        final Set<String> ignoredUsers = ignoreDAO.getIgnoredUsers(from);
                        _hallChat.sendToUser("System", from, "Your ignores: " + Arrays.toString(ignoredUsers.toArray(new String[0])));
                    }
                });
        _hallChat.addChatCommandCallback("incognito",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) {
                        _hallChat.setIncognito(from, true);
                        _hallChat.sendToUser("System", from, "You are now incognito (do not appear in user list)");
                    }
                });
        _hallChat.addChatCommandCallback("endIncognito",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) {
                        _hallChat.setIncognito(from, false);
                        _hallChat.sendToUser("System", from, "You are no longer incognito");
                    }
                });
        _hallChat.addChatCommandCallback("help",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) {
                        //_hallChat.sendToUser("System", from,
                        String message = """
                                List of available commands:
                                /ignore username - Adds user 'username' to list of your ignores
                                /unignore username - Removes user 'username' from list of your ignores
                                /listIgnores - Lists all your ignored users
                                /incognito - Makes you incognito (not visible in user list)
                                /endIncognito - Turns your visibility 'on' again""";
                        if (admin) {
                            message += """
                                                                
                                                                
                                    Admin only commands:
                                    /ban username - Bans user 'username' permanently
                                    /banIp ip - Bans specified ip permanently
                                    /banIpRange ip - Bans ips with the specified prefix, ie. 10.10.10.""";
                        }

                        _hallChat.sendToUser("System", from, message.replace("\n", "<br />"));
                    }
                });
        _hallChat.addChatCommandCallback("nocommand",
                new ChatCommandCallback() {
                    @Override
                    public void commandReceived(String from, String parameters, boolean admin) {
                        _hallChat.sendToUser("System", from, "\"" + parameters + "\" is not a recognized command.");
                    }
                });
    }

    private void hallChanged() {
        for (var hallCommunicationChannel : _playerChannelCommunication.values())
            hallCommunicationChannel.hallChanged();
    }

    @Override
    protected void doAfterStartup() {
        _hallDataAccessLock.writeLock().lock();
        try {
            _tournamentService.reloadTournaments(tableHolder);
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void setShutdown(boolean shutdown) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            boolean cancelMessage = _shutdown && !shutdown;
            _shutdown = shutdown;
            if (shutdown) {
                cancelWaitingTables();
                _tournamentService.cancelAllTournamentQueues();
                _chatServer.sendSystemMessageToAllChatRooms("@everyone System is entering shutdown mode and will be restarted when all games are finished.");
                hallChanged();
            } else if (cancelMessage) {
                _chatServer.sendSystemMessageToAllChatRooms("@everyone Shutdown mode canceled; games may now resume.");
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public String getMOTD() {
        _hallDataAccessLock.readLock().lock();
        try {
            return _motd;
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    public void setMOTD(String motd) {
        _hallDataAccessLock.writeLock().lock();
        try {
            _motd = motd;
            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public int getTablesCount() {
        _hallDataAccessLock.readLock().lock();
        try {
            return tableHolder.getTableCount();
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    private void cancelWaitingTables() {
        tableHolder.cancelWaitingTables();
    }

    /**
     * @return If table created, otherwise <code>false</code> (if the user already is sitting at a table or playing).
     */
    public void createNewTable(String type, Player player, String deckName, String timer, String description, boolean isInviteOnly, boolean isPrivate, boolean isHidden) throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = createGameSettings(type, timer, description, isInviteOnly, isPrivate, isHidden);

        LotroDeck lotroDeck = validateUserAndDeck(gameSettings.format(), player, deckName, gameSettings.collectionType());

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable table = tableHolder.createTable(player, gameSettings, lotroDeck);
            if (table != null)
                createGameFromTable(table);

            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void spoofNewTable(String type, Player player, Player librarian, String deckName, String timer, String description, boolean isInviteOnly, boolean isPrivate, boolean isHidden) throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = createGameSettings(type, timer, description, isInviteOnly, isPrivate, isHidden);

        LotroDeck lotroDeck = validateUserAndDeck(gameSettings.format(), librarian, deckName, gameSettings.collectionType());

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable table = tableHolder.createTable(player, gameSettings, lotroDeck);
            if (table != null)
                createGameFromTable(table);

            hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private GameSettings createGameSettings(String type, String timer, String description, boolean isInviteOnly, boolean isPrivate, boolean isHidden) throws HallException {
        League league = null;
        LeagueSerieInfo leagueSerie = null;
        CollectionType collectionType = _defaultCollectionType;
        LotroFormat format = _formatLibrary.getHallFormats().get(type);
        GameTimer gameTimer = GameTimer.ResolveTimer(timer);

        if (format == null) {
            // Maybe it's a league format?
            league = _leagueService.getLeagueByType(type);
            if (league != null) {
                leagueSerie = _leagueService.getCurrentLeagueSerie(league);
                if (leagueSerie == null)
                    throw new HallException("There is no ongoing serie for that league");

                if (isInviteOnly) {
                    throw new HallException("League games cannot be invite-only");
                }

                if (isPrivate) {
                    throw new HallException("League games cannot be private");
                }

                //Don't want people getting around the anonymity for leagues.
                if (description != null)
                    description = "";

                format = leagueSerie.getFormat();
                collectionType = leagueSerie.getCollectionType();

                gameTimer = GameTimer.COMPETITIVE_TIMER;
            }
        }
        // It's not a normal format and also not a league one
        if (format == null)
            throw new HallException("This format is not supported: " + type);

        return new GameSettings(collectionType, format, null, league, leagueSerie,
                league != null, isPrivate, isInviteOnly, isHidden, gameTimer, description);
    }

    public boolean joinQueue(String queueId, Player player, String deckName) throws HallException, SQLException, IOException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        _hallDataAccessLock.writeLock().lock();
        try {
            TournamentQueue tournamentQueue = _tournamentService.getTournamentQueue(queueId);
            if (tournamentQueue == null)
                throw new HallException("Tournament queue already finished accepting players, try again in a few seconds");
            if (tournamentQueue.isPlayerSignedUp(player.getName()))
                throw new HallException("You have already joined that queue");

            LotroDeck lotroDeck = null;
            if (tournamentQueue.isRequiresDeck())
                lotroDeck = validateUserAndDeck(_formatLibrary.getFormat(tournamentQueue.getFormatCode()), player, deckName, tournamentQueue.getCollectionType());

            tournamentQueue.joinPlayer(player, lotroDeck);

            hallChanged();

            return true;
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public boolean startQueueEarly(String queueId, Player player) throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        _hallDataAccessLock.writeLock().lock();
        try {
            TournamentQueue tournamentQueue = _tournamentService.getTournamentQueue(queueId);
            if (tournamentQueue == null)
                throw new HallException("Tournament queue already finished accepting players, try again in a few seconds");
            if (!tournamentQueue.isStartable(player.getName()))
                throw new HallException("This queue cannot be started early by " + player.getName());
            if (!tournamentQueue.requestStart(player.getName())) {
                throw new HallException("This queue failed to be started early by " + player.getName());
            }

            _tournamentService.processTournamentQueues(); // Immediately refresh when start request was successful
            _tournamentService.processTournaments(this);

            hallChanged();

            return true;
        } catch (SQLException | IOException ex) {
            throw new RuntimeException("Error during server cleanup.", ex);
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }

    }

    /**
     * @return If table joined, otherwise <code>false</code> (if the user already is sitting at a table or playing).
     */
    public boolean joinTableAsPlayer(String tableId, Player player, String deckName) throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = tableHolder.getGameSettings(tableId);
        LotroDeck lotroDeck = validateUserAndDeck(gameSettings.format(), player, deckName, gameSettings.collectionType());

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable runningTable = tableHolder.joinTable(tableId, player, lotroDeck);
            if (runningTable != null)
                createGameFromTable(runningTable);

            hallChanged();

            return true;
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public boolean joinTableAsPlayerWithSpoofedDeck(String tableId, Player player, Player librarian, String deckName) throws HallException {
        if (_shutdown)
            throw new HallException("Server is in shutdown mode. Server will be restarted after all running games are finished.");

        GameSettings gameSettings = tableHolder.getGameSettings(tableId);
        LotroDeck lotroDeck = validateUserAndDeck(gameSettings.format(), librarian, deckName, gameSettings.collectionType());

        _hallDataAccessLock.writeLock().lock();
        try {
            final GameTable runningTable = tableHolder.joinTable(tableId, player, lotroDeck);
            if (runningTable != null)
                createGameFromTable(runningTable);

            hallChanged();

            return true;
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void leaveQueue(String queueId, Player player) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            TournamentQueue tournamentQueue = _tournamentService.getTournamentQueue(queueId);
            if (tournamentQueue != null && tournamentQueue.isPlayerSignedUp(player.getName())) {
                tournamentQueue.leavePlayer(player);
                hallChanged();
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void confirmReadyCheck(String queueId, Player player) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            TournamentQueue tournamentQueue = _tournamentService.getTournamentQueue(queueId);
            if (tournamentQueue != null && tournamentQueue.isPlayerSignedUp(player.getName())) {
                tournamentQueue.confirmReadyCheck(player.getName());

                _tournamentService.processTournamentQueues(); // Immediately refresh
                _tournamentService.processTournaments(this);

                hallChanged();
            }
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    private boolean leaveQueuesForLeavingPlayer(Player player) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            boolean result = false;
            for (TournamentQueue tournamentQueue : _tournamentService.getAllTournamentQueues()) {
                if (tournamentQueue.isPlayerSignedUp(player.getName())) {
                    tournamentQueue.leavePlayer(player);
                    result = true;
                }
            }
            return result;
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public String dropFromTournament(String tournamentId, Player player) {
        _hallDataAccessLock.writeLock().lock();
        try {
            String result = "";
            Tournament tournament = _tournamentService.getTournamentById(tournamentId);
            if (tournament != null) {
                result = tournament.dropPlayer(player.getName());
                hallChanged();
            }
            else {
                result = "That tournament is already over.";
            }

            return result;
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public String joinTournamentLate(String tournamentId, Player player, String deckName) {
        _hallDataAccessLock.writeLock().lock();
        try {
            String result = "";
            var tournament = _tournamentService.getTournamentById(tournamentId);
            if (tournament == null) {
                result = "That tournament is already over.";
            } else if (!tournament.isJoinable()) {
                result = "That tournament does not allow late joining.";
            } else {
                LotroDeck lotroDeck = null;
                if (tournament.getInfo().Parameters().requiresDeck) {
                    lotroDeck = validateUserAndDeck(_formatLibrary.getFormat(tournament.getFormatCode()), player, deckName, tournament.getCollectionType());
                }
                if (_tournamentService.joinTournamentLate(tournamentId, player.getName(), lotroDeck)) {
                    result = "Joined tournament <b>" + tournament.getTournamentName() + "</b> successfully.";
                } else {
                    result = "Joining tournament <b>" + tournament.getTournamentName() + "</b> failed.";
                }
                hallChanged();
            }

            return result;
        }
        catch(HallException ex) {
            return ex.getMessage();
        }
        finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public String registerLimitedTournamentDeck(String tournamentId, Player player, String deckName) {
        _hallDataAccessLock.writeLock().lock();
        try {
            String result = "";
            var tournament = _tournamentService.getTournamentById(tournamentId);
            if (tournament != null) {
                LotroDeck lotroDeck = validateUserAndDeck(_formatLibrary.getFormat(tournament.getFormatCode()), player, deckName, tournament.getCollectionType());
                var submitted = tournament.playerSubmittedDeck(player.getName(), lotroDeck);

                if(submitted) {
                    result = "Registered deck '" + deckName + "' with tournament '" + tournament.getTournamentName() + "' successfully. "
                            + "If you make an update to your deck, you will need to register it here again for any changes to take effect.";
                    _log.trace("Player '" + player.getName() + "' registered deck '" + deckName + "' for tournament '" + tournament.getTournamentName() + "' successfully.");
                }
                else {
                    result = "Could not register deck with tournament '" + tournament.getTournamentName() + "'. "
                            + "Please contact an administrator if you think this was in error.";

                    _log.trace("Player '" + player.getName() + "' failed to register deck '" + deckName + "' for tournament '" + tournament.getTournamentName() + "'.");
                }

                hallChanged();
            }
            else {
                result = "Registration for that tournament has already closed.";
                _log.trace("Player '" + player.getName() + "' attempted to register deck '" + deckName + "' for tournament '" + tournament.getTournamentName() + "' after registration closed.");
            }

            return result;
        }
        catch(HallException ex) {
            return ex.getMessage();
        }
        finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void leaveAwaitingTable(Player player, String tableId) {
        _hallDataAccessLock.writeLock().lock();
        try {
            if (tableHolder.leaveAwaitingTable(player, tableId))
                hallChanged();
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public boolean leaveAwaitingTablesForLeavingPlayer(Player player) {
        _hallDataAccessLock.writeLock().lock();
        try {
            return tableHolder.leaveAwaitingTablesForPlayer(player);
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    public void signupUserForHall(Player player, HallChannelVisitor hallChannelVisitor) {
        _hallDataAccessLock.readLock().lock();
        try {
            HallCommunicationChannel channel = new HallCommunicationChannel(_nextChannelNumber++);
            channel.processCommunicationChannel(this, player, hallChannelVisitor);
            _playerChannelCommunication.put(player, channel);
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    public HallCommunicationChannel getCommunicationChannel(Player player, int channelNumber) throws SubscriptionExpiredException, SubscriptionConflictException {
        _hallDataAccessLock.readLock().lock();
        try {
            HallCommunicationChannel communicationChannel = _playerChannelCommunication.get(player);
            if (communicationChannel != null) {
                if (communicationChannel.getChannelNumber() == channelNumber) {
                    return communicationChannel;
                } else {
                    throw new SubscriptionConflictException();
                }
            } else {
                throw new SubscriptionExpiredException();
            }
        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    protected void processHall(Player player, HallInfoVisitor visitor) {
        //Commentators are users permitted to watch tournament matches for the purposes of stream broadcasting.
        //TODO: enhance this logic to not apply to tournaments that the commentator is themselves participating in
        final boolean isAdmin = player.hasType(Player.Type.ADMIN) || player.hasType(Player.Type.COMMENTATOR);

        _hallDataAccessLock.readLock().lock();
        try {
            visitor.serverTime(DateUtils.getStringDateWithHour());
            if (_motd != null)
                visitor.motd(_motd);

            tableHolder.processTables(isAdmin, player, visitor);
            _tournamentService.processTournamentsForHall(_formatLibrary, player, visitor);

        } finally {
            _hallDataAccessLock.readLock().unlock();
        }
    }

    private LotroDeck validateUserAndDeck(LotroFormat format, Player player, String deckName, CollectionType collectionType) throws HallException {
        LotroDeck lotroDeck = _lotroServer.getParticipantDeck(player, deckName);
        if (lotroDeck == null) {
            _log.debug("Player '" + player.getName() + "' attempting to use deck '" + deckName + "' but failed.");
            throw new HallException("You don't have a deck registered yet");
        }

        try {
            lotroDeck = format.applyErrata(lotroDeck);
            lotroDeck = validateUserAndDeck(format, player, collectionType, lotroDeck);
        } catch (DeckInvalidException e) {
            throw new HallException("Your selected deck is not valid for this format: " + e.getMessage());
        }

        return lotroDeck;
    }

    private LotroDeck validateUserAndDeck(LotroFormat format, Player player, CollectionType collectionType, LotroDeck lotroDeck) throws HallException, DeckInvalidException {
        String validation = format.validateDeckForHall(lotroDeck);
        if (validation == null || !validation.isEmpty()) {
            throw new DeckInvalidException(validation);
        }

        // Now check if player owns all the cards
        if (collectionType.getCode().equals("default")) {
            CardCollection ownedCollection = _collectionsManager.getPlayerCollection(player, "permanent+trophy");

            LotroDeck filteredSpecialCardsDeck = new LotroDeck(lotroDeck.getDeckName());
            filteredSpecialCardsDeck.setNotes(lotroDeck.getNotes());
            filteredSpecialCardsDeck.setTargetFormat(lotroDeck.getTargetFormat());
            if (lotroDeck.getRing() != null)
                filteredSpecialCardsDeck.setRing(filterCard(lotroDeck.getRing(), ownedCollection));
            filteredSpecialCardsDeck.setRingBearer(filterCard(lotroDeck.getRingBearer(), ownedCollection));

            if (format.usesMaps() && lotroDeck.getMap() != null) {
                filteredSpecialCardsDeck.setMap(filterCard(lotroDeck.getMap(), ownedCollection));
            }

            for (String site : lotroDeck.getSites())
                filteredSpecialCardsDeck.addSite(filterCard(site, ownedCollection));

            for (Map.Entry<String, Integer> cardCount : CollectionUtils.getTotalCardCount(lotroDeck.getDrawDeckCards()).entrySet()) {
                String blueprintId = cardCount.getKey();
                int count = cardCount.getValue();

                int owned = ownedCollection.getItemCount(blueprintId);
                //Since the cards we are validating may be automatic errata IDs, we check and see
                // if the base version of the errata ID is owned in foil, and count that if so.
                if (blueprintId.endsWith("*")) {
                    var ids = format.findBaseCards(_library.getBaseBlueprintId(blueprintId));
                    if (ids.size() == 1) {
                        owned += ownedCollection.getItemCount(ids.stream().findFirst() + "*");
                    }
                }
                int fromOwned = Math.min(owned, count);

                int set = Integer.parseInt(blueprintId.split("_")[0]);

                for (int i = 0; i < fromOwned; i++)
                    filteredSpecialCardsDeck.addCard(blueprintId);
                if (count - fromOwned > 0) {
                    String baseBlueprintId = _library.getBaseBlueprintId(blueprintId);
                    for (int i = 0; i < (count - fromOwned); i++) {
                        //hacking in foil support for errata foils
                        if (set > 19 && blueprintId.endsWith("*")) {
                            filteredSpecialCardsDeck.addCard(blueprintId);
                        } else {
                            filteredSpecialCardsDeck.addCard(baseBlueprintId);
                        }
                    }
                }
            }

            lotroDeck = filteredSpecialCardsDeck;
        } else {
            CardCollection collection = _collectionsManager.getPlayerCollection(player, collectionType.getCode());
            if (collection == null)
                throw new HallException("You don't have cards in the required collection to play in this format");

            Map<String, Integer> deckCardCounts = CollectionUtils.getTotalCardCountForDeck(lotroDeck);

            for (Map.Entry<String, Integer> cardCount : deckCardCounts.entrySet()) {
                String overtID = cardCount.getKey();
                String errataID = format.applyErrata(cardCount.getKey());
                var baseIDs = format.findBaseCards(cardCount.getKey());

                int collectionCount = collection.getItemCount(cardCount.getKey());

                if (!errataID.equals(overtID)) {
                    collection.getItemCount(errataID);
                }

                var alts = _library.getAllAlternates(cardCount.getKey());
                if (alts != null) {
                    for (String id : alts) {
                        collectionCount += collection.getItemCount(id);
                    }
                }

                if (collectionCount < cardCount.getValue()) {
                    String cardName = null;
                    try {
                        cardName = GameUtils.getFullName(_library.getLotroCardBlueprint(cardCount.getKey()));
                        throw new HallException("You don't have the required cards in collection: " + cardName + " required " + cardCount.getValue() + ", owned " + collectionCount);
                    } catch (CardNotFoundException e) {
                        // Ignore, card player has in a collection, should not disappear
                    }
                }
            }
        }
        return lotroDeck;
    }

    private String filterCard(String blueprintId, CardCollection ownedCollection) {
        if (ownedCollection.getItemCount(blueprintId) == 0)
            return _library.getBaseBlueprintId(blueprintId);
        return blueprintId;
    }

    private String getTournamentName(GameTable table) {
        final League league = table.getGameSettings().league();
        if (league != null)
            return league.getName() + " - " + table.getGameSettings().leagueSerie().getName();
        else
            return "Casual - " + table.getGameSettings().timeSettings().name();
    }

    private void createGameFromTable(GameTable gameTable) {
        Set<LotroGameParticipant> players = gameTable.getPlayers();
        LotroGameParticipant[] participants = players.toArray(new LotroGameParticipant[0]);
        final League league = gameTable.getGameSettings().league();
        final LeagueSerieInfo leagueSerie = gameTable.getGameSettings().leagueSerie();

        GameResultListener listener = null;
        if (league != null) {
            listener = new GameResultListener() {
                @Override
                public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                    _leagueService.reportLeagueGameResult(league, leagueSerie, winnerPlayerId, loserPlayerIdsWithReasons.keySet().iterator().next());
                }

                @Override
                public void gameCancelled() {
                    // Do nothing...
                }
            };
        }

        LotroGameMediator mediator = createGameMediator(participants, listener, getTournamentName(gameTable), gameTable.getGameSettings());
        gameTable.startGame(mediator);
    }

    private LotroGameMediator createGameMediator(LotroGameParticipant[] participants, GameResultListener listener, String tournamentName, GameSettings gameSettings) {
        final LotroGameMediator lotroGameMediator = _lotroServer.createNewGame(tournamentName, participants, gameSettings);
        if (listener != null)
            lotroGameMediator.addGameResultListener(listener);
        lotroGameMediator.startGame();
        lotroGameMediator.addGameResultListener(_notifyHallListeners);

        return lotroGameMediator;
    }

    private class NotifyHallListenersGameResultListener implements GameResultListener {
        @Override
        public void gameCancelled() {
            hallChanged();
        }

        @Override
        public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
            hallChanged();
        }
    }

    public TournamentCallback getTournamentCallback(Tournament tourney) {
        return new HallTournamentCallback(tourney);
    }

    private int _tickCounter = 60;

    public void cleanup(boolean forceRefresh) throws SQLException, IOException {
        _hallDataAccessLock.writeLock().lock();
        try {
            boolean changed = tableHolder.removeFinishedGames();

            long currentTime = System.currentTimeMillis();
            Map<Player, HallCommunicationChannel> visitCopy = new LinkedHashMap<>(_playerChannelCommunication);
            for (Map.Entry<Player, HallCommunicationChannel> lastVisitedPlayer : visitCopy.entrySet()) {
                if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + _playerTableInactivityPeriod) {
                    Player player = lastVisitedPlayer.getKey();
                    boolean leftTables = leaveAwaitingTablesForLeavingPlayer(player);
                    boolean leftQueues = leaveQueuesForLeavingPlayer(player);
                    changed |= (leftTables || leftQueues);
                }

                if (currentTime > lastVisitedPlayer.getValue().getLastAccessed() + _playerChatInactivityPeriod) {
                    Player player = lastVisitedPlayer.getKey();
                    _playerChannelCommunication.remove(player);
                }
            }

            if (forceRefresh) {
                _tournamentService.reloadTournaments(tableHolder);
            }

            changed |= _tournamentService.processTournamentQueues();
            changed |= _tournamentService.processTournaments(this);

            if (_tickCounter == 60 || forceRefresh) {
                _tickCounter = 0;
                changed |= _tournamentService.refreshQueues();
            }
            _tickCounter++;

            if (changed) {
                hallChanged();
            }

        } catch (Exception ex) {
            throw new RuntimeException("Error during server cleanup.", ex);
        } finally {
            _hallDataAccessLock.writeLock().unlock();
        }
    }

    @Override
    public void cleanup() throws SQLException, IOException {
        cleanup(false);
    }

    public class HallTournamentCallback implements TournamentCallback {
        private final String tournamentId;
        private final String tournamentName;
        private final GameSettings tournamentGameSettings;

        private final GameSettings wcGameSettings;

        private HallTournamentCallback(Tournament tournament) {
            tournamentId = tournament.getTournamentId();
            tournamentName = tournament.getTournamentName();
            // Tournaments with no prizes and no entry are not competitive
            boolean casual = tournament.getInfo().Parameters().prizes == Tournament.PrizeType.NONE
                            && tournament.getInfo().Parameters().cost == 0;
            tournamentGameSettings = new GameSettings(null, _formatLibrary.getFormat(tournament.getFormatCode()),
                    tournamentId, null, null, !casual, !casual, false,
                    false, GameTimer.TOURNAMENT_TIMER, null);

            wcGameSettings = new GameSettings(null, _formatLibrary.getFormat(tournament.getFormatCode()),
                    tournamentId, null, null, true, true, false,
                    false, GameTimer.CHAMPIONSHIP_TIMER, null);
        }

        @Override
        public void createGame(String playerOne, LotroDeck deckOne, String playerTwo, LotroDeck deckTwo) {
            final LotroGameParticipant[] participants = new LotroGameParticipant[2];
            participants[0] = new LotroGameParticipant(playerOne, deckOne);
            participants[1] = new LotroGameParticipant(playerTwo, deckTwo);
            createGameInternal(participants);
        }

        private void createGameInternal(final LotroGameParticipant[] participants) {
            _hallDataAccessLock.writeLock().lock();
            try {
                if (!_shutdown) {
                    var settings = tournamentGameSettings;
                    if (tournamentId.toLowerCase().contains("wc")) {
                        settings = wcGameSettings;
                    }
                    final GameTable gameTable = tableHolder.setupTournamentTable(settings, participants);
                    final LotroGameMediator mediator = createGameMediator(participants,
                            new GameResultListener() {
                                @Override
                                public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                                    _tournamentService.getTournamentById(tournamentId).reportGameFinished(winnerPlayerId, loserPlayerIdsWithReasons.keySet().iterator().next());
                                }

                                @Override
                                public void gameCancelled() {
                                    createGameInternal(participants);
                                }
                            }, tournamentName, settings);
                    gameTable.startGame(mediator);
                }
            } finally {
                _hallDataAccessLock.writeLock().unlock();
            }
        }

        @Override
        public void broadcastMessage(String message, Collection<String> toWhom) {
            try {
                //check-in callback
                if (toWhom == null ) {
                    _hallChat.sendMessageNoHistory("TournamentSystem", message, true);
                } else {
                    StringBuilder builder = new StringBuilder("TournamentSystemTo:");
                    toWhom.forEach(player -> builder.append(player).append(";"));
                    _hallChat.sendMessageNoHistory(builder.substring(0, builder.length() - 1), message, true);
                }
            } catch (PrivateInformationException exp) {
                // Ignore, sent as admin
            } catch (ChatCommandErrorException e) {
                // Ignore, no command
            }
        }
    }

    public ManualGameSpawner createManualGameSpawner(Tournament tourney, LotroFormat format, GameTimer timer, String description) {
        return new ManualGameSpawner(tourney, format, timer, description);
    }

    public class ManualGameSpawner implements TournamentCallback {
        private final String _tournamentName;
        private final GameSettings _settings;

        private ManualGameSpawner(Tournament tourney, LotroFormat format, GameTimer timer, String description) {
            _tournamentName = tourney.getTournamentName();
            _settings = new GameSettings(null, format,
                    tourney.getTournamentId(), null, null, true, false, false, false, timer, description);
        }

        @Override
        public void createGame(String playerOne, LotroDeck deckOne, String playerTwo, LotroDeck deckTwo) {
            final LotroGameParticipant[] participants = new LotroGameParticipant[2];
            participants[0] = new LotroGameParticipant(playerOne, deckOne);
            participants[1] = new LotroGameParticipant(playerTwo, deckTwo);
            createGameInternal(participants);
        }

        private void createGameInternal(final LotroGameParticipant[] participants) {
            _hallDataAccessLock.writeLock().lock();
            try {
                if (!_shutdown) {
                    final GameTable gameTable = tableHolder.setupTournamentTable(_settings, participants);
                    final LotroGameMediator mediator = createGameMediator(participants,
                            _notifyHallListeners, _tournamentName, _settings);
                    gameTable.startGame(mediator);
                }
            } finally {
                _hallDataAccessLock.writeLock().unlock();
            }
        }

        @Override
        public void broadcastMessage(String message, Collection<String> toWhom) {
            try {
                //check-in callback
                if (toWhom == null) {
                    _hallChat.sendMessageNoHistory("TournamentSystem", message, true);
                } else {
                    StringBuilder builder = new StringBuilder("TournamentSystemTo:");
                    toWhom.forEach(player -> builder.append(player).append(";"));
                    _hallChat.sendMessageNoHistory(builder.substring(0, builder.length() - 1), message, true);
                }
            } catch (PrivateInformationException exp) {
                // Ignore, sent as admin
            } catch (ChatCommandErrorException e) {
                // Ignore, no command
            }
        }
    }
}
