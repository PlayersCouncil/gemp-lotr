package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_275_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forth", "4_275");
					put("theoden", "4_292");    // Théoden, Son of Thengel: vitality 2, the Aragorn signet; a regroup ability
					put("theoden2", "4_365");   // Théoden, Lord of the Mark: vitality 2, the Théoden signet; a fellowship ability
					put("eomer", "4_267");      // Éomer, Third Marshal of Riddermark: [rohan] Man, the Théoden signet; strength +2 per wound on his minions
					put("gimli", "5_7");        // Gimli, Skilled Defender: [dwarven] Dwarf, the Théoden signet; an optional trigger on his win
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] ally, no signet; an archery ability
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; damage +1 only
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForthEorlingasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Forth Eorlingas!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert Théoden to make a companion who has the Théoden signet <b>defender +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forth");

		assertEquals("Forth Eorlingas!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheodenToMakeACompanionWithTheTheodenSignetDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forth = scn.GetFreepsCard("forth");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var gimli = scn.GetFreepsCard("gimli");
		var aldor = scn.GetFreepsCard("aldor");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(theoden, eomer, gimli);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(forth);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(forth));
		scn.FreepsPlayCard(forth);

		// Théoden, the one Théoden, exerts; Éomer, who could exert as well, does not.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));
		// Companions of two cultures and races with the Théoden signet; not Théoden (Aragorn signet), Frodo (Frodo
		// signet) or an ally (no signet).
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden, frodo, aldor, savage1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		assertInZone(Zone.DISCARD, forth);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DefenderLastsThroughTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forth = scn.GetFreepsCard("forth");
		var theoden2 = scn.GetFreepsCard("theoden2");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(theoden2, eomer);
		scn.MoveCardsToHand(forth);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(theoden2));
		scn.FreepsPlayCard(forth);
		// This Théoden pays, and has the Théoden signet himself.
		assertEquals(1, scn.GetWoundsOn(theoden2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(theoden2, eomer));
		scn.FreepsChooseCard(eomer);
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theoden2, savage1}, new PhysicalCardImpl[]{eomer, savage2});
		scn.FreepsResolveSkirmish(theoden2);
		scn.PassSkirmishActions();

		// In the skirmish after one already resolved, Éomer is still defender +1.
		scn.FreepsResolveSkirmish(eomer);
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DEFENDER));
	}

	@Test
	public void CannotBePlayedWhileTheodenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forth = scn.GetFreepsCard("forth");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(theoden, eomer);
		scn.MoveCardsToHand(forth);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.AddWoundsToChar(theoden, 1);
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Théoden, vitality 2, is exhausted; Éomer, another [rohan] companion, could exert but is no Théoden.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(forth));
		assertInZone(Zone.HAND, forth);
	}

	@Test
	public void WithNoCompanionWhoHasTheTheodenSignetTheodenExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forth = scn.GetFreepsCard("forth");
		var theoden = scn.GetFreepsCard("theoden");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(theoden);
		scn.MoveCardsToHand(forth);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsPlayAvailable(forth));
		scn.FreepsPlayCard(forth);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertInZone(Zone.DISCARD, forth);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
