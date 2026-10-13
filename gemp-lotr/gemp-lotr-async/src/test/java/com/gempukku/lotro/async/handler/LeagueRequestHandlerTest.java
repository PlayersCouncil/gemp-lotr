package com.gempukku.lotro.async.handler;

import com.gempukku.lotro.async.ResponseWriter;
import com.gempukku.lotro.chat.MarkdownParser;
import com.gempukku.lotro.collection.CollectionsManager;
import com.gempukku.lotro.collection.TransferDAO;
import com.gempukku.lotro.competitive.PlayerStanding;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.db.vo.League;
import com.gempukku.lotro.draft2.SoloDraftDefinitions;
import com.gempukku.lotro.game.DeckValidationContext;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.formats.LotroFormatLibrary;
import com.gempukku.lotro.league.LeagueParams;
import com.gempukku.lotro.league.LeagueService;
import com.gempukku.lotro.league.RTMDLeague;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.service.LoggedUserHolder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * GET /league, the list of leagues the deck builder reads: which of a Race to Mount Doom league's meta-sites it
 * gives the caller, and which of them it marks {@code siteOverride}.
 *
 * The deck builder opens its site list to sites of every block (it sends {@code siteOverride:true} with the card
 * filter) when a meta-site it was given for the league chosen is marked so.  The meta-sites given are the caller's
 * own: the one at his place on the path, or in a cumulative league every one up to it.  Those are the ones his deck
 * is validated under ({@code LeagueService.buildDeckValidationContext}), so the list is opened for a player who has
 * a meta-site that lets his sites be of any block (Race Text 92_19) and for nobody else in the league (F-0342).
 */
public class LeagueRequestHandlerTest {
    // A path of three: the one in the middle lets an adventure deck hold sites of any block
    private static final String Before = "92_3";
    private static final String AnyBlock = "92_19";
    private static final String After = "92_24";

    private String _previousTestProperty;
    private Map<Type, Object> _context;
    private LeagueService _leagueService;
    private PlayerDAO _playerDao;
    private ProductLibrary _productLibrary;
    private LotroFormatLibrary _formatLibrary;

    @Before
    public void setUp() throws Exception {
        // With -Dtest=true the participantId stands for the login
        _previousTestProperty = System.setProperty("test", "true");

        _playerDao = mock(PlayerDAO.class);
        _leagueService = mock(LeagueService.class);
        _productLibrary = mock(ProductLibrary.class);

        _formatLibrary = mock(LotroFormatLibrary.class);
        LotroFormat pcMovie = mock(LotroFormat.class);
        when(pcMovie.getCode()).thenReturn("pc_movie");
        when(_formatLibrary.getFormat("pc_movie")).thenReturn(pcMovie);

        // The card library, as far as this endpoint reads it: a name for each meta-site, and its deck building
        // overrides.  Only the one in the middle has any.
        LotroCardBlueprintLibrary library = mock(LotroCardBlueprintLibrary.class);
        for (String id : List.of(Before, AnyBlock, After)) {
            LotroCardBlueprint blueprint = mock(LotroCardBlueprint.class);
            when(blueprint.getFullName()).thenReturn("Race Text " + id);
            when(library.getLotroCardBlueprint(id)).thenReturn(blueprint);
            if (id.equals(AnyBlock)) {
                when(blueprint.getDeckBuildingOverrides())
                        .thenReturn(new DeckValidationContext().setSkipSiteBlockValidation(true));
            }
        }

        MarkdownParser markdown = mock(MarkdownParser.class);
        when(markdown.renderDescription(any())).thenReturn("");

        _context = new HashMap<>();
        _context.put(PlayerDAO.class, _playerDao);
        _context.put(LoggedUserHolder.class, mock(LoggedUserHolder.class));
        _context.put(TransferDAO.class, mock(TransferDAO.class));
        _context.put(CollectionsManager.class, mock(CollectionsManager.class));
        _context.put(LotroCardBlueprintLibrary.class, library);
        _context.put(SoloDraftDefinitions.class, mock(SoloDraftDefinitions.class));
        _context.put(LeagueService.class, _leagueService);
        _context.put(LotroFormatLibrary.class, _formatLibrary);
        _context.put(ProductLibrary.class, _productLibrary);
        _context.put(MarkdownParser.class, markdown);
    }

    @After
    public void tearDown() {
        if (_previousTestProperty != null)
            System.setProperty("test", _previousTestProperty);
        else
            System.clearProperty("test");
    }

    // A Race to Mount Doom league in PC Movie on the path of three, a place gained for each win, with one member
    // who has won so many games of as many played.  The league itself is the real RTMDLeague: where a player
    // stands and which meta-sites are his are its own answers.
    private void LeagueWithMember(String player, int wins, boolean cumulative) throws Exception {
        var params = new LeagueParams();
        params.name = "Race to Mount Doom";
        params.start = LocalDateTime.now().minusDays(1);
        params.description = "";
        params.series.add(new LeagueParams.SerieData("pc_movie", 30, 10));
        params.racePath = new ArrayList<>(List.of(Before, AnyBlock, After));
        params.raceVisualPath = new ArrayList<>(List.of("90_1", "90_2", "90_3"));
        params.raceCumulative = cumulative;
        params.raceAdvancementMode = RTMDLeague.AdvanceType.WIN;
        params.raceAdvanceFactor = 1;
        var race = new RTMDLeague(_productLibrary, _formatLibrary, params);

        League league = mock(League.class);
        when(league.getLeagueData(any(), any(), any())).thenReturn(race);
        when(league.getType()).thenReturn(League.LeagueType.RTMD);
        when(league.getCodeStr()).thenReturn("1234");
        when(league.getName()).thenReturn(params.name);
        when(league.getDescription()).thenReturn("");

        var member = new Player(1, player, "pass", "u", null, null, null, null, false);
        when(_playerDao.getPlayer(player)).thenReturn(member);
        when(_leagueService.getActiveLeagues()).thenReturn(List.of(league));
        when(_leagueService.isPlayerInLeague(league, member)).thenReturn(true);
        // The standings give 2 points for a win and 1 for a loss
        when(_leagueService.getLeagueStandings(league))
                .thenReturn(List.of(new PlayerStanding(player, 2 * wins, wins, wins, 0, 0)));
    }

    // The meta-sites GET /league gives that player for the league, in order
    private List<Element> MetaSitesGiven(String player) throws Exception {
        var request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                "/gemp-lotr-server/league?participantId=" + player);
        ResponseWriter writer = mock(ResponseWriter.class);
        new LeagueRequestHandler(_context).handleRequest("", request, _context, writer, "127.0.0.1");

        ArgumentCaptor<Document> answer = ArgumentCaptor.forClass(Document.class);
        verify(writer).writeXmlResponse(answer.capture());
        var leagues = answer.getValue().getDocumentElement().getElementsByTagName("league");
        assertEquals(1, leagues.getLength());
        var given = ((Element) leagues.item(0)).getElementsByTagName("metaSite");
        var result = new ArrayList<Element>();
        for (int i = 0; i < given.getLength(); i++)
            result.add((Element) given.item(i));
        return result;
    }

    private static List<String> Ids(List<Element> metaSites) {
        return metaSites.stream().map(e -> e.getAttribute("blueprintId")).toList();
    }

    // The ids of the meta-sites marked as opening the site list to every block
    private static List<String> Marked(List<Element> metaSites) {
        return metaSites.stream().filter(e -> "true".equals(e.getAttribute("siteOverride")))
                .map(e -> e.getAttribute("blueprintId")).toList();
    }

    @Test
    public void APlayerAtTheMetaSiteIsGivenItMarked() throws Exception {
        LeagueWithMember("alice", 1, false);

        var given = MetaSitesGiven("alice");
        assertEquals(List.of(AnyBlock), Ids(given));
        assertEquals(List.of(AnyBlock), Marked(given));
    }

    @Test
    public void APlayerWhoHasNotReachedTheMetaSiteIsGivenNothingMarked() throws Exception {
        LeagueWithMember("alice", 0, false);

        var given = MetaSitesGiven("alice");
        assertEquals(List.of(Before), Ids(given));
        assertEquals(List.of(), Marked(given));
    }

    @Test
    public void APlayerWhoHasPassedTheMetaSiteIsGivenNothingMarked() throws Exception {
        LeagueWithMember("alice", 2, false);

        var given = MetaSitesGiven("alice");
        assertEquals(List.of(After), Ids(given));
        assertEquals(List.of(), Marked(given));
    }

    @Test
    public void InACumulativeLeagueAPlayerWhoHasPassedTheMetaSiteStillHasItAndIsGivenItMarked() throws Exception {
        LeagueWithMember("alice", 2, true);

        var given = MetaSitesGiven("alice");
        assertEquals(List.of(Before, AnyBlock, After), Ids(given));
        assertEquals(List.of(AnyBlock), Marked(given));
    }

    @Test
    public void InACumulativeLeagueAPlayerWhoHasNotReachedTheMetaSiteIsGivenNothingMarked() throws Exception {
        LeagueWithMember("alice", 0, true);

        var given = MetaSitesGiven("alice");
        assertEquals(List.of(Before), Ids(given));
        assertEquals(List.of(), Marked(given));
    }
}
