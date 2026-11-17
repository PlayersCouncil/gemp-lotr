package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_063_Tests
{

	// The skirmishes are at site 2 of the Fellowship path, where the Troop (site 4) and the Savage (site 5) are
	// roaming and Nelya (site 2) is not.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mablung", "15_63");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8; a Maneuver ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, site 4; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, site 5; Damage +1 only
					// Úlairë Nelya, Ringwraith in Twilight: site 2, vitality 3; nothing else below 2 burdens or wounds on the Ring-bearer
					put("nelya", "2_84");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MablungStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mablung, Ranger of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>
		 * 	<b>Skirmish:</b> Exert Mablung to wound a roaming minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mablung");

		assertEquals("Mablung", card.getBlueprint().getTitle());
		assertEquals("Ranger of Ithilien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertingMablungWoundsARoamingMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		scn.MoveCompanionsToTable(mablung);
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, nelya);
		// The Uruk Savage, vitality 3, is exhausted.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(mablung, troop);
		// The minions left over are put on Mablung by the Shadow player.
		scn.ShadowAssignToMinions(mablung, savage, nelya);
		scn.FreepsResolveSkirmish(mablung);

		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(mablung));

		scn.FreepsUseCardAction(mablung);
		assertEquals(1, scn.GetWoundsOn(mablung));

		// The roaming minions he is skirmishing, of two cultures and races, the exhausted one among them (a wound,
		// not an exertion); not Nelya, who is not roaming.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nelya, mablung, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileMablungIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		scn.MoveCompanionsToTable(mablung);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(mablung, 2);
		assertEquals(2, scn.GetWoundsOn(mablung));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(mablung, savage);
		scn.FreepsResolveSkirmish(mablung);

		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(mablung));
		assertEquals(0, scn.GetWoundsOn(savage));
	}

	@Test
	public void InAnotherSkirmishOrAgainstAMinionThatIsNotRoamingTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mablung = scn.GetFreepsCard("mablung");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(mablung, aragorn);
		var troop = scn.GetShadowCard("troop");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{mablung, nelya});
		// Aragorn's skirmish first, against a roaming minion Mablung is not skirmishing.
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(mablung));

		scn.FreepsUseCardAction(mablung);
		assertEquals(1, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 8 against 13: Aragorn loses. Then Mablung's own skirmish, against Nelya, who is not roaming.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(mablung);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(mablung));

		scn.FreepsUseCardAction(mablung);
		assertEquals(2, scn.GetWoundsOn(mablung));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
