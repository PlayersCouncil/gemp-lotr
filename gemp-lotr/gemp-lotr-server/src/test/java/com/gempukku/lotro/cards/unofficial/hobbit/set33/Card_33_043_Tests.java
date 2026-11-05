package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_043_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's Shadow phase
	// is at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sceptre", "33_43");

					// The Great Goblin, Chieftain of the Misty Mountains: unique [moria] Orc, strength 10; a Maneuver ability not used
					put("greatgoblin", "31_36");
					// Azog, Commander of the Wolves Army: unique [moria] Orc who is not The Great Goblin; a Maneuver ability not used
					put("azog", "32_28");
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: unique [gundabad] Orc, strength 5; a Maneuver ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, not unique, strength 5; its play trigger never fires (cheated in)
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: unique [gundabad] Man, strength 4
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4; a Shadow ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GreatGoblinsSceptreStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Great Goblin's Sceptre
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be a unique Orc.
		 * 	While bearer is The Great Goblin, each other Orc is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sceptre");

		assertEquals("Great Goblin's Sceptre", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAUniqueOrcAndOnTheGreatGoblinMakesEachOtherOrcStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var greatgoblin = scn.GetShadowCard("greatgoblin");
		var yazneg = scn.GetShadowCard("yazneg");
		var runner = scn.GetShadowCard("runner");
		var ferny = scn.GetShadowCard("ferny");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(sceptre);

		scn.StartGame();
		assertInHand(sceptre);
		scn.FreepsPass();
		scn.MoveMinionsToTable(greatgoblin, yazneg, runner, ferny, bats);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals(10, scn.GetStrength(greatgoblin));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(4, scn.GetStrength(bats));

		assertTrue(scn.ShadowPlayAvailable(sceptre));
		scn.ShadowPlayCard(sceptre);
		// Unique Orcs of two cultures; not an Orc who is not unique, not a unique minion who is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(greatgoblin, yazneg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, ferny, bats));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(greatgoblin);
		assertAttachedTo(sceptre, greatgoblin);

		// Orcs of two cultures +2; The Great Goblin himself has the Sceptre's printed +2 alone (10 + 2), not +2 more.
		assertEquals(12, scn.GetStrength(greatgoblin));
		assertEquals(7, scn.GetStrength(yazneg));
		assertEquals(7, scn.GetStrength(runner));
		// Minions who are no Orcs: a [gundabad] Man and a [moria] Creature.
		assertEquals(4, scn.GetStrength(ferny));
		assertEquals(4, scn.GetStrength(bats));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BorneByAnotherUniqueOrcBesideTheGreatGoblinOtherOrcsAreNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sceptre = scn.GetShadowCard("sceptre");
		var greatgoblin = scn.GetShadowCard("greatgoblin");
		var azog = scn.GetShadowCard("azog");
		var yazneg = scn.GetShadowCard("yazneg");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(greatgoblin, azog, yazneg, runner);
		scn.AttachCardsTo(azog, sceptre);
		assertAttachedTo(sceptre, azog);

		// The Great Goblin is in play, but the bearer is Azog.
		assertEquals(10, scn.GetStrength(greatgoblin));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
