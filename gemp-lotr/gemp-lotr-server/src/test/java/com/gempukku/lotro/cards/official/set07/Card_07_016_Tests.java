package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "7_16");
					// Legolas, Companion of the Ring: an Elf companion, strength 6, no archer; his text needs a
					// battleground or forest, which no site of this path is
					put("legolas", "11_21");
					// Black Rider: a fierce [wraith] Nazgûl, strength 8, +1 for each companion (11 beside three)
					put("rider", "12_161");
					// White Hand Destroyer: a fierce [uruk-hai] Uruk-hai, strength 10; hunter only for sites controlled
					put("destroyer", "17_124");
					// Morgul Hound: a [wraith] Orc, strength 5, not fierce while the Ring-bearer has fewer than 2 wounds
					put("hound", "7_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Arwen, Fair Elf Maiden
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: While skirmishing a fierce minion, Arwen is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Fair Elf Maiden", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void IsStrengthPlus3InHerOwnSkirmishAgainstAFierceMinionOfEitherRoundAndNotWhileWaitingForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var rider = scn.GetShadowCard("rider");
		var destroyer = scn.GetShadowCard("destroyer");
		scn.MoveCompanionsToTable(arwen, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, destroyer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, destroyer}, new PhysicalCardImpl[]{arwen, rider});

		// Legolas's skirmish is fought first: Arwen is assigned to a fierce minion but is not skirmishing yet,
		// and Legolas, an Elf skirmishing a fierce minion, is not reached.
		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, destroyer));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Legolas (6) loses to the Destroyer (10).
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(legolas));

		// Her own skirmish, against the fierce [wraith] Nazgûl: 6 + 3.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, rider));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(legolas));

		// Arwen (9) loses to the Black Rider (8 + 3 companions).
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(arwen));

		// The fierce round: against the fierce [uruk-hai] Uruk-hai, 6 + 3 again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, destroyer}, new PhysicalCardImpl[]{legolas, rider});
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, destroyer));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotStrengthenedSkirmishingAMinionThatIsNotFierceWhileAFierceMinionFightsElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, hound}, new PhysicalCardImpl[]{legolas, rider});

		// The Ring-bearer has no wound, so the Hound is not fierce.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, hound));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
