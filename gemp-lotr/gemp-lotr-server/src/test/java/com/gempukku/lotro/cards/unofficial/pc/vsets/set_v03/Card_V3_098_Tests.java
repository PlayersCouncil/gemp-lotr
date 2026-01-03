package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "103_98");    // Red Eye Assassin
					put("runner", "1_178");       // Goblin Runner
					put("hollowing", "3_54");     // Hollowing of Isengard - Shadow condition

					put("aragorn", "1_89");
					put("anduril", "7_79");       // Anduril - Hand Weapon Artifact
					put("bow", "1_90");           // Aragorn's Bow - Ranged Weapon Possession
					put("athelas", "1_94");       // Athelas - non-weapon Possession
					put("lastalliance", "1_49");  // Last Alliance - Condition on Gondor Man
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RedEyeAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Red Eye Assassin
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each hindered card you can spot.
		* 	Skirmish: Exert this minion to hinder a weapon or condition borne by a companion it is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("assassin");

		assertEquals("Red Eye Assassin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}


	@Test
	public void AssassinStrengthScalesWithHinderedCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		var hollowing = scn.GetShadowCard("hollowing");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var lastalliance = scn.GetFreepsCard("lastalliance");

		scn.MoveMinionsToTable(assassin, runner);
		scn.MoveCardsToSupportArea(hollowing);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, lastalliance);

		scn.StartGame();

		// Base strength with 0 hindered
		assertEquals(6, scn.GetStrength(assassin));

		// Hinder FP cards
		scn.HinderCard(anduril);
		assertEquals(7, scn.GetStrength(assassin));

		scn.HinderCard(lastalliance);
		assertEquals(8, scn.GetStrength(assassin));

		// Hinder Shadow card - should also count
		scn.HinderCard(hollowing);
		assertEquals(9, scn.GetStrength(assassin));
	}

	@Test
	public void AssassinHindersWeaponsAndConditionsButNotOtherPossessions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");       // Hand Weapon Artifact - valid
		var bow = scn.GetFreepsCard("bow");               // Ranged Weapon Possession - valid
		var lastalliance = scn.GetFreepsCard("lastalliance"); // Condition - valid
		var athelas = scn.GetFreepsCard("athelas");       // Non-weapon Possession - invalid

		scn.MoveMinionsToTable(assassin);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, bow, lastalliance, athelas);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, assassin);
		scn.FreepsResolveSkirmish(aragorn);

		//Removing the arrow from the bow
		scn.RemoveWoundsFromChar(assassin, 1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);

		// Weapons and condition choosable, non-weapon possession not
		assertTrue(scn.ShadowHasCardChoiceAvailable(anduril));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lastalliance));
		assertFalse(scn.ShadowHasCardChoiceAvailable(athelas));

		scn.ShadowChooseCard(anduril);

		assertEquals(1, scn.GetWoundsOn(assassin));
		assertTrue(scn.IsHindered(anduril));
	}
}
