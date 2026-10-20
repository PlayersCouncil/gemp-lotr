package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_308_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("power1", "1_308");
					put("power2", "1_308");
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion who is not the Ring-bearer
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PowerAccordingtoHisStatureStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Power According to His Stature
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Add a burden to wound each minion skirmishing the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("power1");

		assertEquals("Power According to His Stature", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsABurdenToWoundEachMinionSkirmishingTheRingBearerAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var power1 = scn.GetFreepsCard("power1");
		var power2 = scn.GetFreepsCard("power2");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(power1, power2);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, savage1}, new PhysicalCardImpl[]{sam, savage2});
		// The Troop, left over, goes to Frodo too: two minions of different cultures and races skirmish the Ring-bearer.
		scn.ShadowAssignToMinions(frodo, troop);

		// Sam's skirmish first. The Savage there skirmishes a companion who is not the Ring-bearer, and the two
		// minions assigned to Frodo are not skirmishing yet: the burden is added and nobody is wounded.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(power1));
		scn.FreepsPlayCard(power1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.DISCARD, power1);

		// Sam 3 against the Savage's 5: Sam loses and takes 2 wounds (damage +1), and lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));

		// Frodo's skirmish: each minion skirmishing him is wounded once, the Savage that fought Sam is not.
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPlayCard(power2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, power2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheAssignmentPhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power1 = scn.GetFreepsCard("power1");
		scn.MoveCardsToHand(power1);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(power1));
	}
}
