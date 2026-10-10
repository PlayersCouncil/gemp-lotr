package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_139");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savagery", "1_139");

					// Aragorn, Ranger of the North: strength 8, vitality 4, so the Savage at 5 + 4 beats him
					// without overwhelming him
					put("aragorn", "1_89");
					// Companions of three more titles, to make five with the Ring-bearer.  None has a text that
					// touches a skirmish he is not in.
					put("sam", "1_311");        // Sam, Son of Hamfast
					put("merry", "1_303");      // Merry, From O'er the Brandywine
					put("pippin", "1_306");     // Pippin, Friend to Frodo

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, Damage +1; the Uruk-hai in no skirmish
					// Isengard Worker: an [isengard] Orc, strength 5, with a regroup ability used only on purpose.
					// The culture without the race.
					put("worker", "3_62");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SavagerytoMatchbrTheirNumbersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Savagery to Match Their Numbers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Uruk-hai strength +2, or spot 5 companions to make an Uruk-hai strength +4 and <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Savagery to Match Their Numbers", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFiveCompanionsTheShadowPlayerChoosesBetweenStrengthPlus2AndStrengthPlus4WithFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		// With the Ring-bearer, five companions
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("sam"), scn.GetFreepsCard("merry"), scn.GetFreepsCard("pippin"));

		var savagery = scn.GetShadowCard("savagery");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders, worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(savagery));

		scn.ShadowPlayCard(savagery);
		// Both halves of the "or" can be done, so which is done is the Shadow player's to say.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("+2"));
		assertTrue(scn.ShadowChoiceAvailable("+4"));

		scn.ShadowChoose("+4");
		// An Uruk-hai, in the skirmish or out of it; the [isengard] Orc is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(savage);
		// 5 + 4
		assertEquals(9, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(raiders));
		assertFalse(scn.HasKeyword(raiders, Keyword.FIERCE));
		assertInDiscard(savagery);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFourCompanionsItMakesAnUrukHaiStrengthPlus2AndTheShadowPlayerIsNotAskedWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		// With the Ring-bearer, four companions: one short of the five the second half spots
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("sam"), scn.GetFreepsCard("merry"));

		var savagery = scn.GetShadowCard("savagery");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(savagery);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders, worker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.ShadowPlayAvailable(savagery));

		scn.ShadowPlayCard(savagery);
		// "Spot 5 companions to ..." cannot be done, so there is nothing to choose between: the game goes
		// straight to which Uruk-hai.
		assertEquals(0, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker));

		scn.ShadowChooseCard(savage);
		// 5 + 2
		assertEquals(7, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(raiders));
		assertInDiscard(savagery);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
