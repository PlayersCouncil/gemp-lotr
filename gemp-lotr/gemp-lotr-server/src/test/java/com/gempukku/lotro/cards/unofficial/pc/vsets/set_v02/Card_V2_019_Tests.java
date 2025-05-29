package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deathseeker", "102_19");
					put("savage", "1_151");
					put("machine", "5_60");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerserkDeathseekerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Berserk Deathseeker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Damage +1.
		* 	This minion is strength +1 for each wound on each character in its skirmish.
		* 	When this minion is killed, add 3 [isengard] tokens to an [isengard] machine.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deathseeker");

		assertEquals("Berserk Deathseeker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void BerserkDeathseekerIsStrenghtPlus1ForEachWoundOnEachCharacterInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var deathseeker = scn.GetShadowCard("deathseeker");
		var savage = scn.GetShadowCard("savage");
		var machine = scn.GetShadowCard("machine");
		scn.MoveMinionsToTable(deathseeker, savage);
		scn.MoveCardsToSupportArea(machine);

		var frodo = scn.GetRingBearer();

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(deathseeker, 1);
		scn.AddWoundsToChar(savage, 1);

		//not in skirmish, should be base
		assertEquals(10, scn.GetStrength(deathseeker));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(frodo, deathseeker, savage);
		scn.FreepsResolveSkirmish(frodo);

		//Base strength of 10, +1 strength for the single wound on each of itself, the savage, and frodo
		assertEquals(13, scn.GetStrength(deathseeker));
	}

	@Test
	public void BerserkDeathseekerDyingAdds3TokensToMachine() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var deathseeker = scn.GetShadowCard("deathseeker");
		var machine = scn.GetShadowCard("machine");
		scn.MoveMinionsToTable(deathseeker);
		scn.MoveCardsToSupportArea(machine);

		scn.StartGame();

		scn.AddWoundsToChar(deathseeker, 2);

		assertEquals(1, scn.GetVitality(deathseeker));
		assertEquals(0, scn.GetCultureTokensOn(machine));
		assertEquals(Zone.SHADOW_CHARACTERS, deathseeker.getZone());

		scn.AddWoundsToChar(deathseeker, 1);

		scn.FreepsPassCurrentPhaseAction(); //to get the game to realize he's died

		assertEquals(3, scn.GetCultureTokensOn(machine));
		assertEquals(Zone.DISCARD, deathseeker.getZone());
	}
}
