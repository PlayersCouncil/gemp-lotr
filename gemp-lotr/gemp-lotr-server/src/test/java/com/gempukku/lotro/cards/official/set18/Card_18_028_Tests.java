package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cords", "18_28");
					// Shelob, Last Child of Ungoliant: a [gollum] Spider, strength 8, vitality 8; enduring, fierce, and
					// damage +1 in a fierce skirmish.
					put("shelob", "8_26");
					// Gollum, Nasty Treacherous Creature: a [gollum] minion who is no Shelob; his regroup ability is
					// never used.
					put("gollum", "5_24");
					// Uruk Slayer: an [isengard] Uruk-hai, strength 9, damage +1; his skirmish ability is never used.
					put("slayer", "1_153");

					// Aragorn, Ranger of the North: strength 8, vitality 4; his maneuver ability is never used.
					put("aragorn", "1_89");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus of his own
					put("avenger", "6_27");     // Ent Avenger: a companion; a strength bonus at 3 Ents
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CountlessCordsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Countless Cords
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot Shelob to make a minion strength +1 for each threat you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("cords");

		assertEquals("Countless Cords", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAMinionInNoSkirmishStrengthPlusOneForEachOfThreeThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn, gandalf, avenger);

		var cords = scn.GetShadowCard("cords");
		var shelob = scn.GetShadowCard("shelob");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(cords);
		assertInHand(cords);

		scn.StartGame();
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(shelob, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		// The Uruk Slayer is left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInHand(cords);
		assertEquals(9, scn.GetStrength(slayer));
		assertTrue(scn.ShadowPlayAvailable(cords));
		scn.ShadowPlayCard(cords);

		// Either minion, the one in the skirmish and the one in none; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, slayer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, gandalf, avenger));
		scn.ShadowChooseCard(slayer);
		// 9 + 3
		assertEquals(12, scn.GetStrength(slayer));
		assertEquals(8, scn.GetStrength(shelob));
		assertInDiscard(cords);

		// The threats are counted as the event is played: a fourth one added later changes nothing.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertEquals(12, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneThreatTheBonusIsPlusOneAndLastsOnlyThisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var cords = scn.GetShadowCard("cords");
		var shelob = scn.GetShadowCard("shelob");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(cords);
		assertInHand(cords);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(shelob, slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, shelob}, new PhysicalCardImpl[]{gandalf, slayer});

		// In Shelob's skirmish, the Uruk Slayer, whose skirmish is still to come, is made stronger.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertInHand(cords);
		assertEquals(9, scn.GetStrength(slayer));
		scn.ShadowPlayCard(cords);
		scn.ShadowChooseCard(slayer);
		// 9 + 1
		assertEquals(10, scn.GetStrength(slayer));
		scn.FreepsPass();
		scn.ShadowPass();

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(slayer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutShelobThoughAnotherGollumMinionIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var cords = scn.GetShadowCard("cords");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(cords);
		assertInHand(cords);

		scn.StartGame();
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInHand(cords);
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(cords));
	}
}
