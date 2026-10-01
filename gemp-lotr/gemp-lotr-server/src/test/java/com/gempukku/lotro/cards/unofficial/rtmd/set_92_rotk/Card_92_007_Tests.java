package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.TestConstants;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.EventSerializer;
import com.gempukku.lotro.game.state.GameCommunicationChannel;
import com.gempukku.lotro.game.state.GameEvent;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.GameStats;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_007_Tests implements TestConstants
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		put("gimli", "1_13");
		put("legolas", "1_50");
		put("runner", "1_178");
		put("enquea", "1_231");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_7", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_7"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_7
		 * Type: MetaSite
		 * Game Text: You must play with your hand revealed.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_7", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ModifierIsActiveForFreepsOwner() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_7: The card owner's hand should be revealed.
		// This tests the modifier is registered; actual visibility is a client/channel concern
		// that can't be verified in the test rig.

		var scn = GetFreepsScenario();

		scn.StartGame();

		// The modifier should report the freeps player's hand as revealed
		var game = scn.game();
		assertTrue(game.getModifiersQuerying().isHandRevealed(game, P1));
		assertFalse(game.getModifiersQuerying().isHandRevealed(game, P2));
	}

	@Test
	public void ModifierIsActiveForShadowOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		var game = scn.game();
		assertTrue(game.getModifiersQuerying().isHandRevealed(game, P2));
		assertFalse(game.getModifiersQuerying().isHandRevealed(game, P1));
	}

	// ---- #1095: who is told that the hand is revealed, and who receives its cards ----
	// The owner is whoever holds 92_7, which is not tied to a side: both orientations are tested.

	@Test
	public void GameStatsReportTheFreepsOwnersHandAsRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.StartGame();

		var stats = new GameStats();
		stats.updateGameStats(scn.game());
		assertEquals(Set.of(P1), stats.getRevealedHands());
	}

	@Test
	public void GameStatsReportTheShadowOwnersHandAsRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		scn.StartGame();

		var stats = new GameStats();
		stats.updateGameStats(scn.game());
		assertEquals(Set.of(P2), stats.getRevealedHands());
	}

	@Test
	public void RevealedHandStillReportedAfterRolesSwap() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_7 belongs to P1, who is the Free Peoples player on turn 1 and the Shadow player on turn 2.
		var scn = GetFreepsScenario();
		scn.StartGame();
		scn.SkipToSite(2);

		var stats = new GameStats();
		stats.updateGameStats(scn.game());
		assertEquals(Set.of(P1), stats.getRevealedHands());
	}

	@Test
	public void RevealedHandFlagIsSerializedOnlyOnTheOwnersPlayerZones() throws Exception {
		var scn = GetShadowScenario();
		scn.StartGame();

		var stats = new GameStats();
		stats.updateGameStats(scn.game());

		Element ge = serialize(new GameEvent(GameEvent.Type.GAME_STATS).gameStats(stats));
		NodeList zones = ge.getElementsByTagName("playerZones");
		assertEquals(2, zones.getLength());
		for (int i = 0; i < zones.getLength(); i++) {
			var zone = (Element) zones.item(i);
			if (zone.getAttribute("name").equals(P2))
				assertEquals("true", zone.getAttribute("handRevealed"));
			else
				assertFalse(zone.hasAttribute("handRevealed"));
		}
	}

	@Test
	public void NoHandIsReportedRevealedWithout92_7() throws Exception {
		var scn = new VirtualTableScenario(cards, VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo, VirtualTableScenario.RulingRing);
		scn.StartGame();

		var stats = new GameStats();
		stats.updateGameStats(scn.game());
		assertTrue(stats.getRevealedHands().isEmpty());

		Element ge = serialize(new GameEvent(GameEvent.Type.GAME_STATS).gameStats(stats));
		NodeList zones = ge.getElementsByTagName("playerZones");
		for (int i = 0; i < zones.getLength(); i++)
			assertFalse(((Element) zones.item(i)).hasAttribute("handRevealed"));
	}

	@Test
	public void SpectatorJoiningIsSentTheFreepsOwnersHandButNotTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		scn.MoveCardsToFreepsHand("gimli", "legolas");
		scn.MoveCardsToShadowHand("runner", "enquea");
		scn.StartGame();

		assertSpectatorSeesOnlyOwnersHand(scn, P1, Set.of("gimli", "legolas"), P2);
	}

	@Test
	public void SpectatorJoiningIsSentTheShadowOwnersHandButNotTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		scn.MoveCardsToFreepsHand("gimli", "legolas");
		scn.MoveCardsToShadowHand("runner", "enquea");
		scn.StartGame();

		assertSpectatorSeesOnlyOwnersHand(scn, P2, Set.of("runner", "enquea"), P1);
	}

	@Test
	public void SpectatorIsSentCardsDrawnIntoTheRevealedHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		scn.StartGame();
		scn.MoveCardsToTopOfDeck(gimli);

		var spectator = new GameCommunicationChannel("spectator", 1, true, scn.game().getFormat());
		scn.game().addGameStateListener("spectator", spectator);
		spectator.consumeGameEvents();

		scn.FreepsDrawCards(1);
		assertInZone(Zone.HAND, gimli);

		var drawn = spectator.consumeGameEvents().stream()
				.filter(e -> e.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY && e.getZone() == Zone.HAND)
				.map(GameEvent::getCardId).collect(Collectors.toSet());
		assertEquals(Set.of(gimli.getCardId()), drawn);
	}

	private void assertSpectatorSeesOnlyOwnersHand(VirtualTableScenario scn, String owner, Set<String> ownerAliases,
			String opponent) throws CardNotFoundException {
		var spectator = new GameCommunicationChannel("spectator", 1, true, scn.game().getFormat());
		scn.game().addGameStateListener("spectator", spectator);
		List<GameEvent> events = spectator.consumeGameEvents();

		var handCardIds = events.stream()
				.filter(e -> e.getType() == GameEvent.Type.PUT_CARD_INTO_PLAY && e.getZone() == Zone.HAND)
				.map(GameEvent::getCardId).collect(Collectors.toSet());
		var expected = ownerAliases.stream()
				.map(alias -> owner.equals(P1) ? scn.GetFreepsCard(alias) : scn.GetShadowCard(alias))
				.map(card -> card.getCardId()).collect(Collectors.toSet());
		assertEquals(expected, handCardIds);

		// and the stats it is sent say whose hand that is, so the client can show the link for every viewer
		var stats = events.stream().filter(e -> e.getType() == GameEvent.Type.GAME_STATS)
				.map(GameEvent::getGameStats).reduce((a, b) -> b).orElseThrow();
		assertEquals(Set.of(owner), stats.getRevealedHands());
		assertFalse(stats.getRevealedHands().contains(opponent));
	}

	private static Element serialize(GameEvent event) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		return (Element) new EventSerializer().serializeEvent(doc, event);
	}
}
