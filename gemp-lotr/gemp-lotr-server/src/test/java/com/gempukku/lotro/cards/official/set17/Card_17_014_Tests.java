package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weapons", "17_14");

					put("legolas", "1_51");         // Legolas, Prince of Mirkwood: [elven] Elf archer companion; +3 only against a Nazgûl
					put("calaglin", "3_10");        // Calaglin, Elf of Lórien: [elven] Elf archer ally; a Regroup ability used only on purpose
					put("elf", "1_53");             // Lórien Elf: an [elven] Elf companion who is no archer (to play, spot an Elf)
					put("garrison", "8_36");        // Garrison of Gondor: a [gondor] Man archer companion; defender only with a fortification

					put("savage1", "1_151");        // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1
					put("savage2", "1_151");
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("tracker", "15_171");       // Tracking Uruk: [uruk-hai] Uruk-hai, vitality 3; hunter 1 is its whole text
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, vitality 1, so exhausted unwounded; its play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WeaponsofLothlorienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Weapons of Lothlórien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an [elven] archer and add a threat to wound a minion (or add three threats to exert three minions).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weapons");

		assertEquals("Weapons of Lothlórien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElvenArcherCompanionItAddsAThreatToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var legolas = scn.GetFreepsCard("legolas");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(legolas, elf);
		scn.MoveCardsToHand(weapons);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(savage, troop, runner);

		scn.SkipToPhase(Phase.ARCHERY);
		// Frodo, Legolas and the Lórien Elf: room for three threats, so both ways are open.
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, weapons);
		assertTrue(scn.FreepsPlayAvailable(weapons));

		scn.FreepsPlayCard(weapons);
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());

		// A minion of either culture and race, the exhausted Goblin Runner among them; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, elf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(runner);
		// Vitality 1: the wound kills him.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, weapons);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ThreeThreatsExertThreeMinionsWhoCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var legolas = scn.GetFreepsCard("legolas");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(legolas, elf);
		scn.MoveCardsToHand(weapons);

		scn.StartGame();

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");
		var tracker = scn.GetShadowCard("tracker");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(savage1, savage2, troop, tracker, runner);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());

		scn.FreepsPlayCard(weapons);
		scn.FreepsChooseOption("Add three threats");
		assertEquals(3, scn.GetThreats());

		// Four minions able to exert, of three cultures; not the exhausted Runner, not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, savage2, troop, tracker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, legolas, elf));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(savage1, troop, tracker);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.DISCARD, weapons);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithRoomForTwoThreatsOnlyTheWoundIsTakenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var legolas = scn.GetFreepsCard("legolas");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(legolas, elf);
		scn.MoveCardsToHand(weapons);

		scn.StartGame();

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage1, savage2, troop);
		// Three companions, one threat: room for two more, not three.
		scn.AddThreats(1);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());

		scn.FreepsPlayCard(weapons);
		// No choice of ways: the threat is added and a minion is to be wounded.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, savage2, troop));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithTwoMinionsAbleToExertTheThreeThreatsMayStillBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var legolas = scn.GetFreepsCard("legolas");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(legolas, elf);
		scn.MoveCardsToHand(weapons);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(savage, troop, runner);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetThreats());

		scn.FreepsPlayCard(weapons);
		// The price is the player's choice (Q-0089), and an "or" between two pay-to-do halves may take the one that
		// cannot be done in full (Q-0034).
		assertEquals(2, scn.FreepsGetMultipleChoices().size());
		scn.FreepsChooseOption("Add three threats");
		assertEquals(3, scn.GetThreats());
		// The two who can exert do so, with nothing left to choose; the exhausted Runner does not.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AnElvenArcherAllyIsSpottedToPlayItToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveAlliesToTable(calaglin);
		scn.MoveCardsToHand(weapons);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);

		scn.SkipToPhase(Phase.ARCHERY);
		assertInZone(Zone.HAND, weapons);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(weapons));

		// Frodo alone: room for one threat, so the wound is the only way and is asked.
		scn.FreepsPlayCard(weapons);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(scn.GetRingBearer()));
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAnElvenCompanionWhoIsNoArcherAndAnArcherOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var elf = scn.GetFreepsCard("elf");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(elf, garrison);
		scn.MoveCardsToHand(weapons);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage1"));

		scn.SkipToPhase(Phase.ARCHERY);
		// Three companions and no threat: either price could be paid.
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, weapons);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(weapons));
	}

	@Test
	public void CannotBePlayedWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapons = scn.GetFreepsCard("weapons");
		var legolas = scn.GetFreepsCard("legolas");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(legolas, elf);
		scn.MoveCardsToHand(weapons);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage1"));
		// Three companions, three threats.
		scn.AddThreats(3);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		assertInZone(Zone.HAND, weapons);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(weapons));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(weapons));
	}
}
