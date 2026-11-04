package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_58");

					// [gondor] Men of each type, and characters who fail one word of "a [gondor] Man".
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; "Knight." is his whole text
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man companion; a regroup ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; "Valiant." is his whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains; "Each time an Uruk-hai is played, that minion
	// must exert") as site 2.  Site 4 is White Rocks 4_346, a battleground and no plains (its Shadow number
	// counts mounted companions).  Site 3, the Gondorian Merchant's home (3T), is Barrows of Edoras 4_337, a
	// sanctuary and no battleground.
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_58");
					put("bow2", "11_58");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; "Knight." is his whole text
					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man companion; a regroup ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, vitality 1, home 3T; a maneuver ability used only on purpose
					put("cloak", "15_68");      // Ranger's Cloak: vitality +1 for a [gondor] Man; its texts are about skirmishes
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path as it is: site 2 is Eastfold 4_331, a plains and no battleground (its Shadow ability needs 3
	// [isengard] trackers).
	protected VirtualTableScenario GetPlainsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_58");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; "Knight." is his whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BowofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Bow of Minas Tirith
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [gondor] Man.
		 * At the start of the archery phase, if bearer is at a battleground site, you may exert him or her to make the fellowship archery total +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Bow of Minas Tirith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnAGondorManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, ranger, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(bow);
		assertInHand(bow);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);

		// Two [gondor] Man companions and the [gondor] Man ally; not the [gondor] Hobbit, the [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(bow, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheArcheryPhaseAtABattlegroundBearerMayExertToMakeTheFellowshipArcheryTotalPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.AttachCardsTo(knight, bow);
		assertAttachedTo(bow, knight);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals("Uruk Camp", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		// The start of the maneuver phase, at the same battleground: nothing is asked.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.PassManeuverActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.FreepsAcceptOptionalTrigger();

		// The bearer exerts, unasked; the Ranger, a [gondor] Man who does not bear the bow, does not.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		// No Free Peoples archer is on the table: the fellowship archery total is the bow's 1 alone.  The minion
		// archery total is not changed.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The one minion takes the fellowship's archery wound, unasked.
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.PassArcheryActions();
		assertEquals(1, scn.GetWoundsOn(troop));

		// The +1 was for the archery phase: in the assignment phase the total is 0 again.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void DeclinedNoOneExertsAndTheArcheryTotalIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, bow);
		assertAttachedTo(bow, knight);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.PassArcheryActions();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithBearerExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, bow);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(knight, 2);
		assertAttachedTo(bow, knight);
		assertEquals(2, scn.GetWoundsOn(knight));

		scn.StartGame();
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void AtASiteThatIsNoBattlegroundItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsScenario();

		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.AttachCardsTo(knight, bow);
		assertAttachedTo(bow, knight);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals("Eastfold", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void OfferedAgainAtTheStartOfTheArcheryPhaseOfTurn3AtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var bow = scn.GetFreepsCard("bow");
		var ranger = scn.GetFreepsCard("ranger");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(ranger);
		scn.AttachCardsTo(ranger, bow);
		assertAttachedTo(bow, ranger);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(ranger));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(ranger));
		// The runner, of vitality 1, is killed by the fellowship's archery wound: no skirmish follows.
		scn.PassArcheryActions();
		assertInDiscard(runner);

		// Turn 3 opens at Barrows of Edoras, a sanctuary: its heal is declined, so the wound stays.
		scn.SkipToSite(3);
		scn.FreepsDeclineSanctuaryHealing();
		assertEquals(1, scn.GetWoundsOn(ranger));
		scn.FreepsPass();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		scn.MoveMinionsToTable(runner2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void BorneByAnAllyWhoseHomeIsNoBattlegroundItIsNotOfferedWithTheFellowshipAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var cloak = scn.GetFreepsCard("cloak");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(knight, bow);
		scn.AttachCardsTo(merchant, bow2, cloak);
		assertAttachedTo(bow, knight);
		assertAttachedTo(bow2, merchant);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// The Merchant's vitality 1 is 2 with the cloak, so he could pay the exertion.  He is at his home site,
		// Barrows of Edoras, which is no battleground (CR 4.2 "ally"); the Knight is with the fellowship.
		assertEquals(2, scn.GetVitality(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow2));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
