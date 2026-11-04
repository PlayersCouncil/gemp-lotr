package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_055_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "15_55");
					put("sword", "1_112");          // Ranger's Sword: bearer must be Aragorn; strength +2, damage +1

					put("raiders", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1 only
					put("hound", "7_192");          // Morgul Hound: [wraith] Orc, strength 5 (+4 only at 2 wounds on the Ring-bearer)
					put("warband", "1_272");        // Orc War Band: [sauron] Orc, strength 10, vitality 2; a skirmish ability
					put("butcher", "17_122");       // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2, hunter 6 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Aragorn, Thorongil
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ranger</b>. <b>Hunter 2</b>. <helper>(While skirmishing a non-hunter character, this character is strength +2.)</helper><br><b>Maneuver:</b> Add a threat to exert a non- hunter minion with strength less than Aragorn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Thorongil", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void ManeuverAbilityAddsAThreatToExertANonHunterMinionWeakerThanAragornAsHeNowIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var raiders = scn.GetShadowCard("raiders");
		var hound = scn.GetShadowCard("hound");
		var warband = scn.GetShadowCard("warband");
		var butcher = scn.GetShadowCard("butcher");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, hound, warband, butcher);
		scn.SkipToPhase(Phase.MANEUVER);

		// The sword makes him 10: the Raiding Party at 9 is weaker than he is now, though not than his printed 8.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(10, scn.GetStrength(warband));
		assertEquals(5, scn.GetStrength(butcher));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetThreats());
		// Not the War Band, as strong as he is; not the Butcher, a hunter; not Frodo, who is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(raiders, hound));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(warband, butcher, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(raiders);

		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(warband));
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// Two companions, two threats: the limit.
		scn.AddThreats(2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));
	}
}
