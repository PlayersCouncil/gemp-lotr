package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_211_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "7_211");

					put("hound", "7_192");      // Morgul Hound: [wraith] Orc; nothing below 2 wounds on the Ring-bearer
					put("nelya", "2_84");       // Úlairë Nelya: [wraith] Nazgûl; nothing below 2 burdens and 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; nothing below 3 burdens
					put("drawn", "1_211");      // Drawn to Its Power: [wraith] condition, support area; answers a companion killed only
					put("steed", "1_208");      // Black Steed: [wraith] possession (mount) on a Nazgûl; +2 at a plains site
					put("sword", "1_299");      // Hobbit Sword: [shire] possession (hand weapon) on a Hobbit, no other text
					put("anduril", "7_80");     // Andúril, King's Blade: [gondor] ARTIFACT (hand weapon) on Aragorn; defender +1 with no threat

					put("sam", "1_311");        // Sam, Son of Hamfast: abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: no text in play
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: +3 against a Nazgûl in a skirmish
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability used only on purpose
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: an ally; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Cantëa, Faster Than Winds
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>When you play Úlairë Cantëa, add a threat for each companion over 4.<br><b>Maneuver:</b> Remove 2 threats and spot another [wraith] minion to discard a possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Faster Than Winds", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithFiveCompanionsSheAddsOneThreatAndAnotherMinionPlayedAfterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable("sam", "aragorn", "lorien", "arwen");
		scn.MoveCardsToShadowHand("cantea", "hound");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Frodo and four more
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(cantea);
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);
		assertEquals(1, scn.GetThreats());

		// Another [wraith] minion played while she is in play sets nothing off.
		scn.ShadowPlayCard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWithSixCompanionsSheAddsTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCompanionsToTable("sam", "aragorn", "lorien", "arwen", "gimli");
		scn.MoveCardsToShadowHand("cantea");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(cantea);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWithFourCompanionsAndAnAllySheAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCompanionsToTable("sam", "aragorn", "lorien");
		// An ally is no companion. The Shadow player's own Frodo is in play too, and not active on this turn.
		scn.MoveAlliesToTable("celeborn");
		scn.MoveCardsToShadowHand("cantea");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(cantea);
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityRemovesTwoThreatsToDiscardTheChosenPossessionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var drawn = scn.GetShadowCard("drawn");
		var steed = scn.GetShadowCard("steed");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, sword);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();
		// Three companions: room for three threats.
		scn.AddThreats(3);
		scn.MoveMinionsToTable(cantea, hound);
		scn.AttachCardsTo(cantea, steed);
		scn.MoveCardsToSupportArea(drawn);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(cantea));

		scn.ShadowUseCardAction(cantea);
		assertEquals(1, scn.GetThreats());
		// A Free Peoples possession and a Shadow one; not an artifact, nor a condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, steed));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(anduril, drawn, sam, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertInDiscard(sword);
		assertAttachedTo(steed, cantea);
		assertAttachedTo(anduril, aragorn);
		assertInZone(Zone.SUPPORT, drawn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ANazgulBesideHerIsAnotherWraithMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		// Exactly the two threats the cost asks for.
		scn.AddThreats(2);
		scn.MoveMinionsToTable(cantea, nelya);
		scn.AttachCardsTo(nelya, steed);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(cantea));

		scn.ShadowUseCardAction(cantea);
		assertEquals(0, scn.GetThreats());
		scn.ShadowChooseCard(steed);

		assertInDiscard(steed);
		assertAttachedTo(sword, sam);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedUntilAnotherWraithMinionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		var drawn = scn.GetShadowCard("drawn");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, scn.GetFreepsCard("aragorn"));
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		scn.AddThreats(3);
		// A Nazgûl who is not [wraith], and a [wraith] card that is no minion.
		scn.MoveMinionsToTable(cantea, khamul);
		scn.MoveCardsToSupportArea(drawn);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));

		scn.MoveMinionsToTable(hound);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(cantea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var hound = scn.GetShadowCard("hound");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam, scn.GetFreepsCard("aragorn"));
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(cantea, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cantea));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(cantea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
