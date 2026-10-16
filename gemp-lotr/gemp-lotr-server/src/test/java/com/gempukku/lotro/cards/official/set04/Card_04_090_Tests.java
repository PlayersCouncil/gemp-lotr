package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "4_90");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability only ever used on purpose
					put("bounder", "1_286");    // Bounder: [shire] ally of cost 1; a skirmish ability only ever used on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion of cost 6, site 4, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Gandalf, The White Wizard
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: While you can spot 3 twilight tokens, Gandalf is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("The White Wizard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void StrengthPlus3WhileThreeOrMoreTwilightTokensCanBeSpottedAndNotAtTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		// Tokens that are not twilight: a burden and a wound.
		scn.AddBurdens(1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(gandalf));
		// Another companion is not made stronger.
		assertEquals(8, scn.GetStrength(aragorn));

		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusComesAsAPlayAddsTheThirdTokenAndGoesAsAMinionsPlayTakesThePoolBelowThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(bounder, troop);

		scn.StartGame();
		scn.SetTwilight(2);
		assertEquals(7, scn.GetStrength(gandalf));

		// Bounder's cost of 1 is added to the pool.
		scn.FreepsPlayCard(bounder);
		assertEquals(3, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(gandalf));

		scn.FreepsPass();
		// 3 + 3 companions + 2 for Ettenmoors.
		assertEquals(8, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(gandalf));

		// 6 to play + 2 roaming: the pool is emptied.
		scn.ShadowPlayCard(troop);
		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
