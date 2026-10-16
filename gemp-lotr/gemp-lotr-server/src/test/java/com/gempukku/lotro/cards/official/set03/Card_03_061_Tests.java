package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "3_61");
					put("warrior2", "3_61");
					put("servant", "3_58");     // Isengard Servant: another [isengard] Orc; a regroup ability used only on purpose
					put("chieftain", "1_266");  // Orc Chieftain: an Orc who is [sauron]; a trigger on a weapon played on him only
					// Uruk Crossbowman: an [isengard] Uruk-hai, no Orc, and an archer: "Archery: Exert this minion to
					// make the Free Peoples player wound an unbound companion; this minion does not add to the minion
					// archery total."
					put("crossbowman", "4_183");

					// Legolas, Greenleaf: "Archery: Exert Legolas to wound a minion; Legolas does not add to the
					// fellowship archery total."
					put("legolas", "1_50");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("doubleshot", "1_38");  // Double Shot: an archery EVENT: "Spot an Elf archer companion to make the fellowship archery total +1."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo bears The One Ring, Answer To All Riddles (vitality +2), so that with 3 wounds he can still exert.
	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "3_61");
					put("warrior2", "3_61");

					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; a fellowship ability and a response to Frodo's death
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf companion; an archery ability used only on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: an ally, no companion; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				"4_1"
		);
	}

	@Test
	public void IsengardWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Isengard Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you can spot another [isengard] Orc, no player may use archery special abilities.<br><b>Regroup:</b> Exert this minion and spot 3 wounds on the Ring-bearer to exert every companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Isengard Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAnotherIsengardOrcNeitherPlayerMayUseAnArcherySpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var doubleshot = scn.GetFreepsCard("doubleshot");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(doubleshot);

		var warrior = scn.GetShadowCard("warrior");
		var servant = scn.GetShadowCard("servant");
		var crossbowman = scn.GetShadowCard("crossbowman");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, servant, crossbowman);
		scn.SkipToPhase(Phase.MANEUVER);

		// A special ability of another phase is not barred.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.PassManeuverActions();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(crossbowman));
		int fellowshipTotal = scn.GetFreepsArcheryTotal();

		// Legolas could exert, and is not offered his archery ability.  An archery event is no special ability,
		// and is played.
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertTrue(scn.FreepsPlayAvailable(doubleshot));
		scn.FreepsPlayCard(doubleshot);
		assertEquals(fellowshipTotal + 1, scn.GetFreepsArcheryTotal());
		assertInDiscard(doubleshot);

		// The Shadow player's archer could exert, and is not offered his archery ability either.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(crossbowman));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(crossbowman));
	}

	@Test
	public void ASecondIsengardWarriorIsAnotherIsengardOrcForTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");
		var crossbowman = scn.GetShadowCard("crossbowman");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2, crossbowman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsActionAvailable(legolas));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(crossbowman));
		assertFalse(scn.ShadowActionAvailable(crossbowman));
	}

	@Test
	public void BesideAnIsengardUrukHaiAndASauronOrcAndNoOtherIsengardOrcArcheryAbilitiesMayBeUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var warrior = scn.GetShadowCard("warrior");
		var chieftain = scn.GetShadowCard("chieftain");
		var crossbowman = scn.GetShadowCard("crossbowman");

		scn.StartGame();
		// The Warrior himself, an [isengard] Uruk-hai and a [sauron] Orc: none is "another [isengard] Orc".
		scn.MoveMinionsToTable(warrior, chieftain, crossbowman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		scn.FreepsChooseCard(chieftain);
		assertEquals(1, scn.GetWoundsOn(chieftain));

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(crossbowman));
		assertTrue(scn.ShadowActionAvailable(crossbowman));
		scn.ShadowUseCardAction(crossbowman);
		assertEquals(1, scn.GetWoundsOn(crossbowman));
		// Legolas, the only unbound companion, takes the wound.
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void RegroupExertsThisMinionToExertEveryCompanionWithThreeWoundsOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(sam, aragorn, legolas);
		scn.MoveAlliesToTable(bilbo);

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.AddWoundsToChar(frodo, 3);

		// Frodo, 4 + 2 for the Ring, has 3 vitality left.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetVitality(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(warrior2));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warrior));

		scn.ShadowUseCardAction(warrior);

		// The Warrior used exerts, and the other copy does not.
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(warrior2));
		// Every companion exerts, the Ring-bearer among them; the ally does not.
		assertEquals(4, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoWoundsOnTheRingBearerAndThreeOnSam() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(warrior);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 3);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(warrior));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
	}

	@Test
	public void AnExhaustedWarriorIsNotOfferedWhileAFreshOneIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var frodo = scn.GetRingBearer();

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.AddWoundsToChar(frodo, 3);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(warrior, 2);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(warrior2));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));
		assertTrue(scn.ShadowActionAvailable(warrior2));
	}
}
