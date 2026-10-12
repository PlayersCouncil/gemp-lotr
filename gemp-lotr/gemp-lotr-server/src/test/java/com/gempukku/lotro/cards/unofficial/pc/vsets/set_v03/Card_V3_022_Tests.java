package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_022_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march1", "103_22");
					put("march2", "103_22");
					put("oath", "8_41");        // Oathbreaker: [gondor] Wraith companion, strength 6; to play, add a threat
					put("sleepless", "8_45");   // Sleepless Dead: [gondor] Wraith companion, strength 7, cost 3; to play, add a threat
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man and no Wraith, strength 8
					put("knight", "5_36");      // Knight of Gondor: "Fellowship: Discard a [gondor] condition to heal this companion."
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith minion, strength 7; a skirmish ability never used here
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] ally; a maneuver ability only
					// Stronger and More Terrible: [gondor] condition, cost 1, "To play, add a threat." Its other text answers
					// a Wraith winning a skirmish, and no test here has a skirmish.
					put("terrible1", "8_47");
					put("terrible2", "8_47");
					put("terrible3", "8_47");
					put("terrible4", "8_47");
					// Saruman's Power (the PC erratum): "Exert an [isengard] minion to discard a Free Peoples condition
					// and then hinder all other conditions."
					put("power1", "51_136");
					put("power2", "51_136");
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, vitality 3, to exert for Saruman's Power
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EtherealMarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ethereal March
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 2 [gondor] Wraiths, the threat limit is +2.
		 * 	While you can spot more threats than companions, each Wraith is strength +1.
		 * 	Each time this condition is hindered or discarded, remove a threat.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("march1");

		assertEquals("Ethereal March", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedToTheSupportAreaItRaisesTheThreatLimitByTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var oath = scn.GetFreepsCard("oath");
		var sleepless = scn.GetFreepsCard("sleepless");
		scn.MoveCompanionsToTable(oath, sleepless);
		scn.MoveCardsToHand(march);

		scn.StartGame();

		// Frodo and two [gondor] Wraiths: three companions.
		assertEquals(3, scn.GetThreatLimit());
		assertTrue(scn.FreepsPlayAvailable(march));

		scn.FreepsPlayCard(march);
		assertInZone(Zone.SUPPORT, march);
		assertNotAttachedTo(march, oath);
		assertNotAttachedTo(march, sleepless);
		// 3 companions + 2
		assertEquals(5, scn.GetThreatLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThreatLimitIsTwoHigherOnceASecondGondorWraithIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oath = scn.GetFreepsCard("oath");
		var sleepless = scn.GetFreepsCard("sleepless");
		var terrible1 = scn.GetFreepsCard("terrible1");
		var terrible2 = scn.GetFreepsCard("terrible2");
		var terrible3 = scn.GetFreepsCard("terrible3");
		var terrible4 = scn.GetFreepsCard("terrible4");
		scn.MoveCompanionsToTable(aragorn, oath);
		scn.MoveCardsToSupportArea(march);
		scn.MoveCardsToHand(sleepless, terrible1, terrible2, terrible3, terrible4);

		scn.StartGame();
		scn.AddThreats(2);

		// Frodo, Aragorn and one [gondor] Wraith: the limit is the three companions.
		assertEquals(3, scn.GetThreatLimit());

		// Sleepless Dead adds a threat to be played, and is the second [gondor] Wraith.
		scn.FreepsPlayCard(sleepless);
		assertEquals(3, scn.GetThreats());
		// 4 companions + 2
		assertEquals(6, scn.GetThreatLimit());

		// Each Stronger and More Terrible adds a threat to be played, so it can only be played below the limit.
		scn.FreepsPlayCard(terrible1);
		assertEquals(4, scn.GetThreats());
		// As many threats as companions: one more is only possible because the limit is higher.
		assertTrue(scn.FreepsPlayAvailable(terrible2));
		scn.FreepsPlayCard(terrible2);
		assertEquals(5, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(terrible3));
		scn.FreepsPlayCard(terrible3);
		assertEquals(6, scn.GetThreats());

		// 6 threats is the limit.
		assertFalse(scn.FreepsPlayAvailable(terrible4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThreatLimitIsNotRaisedByOneGondorWraithBesideAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oath = scn.GetFreepsCard("oath");
		var terrible1 = scn.GetFreepsCard("terrible1");
		var terrible2 = scn.GetFreepsCard("terrible2");
		scn.MoveCompanionsToTable(aragorn, oath);
		scn.MoveCardsToSupportArea(march);
		scn.MoveCardsToHand(terrible1, terrible2);

		scn.StartGame();
		scn.AddThreats(2);

		// Frodo, Aragorn and Oathbreaker. Aragorn is [gondor] and no Wraith.
		assertEquals(3, scn.GetThreatLimit());

		scn.FreepsPlayCard(terrible1);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		// As many threats as companions, and the limit is no higher: the second copy cannot add its threat.
		assertFalse(scn.FreepsPlayAvailable(terrible2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThreatLimitIsNotRaisedByOneGondorWraithBesideASauronWraith() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oath = scn.GetFreepsCard("oath");
		var terrible1 = scn.GetFreepsCard("terrible1");
		var terrible2 = scn.GetFreepsCard("terrible2");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCompanionsToTable(aragorn, oath);
		scn.MoveCardsToSupportArea(march);
		scn.MoveCardsToHand(terrible1, terrible2);

		scn.StartGame();
		// Corpse Lights is a Wraith of the [sauron] culture.
		scn.MoveMinionsToTable(lights);
		scn.AddThreats(2);

		assertEquals(3, scn.GetThreatLimit());

		scn.FreepsPlayCard(terrible1);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		assertFalse(scn.FreepsPlayAvailable(terrible2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWraithOfEitherSideIsStrengthPlusOneOnlyWhileThreatsOutnumberCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oath = scn.GetFreepsCard("oath");
		var sleepless = scn.GetFreepsCard("sleepless");
		var terrible1 = scn.GetFreepsCard("terrible1");
		var terrible2 = scn.GetFreepsCard("terrible2");
		var merchant = scn.GetFreepsCard("merchant");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCompanionsToTable(aragorn, oath, sleepless);
		// An ally is a character and no companion: counted with them, he would make five.
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(march);
		scn.MoveCardsToHand(terrible1, terrible2);

		scn.StartGame();
		scn.MoveMinionsToTable(lights);
		// Frodo, Aragorn and the two Wraiths: as many threats as companions.
		scn.AddThreats(4);

		assertEquals(4, scn.GetThreats());
		assertEquals(6, scn.GetStrength(oath));
		assertEquals(7, scn.GetStrength(sleepless));
		assertEquals(7, scn.GetStrength(lights));
		assertEquals(8, scn.GetStrength(aragorn));

		// A fifth threat: one more than there are companions, the ally not being one.
		scn.FreepsPlayCard(terrible1);
		assertEquals(5, scn.GetThreats());
		assertEquals(7, scn.GetStrength(oath));
		assertEquals(8, scn.GetStrength(sleepless));
		// The Shadow player's [sauron] Wraith too: the text says "each Wraith".
		assertEquals(8, scn.GetStrength(lights));
		// A [gondor] companion who is no Wraith.
		assertEquals(8, scn.GetStrength(aragorn));

		// Two more than there are companions is still +1.
		scn.FreepsPlayCard(terrible2);
		assertEquals(6, scn.GetThreats());
		assertEquals(7, scn.GetStrength(oath));
		assertEquals(8, scn.GetStrength(sleepless));
		assertEquals(8, scn.GetStrength(lights));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AWraithIsStrongerWithThreatsOutnumberingCompanionsEvenWithoutASecondWraith() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetFreepsCard("march1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(aragorn, oath);
		scn.MoveCardsToSupportArea(march);

		scn.StartGame();

		// Frodo, Aragorn and one Wraith, with as many threats as companions.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(6, scn.GetStrength(oath));

		// More threats than companions is a board a discarded companion leaves behind; here it is arranged.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(7, scn.GetStrength(oath));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopyRemovesAThreatWhenItIsDiscardedOrHinderedAndAgainWhenHinderedASecondTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march1 = scn.GetFreepsCard("march1");
		var march2 = scn.GetFreepsCard("march2");
		var terrible = scn.GetFreepsCard("terrible1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var oath = scn.GetFreepsCard("oath");
		var sleepless = scn.GetFreepsCard("sleepless");
		var power1 = scn.GetShadowCard("power1");
		var power2 = scn.GetShadowCard("power2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, oath, sleepless);
		scn.MoveCardsToSupportArea(march1, march2, terrible);
		scn.MoveCardsToHand(power1, power2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddThreats(7);
		scn.SetTwilight(10);

		// 4 companions + 2 for each copy
		assertEquals(8, scn.GetThreatLimit());
		// 6 + 1 for each copy
		assertEquals(8, scn.GetStrength(oath));

		scn.FreepsPass();

		// Saruman's Power discards one Free Peoples condition and hinders every other condition.
		scn.ShadowPlayCard(power1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(march1, march2, terrible));
		scn.ShadowChooseCard(march2);

		assertInZone(Zone.DISCARD, march2);
		assertTrue(scn.IsHindered(march1));
		assertTrue(scn.IsHindered(terrible));
		// One threat for the copy discarded and one for the copy hindered.
		assertEquals(5, scn.GetThreats());
		// No copy has text that applies: the limit is the 4 companions, and 5 threats to 4 companions give no strength.
		assertEquals(4, scn.GetThreatLimit());
		assertEquals(6, scn.GetStrength(oath));

		scn.RestoreCard(march1, terrible);
		assertEquals(5, scn.GetThreats());
		assertEquals(6, scn.GetThreatLimit());
		assertEquals(7, scn.GetStrength(oath));

		// A second Saruman's Power discards the other condition and hinders the remaining copy again.
		scn.ShadowPlayCard(power2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(march1, terrible));
		scn.ShadowChooseCard(terrible);

		assertInZone(Zone.DISCARD, terrible);
		assertTrue(scn.IsHindered(march1));
		// One threat for the copy hindered, none for the other condition discarded.
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardingOneCopyRemovesOneThreatAndDiscardingAnotherGondorConditionRemovesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march1 = scn.GetFreepsCard("march1");
		var march2 = scn.GetFreepsCard("march2");
		var terrible = scn.GetFreepsCard("terrible1");
		var knight = scn.GetFreepsCard("knight");
		var oath = scn.GetFreepsCard("oath");
		var sleepless = scn.GetFreepsCard("sleepless");
		scn.MoveCompanionsToTable(knight, oath, sleepless);
		scn.MoveCardsToSupportArea(march1, march2, terrible);

		scn.StartGame();
		scn.AddThreats(6);

		// 4 companions + 2 for each copy
		assertEquals(8, scn.GetThreatLimit());
		assertEquals(8, scn.GetStrength(oath));

		// Knight of Gondor discards a [gondor] condition of the player's choosing.
		scn.FreepsUseCardAction(knight);
		assertTrue(scn.FreepsHasCardChoiceAvailable(march1, march2, terrible));
		scn.FreepsChooseCard(terrible);
		assertInZone(Zone.DISCARD, terrible);
		assertEquals(6, scn.GetThreats());

		scn.FreepsUseCardAction(knight);
		assertTrue(scn.FreepsHasCardChoiceAvailable(march1, march2));
		scn.FreepsChooseCard(march1);
		assertInZone(Zone.DISCARD, march1);
		assertEquals(5, scn.GetThreats());

		// The other copy is untouched and goes on applying: 4 companions + 2, and 6 + 1.
		assertInZone(Zone.SUPPORT, march2);
		assertFalse(scn.IsHindered(march2));
		assertEquals(6, scn.GetThreatLimit());
		assertEquals(7, scn.GetStrength(oath));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
