package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "103_78");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [ringwraith] Nazgul, strength 9, vitality 2; his text is about companions bearing [wraith] conditions
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, fierce; the rest of his text needs 3 burdens
					put("cur", "7_189");        // Morgul Cur: [ringwraith] Orc, strength 8, and +2 while a Nazgul can be spotted

					put("destroyed", "1_220");  // Not Easily Destroyed: [ringwraith] condition, cost 1, support area; an ability never used here
					put("wreathed", "1_238");   // Wreathed in Shadow: [ringwraith] condition, cost 0, support area; to play, spot a Nazgul
					put("servants", "2_77");    // His Terrible Servants: [ringwraith] condition; only ever in hand
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] condition, cost 1, support area; does nothing without an Uruk-hai
					put("pinned", "1_192");     // Pinned Down: [moria] condition; only ever in the other player's discard pile
					put("lord", "1_21");        // Lord of Moria: a Free Peoples condition that plays to the support area
					put("steed", "1_208");      // Black Steed: a Shadow possession a Nazgul can bear

					put("aragorn", "1_89");     // strength 8, vitality 4
					put("gimli", "1_13");       // strength 6, vitality 3
					put("sam", "1_311");        // strength 3, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Otsea, Consecrated by Pestilence
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	Each time a Nazgul wins a skirmish, you may exert this minion and remove (1) to play a Shadow condition
		 * 	from your discard pile. Add a threat if it is [ringwraith].
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Ulaire Otsea", card.getBlueprint().getTitle());
		assertEquals("Consecrated by Pestilence", card.getBlueprint().getSubtitle());
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
	public void IsPlayedForFiveAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(otsea));

		scn.ShadowPlayCard(otsea);

		// 13 - 5 to play - 2 roaming: his site number is 3 and the fellowship is at site 2
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWinByOtseaMayExertHimAndRemoveOneToPlayAShadowConditionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var otsea = scn.GetShadowCard("otsea");
		// Two Shadow conditions, of two cultures.
		var destroyed = scn.GetShadowCard("destroyed");
		var armory = scn.GetShadowCard("armory");
		// In the same discard pile: a Free Peoples condition and a Shadow possession Otsea could bear.
		var lord = scn.GetShadowCard("lord");
		var steed = scn.GetShadowCard("steed");
		// Shadow conditions in hand, in the draw deck and in the other player's discard pile.
		var servants = scn.GetShadowCard("servants");
		var wreathed = scn.GetShadowCard("wreathed");
		var theirPinned = scn.GetFreepsCard("pinned");
		scn.MoveCardsToDiscard(destroyed, armory, lord, steed);
		scn.MoveCardsToDiscard(theirPinned);
		scn.MoveCardsToHand(servants);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);

		assertEquals(10, scn.GetStrength(otsea));
		assertEquals(8, scn.GetStrength(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, wreathed);

		scn.PassSkirmishActions();

		// Otsea 10 beats Aragorn 8
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());

		scn.ShadowChooseOptionalTrigger(otsea);

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(destroyed, armory));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lord, steed, servants, wreathed, theirPinned));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		// The [isengard] condition.
		scn.ShadowChooseCard(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertInZone(Zone.DISCARD, destroyed, lord, steed);
		// 4 - 1 for Uruk-hai Armory
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Otsea is fierce and fights Aragorn again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));

		scn.ShadowChooseOptionalTrigger(otsea);

		// The [ringwraith] condition is the only Shadow condition left in the discard pile.
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertInZone(Zone.SUPPORT, destroyed);
		assertInZone(Zone.DISCARD, lord, steed);
		// 3 - 1 removed - 1 for Not Easily Destroyed
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherNazgulWinningIsAnsweredWhateverItsCultureAndOtseaIsTheOneWhoExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var destroyed = scn.GetShadowCard("destroyed");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToDiscard(destroyed, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, lemenya, khamul);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, khamul}, new PhysicalCardImpl[]{sam, lemenya});
		// Otsea is left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(10);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(3, scn.GetStrength(sam));

		scn.PassSkirmishActions();

		// The [ringwraith] Lemenya, 9, overwhelms Sam, 3.
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));

		scn.ShadowChooseOptionalTrigger(otsea);

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(destroyed, armory));

		// A condition that is not [ringwraith], after a [ringwraith] Nazgul's win: no threat.
		scn.ShadowChooseCard(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertEquals(8, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The [raider] Khamul, 12, beats Aragorn, 8.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));

		scn.ShadowChooseOptionalTrigger(otsea);

		// A [ringwraith] condition, after a Nazgul's win who is not [ringwraith]: a threat.
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertInZone(Zone.SUPPORT, destroyed);
		// 8 - 1 removed - 1 for Not Easily Destroyed
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotAnsweredWhenANazgulLosesNorWhenAMinionThatIsNoNazgulWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		var cur = scn.GetShadowCard("cur");
		var destroyed = scn.GetShadowCard("destroyed");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToDiscard(destroyed, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, lemenya, cur);
		// Aragorn 8 + 3 = 11, to beat Lemenya's 9 without killing him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, cur});
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(10);
		scn.FreepsResolveSkirmish(gimli);

		// Morgul Cur 8 + 2 with a Nazgul to spot
		assertEquals(10, scn.GetStrength(cur));
		assertEquals(6, scn.GetStrength(gimli));

		scn.PassSkirmishActions();

		// A [ringwraith] Orc wins.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(cur));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(lemenya));

		scn.PassSkirmishActions();

		// A Nazgul loses, and survives.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DISCARD, destroyed, armory);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// On the same board Otsea, who is fierce, beats Gimli.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, otsea);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
	}

	@Test
	public void DecliningLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var otsea = scn.GetShadowCard("otsea");
		var destroyed = scn.GetShadowCard("destroyed");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToDiscard(destroyed, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, destroyed, armory);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileOtseaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var otsea = scn.GetShadowCard("otsea");
		var destroyed = scn.GetShadowCard("destroyed");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToDiscard(destroyed, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		// Vitality 3: two wounds exhaust him.
		scn.AddWoundsToChar(otsea, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, destroyed, armory);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// With one wound fewer he can exert, and his next win is answered.
		scn.RemoveWoundsFromChar(otsea, 1);
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
	}

	@Test
	public void NotOfferedWithAnEmptyPoolAndWithOneInItOnlyAConditionThatCostsNothingCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var otsea = scn.GetShadowCard("otsea");
		// Wreathed in Shadow costs 0, Not Easily Destroyed 1.
		var wreathed = scn.GetShadowCard("wreathed");
		var destroyed = scn.GetShadowCard("destroyed");
		scn.MoveCardsToDiscard(wreathed, destroyed);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertInZone(Zone.DISCARD, wreathed, destroyed);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(1);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));

		scn.ShadowChooseOptionalTrigger(otsea);

		// The 1 is removed, and what is left pays for the condition that costs 0 and not for the one that costs 1.
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, wreathed);
		assertInZone(Zone.DISCARD, destroyed);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePoolCanPayTheOneButNotTheConditionAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var otsea = scn.GetShadowCard("otsea");
		// Each costs 1.
		var destroyed = scn.GetShadowCard("destroyed");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToDiscard(destroyed, armory);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(1);
		scn.PassSkirmishActions();

		// 1 in the pool: once it is removed nothing is left for a condition that costs 1.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, destroyed, armory);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.PassSkirmishActions();

		// 2 in the pool: 1 to remove and 1 for the condition.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));

		scn.ShadowChooseOptionalTrigger(otsea);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(destroyed, armory));
		scn.ShadowChooseCard(armory);

		assertInZone(Zone.SUPPORT, armory);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoShadowConditionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var otsea = scn.GetShadowCard("otsea");
		// A Free Peoples condition and a Shadow possession in the discard pile, a Shadow condition in hand.
		var lord = scn.GetShadowCard("lord");
		var steed = scn.GetShadowCard("steed");
		var servants = scn.GetShadowCard("servants");
		var destroyed = scn.GetShadowCard("destroyed");
		scn.MoveCardsToDiscard(lord, steed);
		scn.MoveCardsToHand(servants);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, lord, steed);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// With a Shadow condition beside them, his next win is answered.
		scn.MoveCardsToDiscard(destroyed);
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
	}
}
