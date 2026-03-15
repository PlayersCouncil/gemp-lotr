package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charger", "103_41");     // Bladetusk Charger
					put("southron1", "4_222");    // Desert Warrior - Southron Man
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("southron4", "4_222");
					put("orc", "1_271");          // Orc Soldier - not a Southron

					put("aragorn", "1_89");
					put("gandalf", "1_364");      // Gandalf - for Roll of Thunder
					put("thunder", "4_99");       // Roll of Thunder - discards Shadow possession
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
		 * until the end of the turn that is strength +3 and <b>ambush (1)</b> for each Southron stacked here.
		 * At the start of each skirmish involving this minion, add a threat (or 2 threats if there are 4 Southrons stacked here).
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
	}



	@Test
	public void BladetuskChargerManeuverActionOnlyStacksSouthronMen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(charger);
		scn.MoveMinionsToTable(southron1, southron2, orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(charger));
		scn.ShadowUseCardAction(charger);

		// Should only be able to stack Southron Men, not the Orc
		assertTrue(scn.ShadowHasCardChoicesAvailable(southron1, southron2));
		assertFalse(scn.ShadowHasCardChoiceAvailable(orc));
	}

	@Test
	public void BladetuskChargerTransformsIntoMinionWithStackedSouthronBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(charger);
		scn.MoveMinionsToTable(southron1, southron2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Transform by stacking 2 Southrons
		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(southron1, southron2);

		// Verify transformation
		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertTrue(scn.HasKeyword(charger, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(charger, Keyword.MOUNTED));
		assertTrue(scn.HasKeyword(charger, Keyword.SOUTHRON));

		// Verify stacking
		assertEquals(2, scn.GetStackedCards(charger).size());

		// Base strength 4 + (2 Southrons * 3) = 10
		assertEquals(10, scn.GetStrength(charger));

		// Ambush (2) for 2 stacked Southrons
		assertTrue(scn.HasKeyword(charger, Keyword.AMBUSH));
		assertEquals(2, scn.GetKeywordCount(charger, Keyword.AMBUSH));
	}

	@Test
	public void BladetuskChargerAdds1ThreatAtStartOfSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var southron1 = scn.GetShadowCard("southron1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(charger);
		scn.MoveMinionsToTable(southron1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Transform with 1 Southron
		scn.ShadowUseCardAction(charger);
		// southron1 auto-selected

		int threatsBefore = scn.GetThreats();

		scn.PassCurrentPhaseActions(); // Pass remaining Maneuver
		scn.PassCurrentPhaseActions(); // Archery
		scn.PassCurrentPhaseActions(); // Assignment actions
		scn.FreepsAssignToMinions(aragorn, charger);
		scn.ShadowAcceptOptionalTrigger(); //ambush
		scn.FreepsResolveSkirmish(aragorn);

		// At start of skirmish, should add 1 threat (less than 4 Southrons stacked)
		assertEquals(threatsBefore + 1, scn.GetThreats());
	}

	@Test
	public void BladetuskChargerAdds2ThreatsIfFourSouthronsStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(charger);
		scn.MoveMinionsToTable(southron1, southron2, southron3, southron4);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Transform with 4 Southrons
		scn.ShadowUseCardAction(charger);
		scn.ShadowChooseCards(southron1, southron2, southron3, southron4);

		// Base 4 + (4 * 3) = 16 strength, ambush 4
		assertEquals(16, scn.GetStrength(charger));
		assertEquals(4, scn.GetKeywordCount(charger, Keyword.AMBUSH));

		int threatsBefore = scn.GetThreats();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, charger);
		scn.ShadowAcceptOptionalTrigger(); //ambush
		scn.FreepsResolveSkirmish(aragorn);

		// At start of skirmish, should add 2 threats (4+ Southrons stacked)
		assertEquals(threatsBefore + 2, scn.GetThreats());
	}

	@Test
	public void BladetuskChargerIsDiscardedAsAMinionDuringRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var southron1 = scn.GetShadowCard("southron1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(charger);
		scn.MoveMinionsToTable(southron1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowUseCardAction(charger);

		assertInZone(Zone.SHADOW_CHARACTERS, charger);

		// Skip to Regroup and stay
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToStay();

		// Charger should be discarded along with other minions, including stacked cards
		assertInDiscard(charger);
		assertInDiscard(southron1);
	}

	@Test
	public void BladetuskChargerRemainsPossessionWhileTransformedAndCanBeTargeted() throws DecisionResultInvalidException, CardNotFoundException {
		// Roll of Thunder: "Maneuver: Spot Gandalf to discard a Shadow possession or Shadow artifact."
		var scn = GetScenario();

		var charger = scn.GetShadowCard("charger");
		var southron1 = scn.GetShadowCard("southron1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thunder = scn.GetFreepsCard("thunder");
		scn.MoveCardsToSupportArea(charger);
		scn.MoveMinionsToTable(southron1);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(thunder);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Freeps passes first to let Shadow transform
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(charger);

		assertInZone(Zone.SHADOW_CHARACTERS, charger);
		assertTrue(scn.HasKeyword(charger, Keyword.FIERCE)); // Confirm transformation

		scn.FreepsPlayCard(thunder);

		// Charger and its stacked cards should be discarded
		assertInDiscard(charger);
		assertInDiscard(southron1);
	}
}
