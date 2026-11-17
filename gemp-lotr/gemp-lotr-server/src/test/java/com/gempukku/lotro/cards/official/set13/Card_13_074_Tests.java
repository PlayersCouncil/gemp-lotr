package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rally", "13_74");
					put("isildur", "9_33");      // Isildur, Bearer of Heirlooms: [gondor] Man, strength 7, resistance 6; his texts need him the Ring-bearer or bearing an artifact
					put("aragorn", "11_54");     // Aragorn, Strider: [gondor] Man, strength 8, resistance 8; adds (1) as the fellowship moves
					put("pippin", "103_31");     // Pippin, Trollslayer: a [gondor] Hobbit companion, resistance 6; his skirmish ability is used only on purpose
					put("merchant", "7_97");     // Gondorian Merchant: the [gondor] ally; his ability needs initiative
					put("rider", "4_286");       // Rider of Rohan: a [rohan] Man companion; his ability wants a mount
					put("savage1", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallytheCompanyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Rally the Company
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [gondor] companion strength +2 (or +4 if that companion has resistance 6 or more).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rally");

		assertEquals("Rally the Company", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AGondorCompanionOfResistanceExactly6IsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var rally = scn.GetFreepsCard("rally");
		var isildur = scn.GetFreepsCard("isildur");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var rider = scn.GetFreepsCard("rider");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(isildur, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(rally);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(isildur, savage1);
		scn.FreepsResolveSkirmish(isildur);

		assertEquals(6, scn.GetResistance(isildur));
		assertEquals(7, scn.GetStrength(isildur));
		assertTrue(scn.FreepsPlayAvailable(rally));
		scn.FreepsPlayCard(rally);
		// [gondor] companions of two races, one of them in no skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(isildur, pippin));
		// The [gondor] ally, a [rohan] Man and a [shire] companion.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, rider, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(isildur);

		// 7 + 4
		assertEquals(11, scn.GetStrength(isildur));
		assertInDiscard(rally);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtResistance5ItIsOnlyPlus2AndTheBonusLastsTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(isildur, aragorn);
		scn.MoveCardsToHand(rally);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{isildur, savage1}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(isildur);

		// One burden: Isildur 6 - 1, while Aragorn beside him is 8 - 1.
		assertEquals(5, scn.GetResistance(isildur));
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetStrength(isildur));
		scn.FreepsPlayCard(rally);
		scn.FreepsChooseCard(isildur);
		// 7 + 2
		assertEquals(9, scn.GetStrength(isildur));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// The next skirmish is a phase of its own: the bonus is gone.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(7, scn.GetStrength(isildur));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AResistanceAbove6IsStillPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(rally);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(rally);
		// 8 + 4: "6 or more" is no exact 6.
		assertEquals(12, scn.GetStrength(aragorn));
		assertInDiscard(rally);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var isildur = scn.GetFreepsCard("isildur");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(isildur);
		scn.MoveCardsToHand(rally);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(rally);
		assertFalse(scn.FreepsPlayAvailable(rally));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(isildur, savage1);
		scn.FreepsResolveSkirmish(isildur);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(rally));
	}
}
