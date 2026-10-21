package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("agitator", "8_94");

					put("guard1", "1_7");       // Dwarf Guard: a companion with no text once in play
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("barliman", "1_70");    // Barliman Butterbur: an ally; a fellowship ability used only on purpose

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion who is no besieger; his text answers only a weapon played on him
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc besieger; in play its text does nothing
					put("pillager2", "7_275");
					put("troll", "8_102");      // Great Hill Troll: [sauron] Troll besieger; its play trigger is for King-block sites only
					put("urukbesieger", "4_180"); // Uruk Besieger: "Besieger" in the title and not the keyword; abilities used only on purpose
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc minion, no text

					put("towers", "7_281");     // Great Siege-towers: [sauron] engine, a condition, cost 1; abilities used only on purpose
					put("grond", "8_103");      // Grond, Hammer of the Underworld: [sauron] engine, a possession, cost 3; abilities used only on purpose
					put("walkway", "7_315");    // Tower Walkway: [sauron] engine condition, cost 1, to play spot a [sauron] Orc
					put("peril", "7_280");      // Great Peril of Fire: [sauron] condition that is no engine, cost 1, to play spot a besieger
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The first is cheated into play: the [sauron] Orc to spot, and the other besieger
					put("agitator1", "8_94");
					put("agitator2", "8_94");
					put("agitator3", "8_94");
					// Dwarf Guard: a companion with no text once in play
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothAgitatorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Agitator
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>. To play, spot a [sauron] Orc.<br>When you play this minion, if you can spot another besieger, you may add a threat for each companion over 4.<br><b>Shadow:</b> Remove a threat to play a [sauron] engine from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("agitator");

		assertEquals("Gorgoroth Agitator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A threat for each
	// companion over 4" is no threat with four companions.
	@Test
	public void TheThreatsAreOfferedWithFiveCompanionsAndNotWithFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var frodo = scn.GetRingBearer();
		var guard4 = scn.GetFreepsCard("guard4");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));

		var agitator1 = scn.GetShadowCard("agitator1");
		var agitator2 = scn.GetShadowCard("agitator2");
		var agitator3 = scn.GetShadowCard("agitator3");
		scn.MoveCardsToHand(agitator2, agitator3);

		scn.StartGame();
		scn.MoveMinionsToTable(agitator1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Frodo and three Dwarf Guards; another besieger is there to spot.
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		scn.ShadowPlayCard(agitator2);
		assertInZone(Zone.SHADOW_CHARACTERS, agitator2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(agitator2));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A fifth companion: one over 4.
		scn.MoveCompanionsToTable(guard4);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(agitator3);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(agitator3));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcIsSpottedAndMayDeclineTheThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4");
		scn.MoveCardsToHand(agitator);
		assertInHand(agitator);

		scn.StartGame();
		// A [sauron] minion that is no Orc (and a besieger), and an Orc that is not [sauron].
		scn.MoveMinionsToTable(troll, patrol);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, troll, patrol);
		assertInHand(agitator);
		assertFalse(scn.ShadowPlayAvailable(agitator));

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(agitator));

		scn.ShadowPlayCard(agitator);
		assertInZone(Zone.SHADOW_CHARACTERS, agitator);
		// Frodo and four Dwarf Guards, and the Troll is another besieger.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(agitator));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnotherBesiegerAndFiveCompanionsItAddsAThreatAndAnotherBesiegerPlayedAfterAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var pillager = scn.GetShadowCard("pillager");
		var pillager2 = scn.GetShadowCard("pillager2");
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4");
		scn.MoveCardsToHand(agitator, pillager2);

		scn.StartGame();
		// The Pillager is the [sauron] Orc to spot and the other besieger.
		scn.MoveMinionsToTable(pillager);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(agitator);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(agitator));
		scn.ShadowAcceptOptionalTrigger();
		// Frodo and four Dwarf Guards: one companion over 4.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another besieger played with the Agitator in play sets nothing off.
		scn.ShadowPlayCard(pillager2);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithATrollBesiegerAndSixCompanionsItAddsTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4", "guard5");
		scn.MoveCardsToHand(agitator);

		scn.StartGame();
		// The Chieftain is the [sauron] Orc to spot, the Troll the other besieger.
		scn.MoveMinionsToTable(chieftain, troll);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain, troll);
		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(agitator);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(agitator));
		scn.ShadowAcceptOptionalTrigger();
		// Frodo and five Dwarf Guards: two companions over 4.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NoThreatIsOfferedWithoutAnotherBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var chieftain = scn.GetShadowCard("chieftain");
		var urukbesieger = scn.GetShadowCard("urukbesieger");
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4");
		scn.MoveCardsToHand(agitator);

		scn.StartGame();
		// A [sauron] Orc who is no besieger, and a minion with "Besieger" in its title only.
		scn.MoveMinionsToTable(chieftain, urukbesieger);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain, urukbesieger);
		assertEquals(0, scn.GetThreats());
		// The Agitator is a besieger itself, but not "another".
		scn.ShadowPlayCard(agitator);
		assertInZone(Zone.SHADOW_CHARACTERS, agitator);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAllyIsNoCompanionCountedOverFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var pillager = scn.GetShadowCard("pillager");
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3");
		scn.MoveAlliesToTable("barliman");
		scn.MoveCardsToHand(agitator);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Frodo, three Dwarf Guards and Barliman: four companions.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, scn.GetFreepsCard("barliman"));
		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(agitator);
		assertInZone(Zone.SHADOW_CHARACTERS, agitator);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesAThreatToPlayASauronEngineFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var towers = scn.GetShadowCard("towers");
		var grond = scn.GetShadowCard("grond");
		var peril = scn.GetShadowCard("peril");
		var walkway = scn.GetFreepsCard("walkway");
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.MoveMinionsToTable(agitator);
		scn.MoveCardsToShadowDiscard("towers", "grond", "peril");
		// The Free Peoples player's own Tower Walkway, an engine in HIS discard pile; the Shadow player's stays in his deck.
		scn.MoveCardsToFreepsDiscard("walkway");
		scn.AddThreats(1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetThreats());
		assertInDiscard(towers, grond, peril, walkway);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(agitator));
		scn.ShadowUseCardAction(agitator);

		assertEquals(0, scn.GetThreats());
		// An engine condition and an engine possession; not a [sauron] condition that is no engine, nor an
		// engine in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(towers, grond));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(peril, walkway));
		scn.ShadowChooseCard(grond);

		assertInZone(Zone.SUPPORT, grond);
		assertInDiscard(towers, peril, walkway);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithoutAThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.MoveMinionsToTable(agitator);
		scn.MoveCardsToShadowDiscard("towers");
		scn.SetTwilight(10);
		assertInDiscard(towers);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(agitator));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(agitator));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoEngineInTheDiscardPileThatCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var towers = scn.GetShadowCard("towers");
		var peril = scn.GetShadowCard("peril");
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.MoveMinionsToTable(agitator);
		scn.MoveCardsToShadowDiscard("peril");
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInDiscard(peril);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Only a [sauron] condition that is no engine.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(agitator));

		// An engine of cost 1, with nothing in the pool to play it.
		scn.SetTwilight(0);
		scn.MoveCardsToShadowDiscard("towers");
		assertInDiscard(towers);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(agitator));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(agitator));
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var agitator = scn.GetShadowCard("agitator");
		var towers = scn.GetShadowCard("towers");
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.MoveMinionsToTable(agitator);
		scn.MoveCardsToShadowDiscard("towers");
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertInDiscard(towers);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// A threat to remove, an engine in the discard pile and twilight for it, but no Shadow phase.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(agitator));
	}
}
