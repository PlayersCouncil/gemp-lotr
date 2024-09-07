package com.gempukku.lotro.game;

import com.alibaba.fastjson2.JSON;
import com.gempukku.lotro.common.AppConfig;
import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.state.EventSerializer;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.logic.vo.LotroDeck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class GameRecorder {
    private static final String _possibleChars = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int _charsCount = _possibleChars.length();

    private final GameHistoryService _gameHistoryService;
    private final PlayerDAO _playerDAO;

    public GameRecorder(GameHistoryService gameHistoryService, PlayerDAO playerDAO) {
        _gameHistoryService = gameHistoryService;
        _playerDAO = playerDAO;
    }


    public GameRecordingInProgress recordGame(LotroGameMediator lotroGame, LotroFormat format, final String tournamentName, final Map<String, LotroDeck> decks) {
        final ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC);
        final Map<String, GameCommunicationChannel> recordingChannels = new HashMap<>();
        for (String playerId : lotroGame.getPlayersPlaying()) {
            var recordChannel = new GameCommunicationChannel(playerId, 0, format);
            lotroGame.addGameStateListener(playerId, recordChannel);
            recordingChannels.put(playerId, recordChannel);
        }

        return (winnerName, winReason, loserName, loseReason) -> {
            final ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC);

            var time = lotroGame.getTimeSettings();
            var clocks = lotroGame.getPlayerClocks();
            var gameInfo = new DBDefs.GameHistory() {{
                gameId = lotroGame.getGameId();

                winner = winnerName;
                winnerId = _playerDAO.getPlayer(winnerName).getId();
                loser = loserName;
                loserId = _playerDAO.getPlayer(loserName).getId();

                win_reason = winReason;
                lose_reason = loseReason;

                win_recording_id = getNewRecordingID();
                lose_recording_id = getNewRecordingID();

                start_date = startDate.toLocalDateTime();
                end_date = endDate.toLocalDateTime();

                format_name = format.getName();

                winner_deck_name = decks.get(winner).getDeckName();
                loser_deck_name = decks.get(loser).getDeckName();

                tournament = tournamentName;

                winner_site = lotroGame.getGame().getGameState().getPlayerPosition(winner);
                loser_site = lotroGame.getGame().getGameState().getPlayerPosition(winner);

                game_length_type = time.name();
                max_game_time = time.maxSecondsPerPlayer();
                game_timeout = time.maxSecondsPerDecision();
                winner_clock_remaining = clocks.getOrDefault(winnerName, -1);
                loser_clock_remaining = clocks.getOrDefault(loserName, -1);

                //Update this version as needed; note that this is the REPLAY FORMAT, not the JSON summary
                replay_version = 1;
            }};

            var playerRecordingId = saveRecordedChannels(recordingChannels, gameInfo, decks);
            gameInfo.id = _gameHistoryService.addGameHistory(gameInfo);

//            if(format.isPlaytest())
//            {
                String url = "https://docs.google.com/forms/d/e/1FAIpQLSdKJrCmjoyUqDTusDcpNoWAmvkGdzQqTxWGpdNIFX9biCee-A/viewform?usp=pp_url&entry.1592109986=";
                String winnerURL = "https://play.lotrtcgpc.net/gemp-lotr/game.html%3FreplayId%3D" + winnerName + "$" + playerRecordingId.get(winnerName);
                String loserURL = "https://play.lotrtcgpc.net/gemp-lotr/game.html%3FreplayId%3D" + loserName + "$" + playerRecordingId.get(loserName);
                url += winnerURL + "%20" + loserURL;
                lotroGame.sendMessageToPlayers("Thank you for playtesting!  Please report how you feel about this match (good, bad, or neutral) as well as any bugs or other issues <a href= '" + url + "'>using the playtest feedback form here.</a>");
//            }

        };
    }

    public interface GameRecordingInProgress {
        void finishRecording(String winner, String winReason, String loser, String loseReason);
    }

    private File getRecordingFileVersion0(String playerId, String gameId) {
        File gameReplayFolder = new File(AppConfig.getProperty("application.root"), "replay");
        File playerReplayFolder = new File(gameReplayFolder, playerId);
        return new File(playerReplayFolder, gameId + ".xml.gz");
    }

    private File getRecordingFileVersion1(String playerId, String gameId, ZonedDateTime startDate) {
        var gameReplayFolder = new File(AppConfig.getProperty("application.root"), "replay");
        //This dumbass formatting output is because anything that otherwise interacts with the
        // year subfield appears to trigger a JVM segfault in the guts of the java ecosystem.
        // Super-dumb.  Don't touch these two lines.
        var year = startDate.format(DateTimeFormatter.ofPattern("yyyy"));
        var month = startDate.format(DateTimeFormatter.ofPattern("MM"));

        var yearFolder = new File(gameReplayFolder, year);
        var monthFolder = new File(yearFolder, month);
        var playerReplayFolder = new File(monthFolder, playerId);
        return new File(playerReplayFolder, gameId + ".xml.gz");
    }

    private File getSummaryFile(DBDefs.GameHistory history) {
        var gameReplayFolder = new File(AppConfig.getProperty("application.root"), "replay");
        var summaryFolder = new File(gameReplayFolder, "summaries");
        var yearFolder = new File(summaryFolder, String.format("%04d", history.start_date.getYear()));
        var monthFolder = new File(yearFolder, String.format("%02d", history.start_date.getMonthValue()));
        monthFolder.mkdirs();
        return new File(monthFolder, history.winner + "_vs_" + history.loser + "_" +
                history.win_recording_id + "_" + history.lose_recording_id+ ".json");
    }

    private OutputStream getRecordingWriteStream(String playerId, String gameId, ZonedDateTime startDate) throws IOException {
        File recordingFile = getRecordingFileVersion1(playerId, gameId, startDate);
        recordingFile.getParentFile().mkdirs();

        Deflater deflater = new Deflater(9);
        return new DeflaterOutputStream(new FileOutputStream(recordingFile), deflater);
    }

    private Map<String, String> saveRecordedChannels(Map<String, GameCommunicationChannel> gameProgress, DBDefs.GameHistory gameInfo, Map<String, LotroDeck> decks) {
        Map<String, String> result = new HashMap<>();
        var metadata = new ReplayMetadata(gameInfo, decks);

        for (Map.Entry<String, GameCommunicationChannel> playerRecordings : gameProgress.entrySet()) {
            String playerId = playerRecordings.getKey();
            String recordingId = "";
            if(playerId.equals(gameInfo.winner)) {
                recordingId = gameInfo.win_recording_id;
            }
            else {
                recordingId = gameInfo.lose_recording_id;
            }

            final List<GameEvent> gameEvents = playerRecordings.getValue().consumeGameEvents();
            metadata.ParseReplay(playerId, gameEvents);

            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document doc = documentBuilder.newDocument();
                Element gameReplay = doc.createElement("gameReplay");
                EventSerializer serializer = new EventSerializer();

                var info = doc.createElement("info");

                info.setAttribute("replay_version", String.valueOf(gameInfo.replay_version));
                info.setAttribute("winner", gameInfo.winner);
                info.setAttribute("loser", gameInfo.loser);
                info.setAttribute("win_reason", gameInfo.win_reason);
                info.setAttribute("lose_reason", gameInfo.lose_reason);
                info.setAttribute("start_date", gameInfo.start_date.toString());
                info.setAttribute("end_date", gameInfo.end_date.toString());
                info.setAttribute("format", gameInfo.format_name);
                info.setAttribute("tournament", gameInfo.tournament);

                for(var pair : decks.entrySet()) {
                    String player = pair.getKey();
                    var deck = pair.getValue();

                    var deckElement = doc.createElement("deckReadout");
                    deckElement.setAttribute("playerId", player);
                    deckElement.setAttribute("name", deck.getDeckName());
                    deckElement.setAttribute("rb", deck.getRingBearer());
                    deckElement.setAttribute("ring", deck.getRing());
                    deckElement.setAttribute("sites", String.join(",", deck.getSites()));
                    deckElement.setAttribute("deck", String.join(",", deck.getDrawDeckCards()));

                    info.appendChild(deckElement);
                }

                gameReplay.appendChild(info);

                for (GameEvent gameEvent : gameEvents) {
                    gameReplay.appendChild(serializer.serializeEvent(doc, gameEvent));
                }

                doc.appendChild(gameReplay);

                try (OutputStream replayStream = getRecordingWriteStream(playerId, recordingId, gameInfo.GetUTCStartDate())) {
                    // Prepare the DOM document for writing
                    Source source = new DOMSource(doc);
                    // Prepare the output file
                    Result streamResult = new StreamResult(replayStream);
                    // Write the DOM document to the file
                    Transformer xformer = TransformerFactory.newInstance().newTransformer();
                    xformer.transform(source, streamResult);
                }
                result.put(playerId, recordingId);

                try(var out = new PrintWriter(getSummaryFile(gameInfo).getAbsolutePath())) {
                    out.println(JSON.toJSONString(metadata));
                }
            } catch (Exception exp) {

            }

        }
        return result;
    }

    private String randomUid() {
        int length = 16;
        char[] chars = new char[length];
        Random rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++)
            chars[i] = _possibleChars.charAt(rnd.nextInt(_charsCount));

        return new String(chars);
    }

    private String getNewRecordingID() {
        String id;
        do {
            id = randomUid();
        } while (_gameHistoryService.doesReplayIDExist(id));
        return id;
    }

    public InputStream getRecordedGame(String playerId, String recordId) throws IOException {
        var history = _gameHistoryService.getGameHistory(recordId);

        if(history == null)
            return null;

        File recordingFile = null;

        if(history.replay_version == 0) {
            recordingFile = getRecordingFileVersion0(playerId, recordId);
        }
        else if(history.replay_version == 1) {
            recordingFile = getRecordingFileVersion1(playerId, recordId, history.GetUTCStartDate());
        }

        if (recordingFile == null || !recordingFile.exists() || !recordingFile.isFile())
            return null;

        return new InflaterInputStream(new FileInputStream(recordingFile));
    }

    public InputStream getRecordedSummary(String recordId) throws IOException {
        var history = _gameHistoryService.getGameHistory(recordId);

        if(history == null)
            return null;

        File recordingFile = getSummaryFile(history);

        if (recordingFile == null || !recordingFile.exists() || !recordingFile.isFile())
            return null;

        return new FileInputStream(recordingFile);
    }
}
