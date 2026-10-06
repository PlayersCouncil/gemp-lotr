package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charger", "103_41");
					put("charger2", "103_41");

					// Desert Warrior: Southron Man. At the start of its own skirmishes it may remove (3) to wound;
					// it is never in a skirmish here.
					put("warrior1", "4_222");
					put("warrior2", "4_222");
					put("warrior3", "4_222");
					put("warrior4", "4_222");
					put("explorer", "4_250");     // Southron Explorer: Southron Man; a regroup ability never used here
					put("explorer2", "4_250");
					put("scout", "4_252");        // Southron Scout: Southron Man, ambush (2) and nothing else
					put("infantry", "4_227");     // Easterling Infantry: a [raider] Man who is no Southron; strength 8; its text needs 3 burdens
					put("savage", "4_17");        // Dunlending Savage: a [dunland] Man with no text

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");       // Boromir, Son of Denethor: strength 7
					put("gimli", "1_13");         // Gimli, Son of Gloin
					put("merry", "1_302");        // Merry, Friend to Sam
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: there for Roll of Thunder to spot
					put("thunder", "4_99");       // Roll of Thunder: spot Gandalf to discard a Shadow possession or Shadow artifact
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladetuskChargerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Bladetusk Charger
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Support area
		 * Strength: 4
		 * Vitality: 4
		 * Game Text: Maneuver: Stack 1 or more Southron Men here to make this a <b>fierce</b> mounted Southron minion
		 * 	until the end of the turn that is strength +4 and <b>ambush (1)</b> for each Southron stacked here.
		 * 	At the start of each skirmish involving this minion, add a threat (or 2 threats if there are 4 Southrons stacked here).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("charger");

		assertEquals("Bladetusk Charger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		// The strength and vitality printed on the card are those of the minion it becomes. The possession
		// itself has neither; they are read where the Maneuver ability is tested.
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedForThreeToTheSupportAreaAndIsNoMinionThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		scn.MoveCardsToHand(charger);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 companion + 2 for the site
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(charger));

		scn.ShadowPlayCard(charger);
		// A possession pays no roaming penalty.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, charger);
		assertFalse(scn.IsType(charger, CardType.MINION));
		assertEquals(0, scn.GetStrength(charger));
		assertEquals(0, scn.GetVitality(charger));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionStacksSouthronMenFromPlayHereAndMakesItAFierceMountedSouthronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var charger2 = scn.GetShadowCard("charger2");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var explorer = scn.GetShadowCard("explorer");
		var scout = scn.GetShadowCard("scout");
		var infantry = scn.GetShadowCard("infantry");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(charger, charger2);
		scn.MoveCardsToHand(scout);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, warrior2, explorer, infantry, savage);
		// A Southron Man who is not [raider]
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, Culture.DUNLAND, null, Keyword.SOUTHRON));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(charger));
		assertTrue(scn.ShadowActionAvailable(charger2));

		// The other copy first, with one Man.
		scn.ShadowUseCardAction(charger2);
		scn.ShadowChooseCards(warrior2);
		assertTrue(scn.IsType(charger2, CardType.MINION));
		// 4 + 4 for the one Southron stacked on it
		assertEquals(8, scn.GetStrength(charger2));
		assertEquals(1, scn.GetKeywordCount(charger2, Keyword.AMBUSH));

		scn.FreepsPass();
		assertFalse(scn.IsType(charger, CardType.MINION));
		assertFalse(scn.HasKeyword(charger, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertFalse(scn.HasKeyword(charger, Keyword.SOUTHRON));
		assertEquals(0, scn.GetKeywordCount(charger, Keyword.AMBUSH));
		assertEquals(0, scn.GetStackedCards(charger).size());

		scn.ShadowUseCardAction(charger);
		// Southron Men in play, whatever their culture. Not the Easterling (a [raider] Man), not the other
		// Charger (now a Southron minion, and no Man), not the Southron Man in hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior1, explorer, savage));
		assertFalse(scn.ShadowHasCardChoiceAvailable(infantry));
		assertFalse(scn.ShadowHasCardChoiceAvailable(charger2));
		assertFalse(scn.ShadowHasCardChoiceAvailable(scout));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(warrior1, explorer);
		assertInZone(Zone.STACKED, warrior1, explorer);
		assertEquals(2, scn.GetStackedCards(charger).size());
		assertTrue(scn.GetStackedCards(charger).contains(warrior1));
		assertTrue(scn.GetStackedCards(charger).contains(explorer));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertTrue(scn.IsType(charger, CardType.MINION));
		assertTrue(scn.HasKeyword(charger, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));
		// 4 + 4 for each of the 2 Southrons stacked on it
		assertEquals(12, scn.GetStrength(charger));
		assertEquals(4, scn.GetVitality(charger));
		assertEquals(2, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		// The first copy still counts its own stack only.
		assertEquals(1, scn.GetStackedCards(charger2).size());
		assertEquals(8, scn.GetStrength(charger2));
		assertEquals(1, scn.GetKeywordCount(charger2, Keyword.AMBUSH));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedOnceNoSouthronManIsLeftInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var charger2 = scn.GetShadowCard("charger2");
		var warrior1 = scn.GetShadowCard("warrior1");
		var scout = scn.GetShadowCard("scout");
		var infantry = scn.GetShadowCard("infantry");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(charger, charger2);
		scn.MoveCardsToHand(scout);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, infantry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(charger));
		assertTrue(scn.ShadowActionAvailable(charger2));

		// The only Southron Man in play goes onto the other copy.
		scn.ShadowUseCardAction(charger2);
		assertTrue(scn.GetStackedCards(charger2).contains(warrior1));
		assertTrue(scn.IsType(charger2, CardType.MINION));

		scn.FreepsPass();
		// Left: an Easterling, a Southron minion that is no Man (the other copy), and a Southron Man in hand.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(charger));
		assertFalse(scn.IsType(charger, CardType.MINION));
		assertInZone(Zone.SUPPORT, charger);
	}

	@Test
	public void EachSkirmishInvolvingItAddsAThreatAndAnotherMinionsSkirmishAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var warrior1 = scn.GetShadowCard("warrior1");
		var infantry = scn.GetShadowCard("infantry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCardsToSupportArea(charger);
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, infantry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// One Southron Man in play, so it is the one stacked: strength 4 + 4.
		scn.ShadowUseCardAction(charger);
		assertEquals(8, scn.GetStrength(charger));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, charger}, new PhysicalCardImpl[]{boromir, infantry});
		// ambush (1)
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(boromir);
		// The Easterling's skirmish, while the Charger is a minion: no threat.
		assertTrue(scn.IsCharSkirmishing(infantry));
		assertEquals(0, scn.GetThreats());
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(charger));
		assertEquals(1, scn.GetThreats());
		// Aragorn 8 against 8: he loses and takes a wound.
		scn.PassCurrentPhaseActions();

		// It is fierce, and is fought a second time.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, charger);
		scn.ShadowDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(charger));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AddsTwoThreatsWithFourSouthronsStackedAndOneWithThreeAndAnEasterling() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var charger2 = scn.GetShadowCard("charger2");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var warrior3 = scn.GetShadowCard("warrior3");
		var warrior4 = scn.GetShadowCard("warrior4");
		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");
		var scout = scn.GetShadowCard("scout");
		var infantry = scn.GetShadowCard("infantry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToSupportArea(charger, charger2);
		// With Frodo, 5 companions: room for 3 threats
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, warrior2, warrior3, warrior4, explorer, explorer2, scout);
		// Nobody is to be overwhelmed: a death would clear the threats this test reads.
		scn.ApplyAdHocModifier(new StrengthModifier(null, CardType.COMPANION, null, 12));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(warrior1, warrior2, warrior3, warrior4);
		// 4 + 4 for each of 4 Southrons
		assertEquals(20, scn.GetStrength(charger));
		assertEquals(4, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		scn.FreepsPass();
		scn.ShadowUseCardAction(charger2);
		scn.ShadowChooseCards(explorer, explorer2, scout);
		// A fourth card on the second copy that is no Southron
		scn.StackCardsOn(charger2, infantry);
		assertEquals(4, scn.GetStackedCards(charger).size());
		assertEquals(4, scn.GetStackedCards(charger2).size());
		// 4 + 4 for each of 3 Southrons
		assertEquals(16, scn.GetStrength(charger2));
		assertEquals(3, scn.GetKeywordCount(charger2, Keyword.AMBUSH));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, charger2}, new PhysicalCardImpl[]{boromir, charger});
		// ambush on each
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(charger2));
		assertEquals(1, scn.GetThreats());
		// Aragorn 8 + 12 against 16
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishing(charger));
		// 1 + 2
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void StaysAMinionThroughASecondMoveAndIsDiscardedWithItsStackWhenTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var warrior1 = scn.GetShadowCard("warrior1");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		assertTrue(scn.IsType(charger, CardType.MINION));

		// The regroup phase of the first move
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.IsType(charger, CardType.MINION));
		assertTrue(scn.HasKeyword(charger, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(charger));

		// The second move: the same turn, and it is a minion still.
		scn.FreepsChooseToMove();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertTrue(scn.IsType(charger, CardType.MINION));
		assertTrue(scn.HasKeyword(charger, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));
		assertEquals(8, scn.GetStrength(charger));
		assertEquals(4, scn.GetVitality(charger));
		assertEquals(1, scn.GetKeywordCount(charger, Keyword.AMBUSH));
		assertEquals(1, scn.GetStackedCards(charger).size());

		// The turn ends, and every minion is discarded as it does.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.IsType(charger, CardType.MINION));
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		assertInDiscard(charger);
		assertInDiscard(warrior1);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void IsStillAPossessionWhileItIsAMinionAndIsDiscardedAsOneWithItsStack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var charger2 = scn.GetShadowCard("charger2");
		var warrior1 = scn.GetShadowCard("warrior1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thunder = scn.GetFreepsCard("thunder");

		scn.MoveCardsToSupportArea(charger, charger2);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertTrue(scn.IsType(charger, CardType.MINION));
		assertTrue(scn.IsType(charger, CardType.POSSESSION));

		// Roll of Thunder discards a Shadow possession or Shadow artifact: the Charger that is a minion is
		// offered beside the one in the support area.
		scn.FreepsPlayCard(thunder);
		assertTrue(scn.FreepsHasCardChoiceAvailable(charger, charger2));
		scn.FreepsChooseCard(charger);

		assertInDiscard(charger);
		assertInDiscard(warrior1);
		assertInZone(Zone.SUPPORT, charger2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	@Ignore("Q-0077: may the Maneuver ability be used again while the Charger is a minion, to stack more Southron Men on it?")
	public void ManeuverActionCanBeUsedAgainWhileItIsAMinionToStackMoreSouthronMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var explorer = scn.GetShadowCard("explorer");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(charger);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior1, warrior2, explorer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(warrior1);
		assertEquals(8, scn.GetStrength(charger));
		assertEquals(1, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(charger));
		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(warrior2);

		assertEquals(2, scn.GetStackedCards(charger).size());
		assertTrue(scn.IsType(charger, CardType.MINION));
		// 4 + 4 for each of the 2 Southrons now stacked on it, counted once
		assertEquals(12, scn.GetStrength(charger));
		assertEquals(4, scn.GetVitality(charger));
		assertEquals(2, scn.GetKeywordCount(charger, Keyword.AMBUSH));
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
