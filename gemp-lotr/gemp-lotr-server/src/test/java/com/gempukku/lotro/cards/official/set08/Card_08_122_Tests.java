package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "8_122");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ALLY; a maneuver ability only
					// Oathbreaker: [gondor] Wraith companion, enduring; a skirmish ability only, never used
					put("oathbreaker", "8_41");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and nothing else

					// [gondor] fortifications a minion can bear; each has a maneuver ability only, never used here
					put("firstlevel", "7_92");  // First Level: bearer is strength -1
					put("secondlevel", "7_118");// Second Level

					// Hand cards of every kind, for the cost's choice (all held in hand, where their text does nothing):
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("shadowcard", "1_177"); // Goblin Patrol Troop: a Shadow minion in the Free Peoples hand
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					// Dwarven Armor: cards to hold in hand and nothing else.
					put("filler1", "1_8");
					put("filler2", "1_8");
					put("filler3", "1_8");
					put("filler4", "1_8");
					put("filler5", "1_8");
					put("filler6", "1_8");
					put("filler7", "1_8");
					put("filler8", "1_8");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; a "when you play" only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Pippin, Guard of Minas Tirith
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 5
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: To play, spot a [gondor] Man.
		 * <b>Skirmish:</b> Discard 4 cards from hand to wound a minion Pippin is skirmishing twice if that minion
		 * bears a fortification.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Guard of Minas Tirith", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilAGondorManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var guard = scn.GetFreepsCard("guard");
		// A [gondor] companion who is no Man, and a Man of another culture.
		scn.MoveCompanionsToTable(oathbreaker, guard);
		scn.MoveCardsToHand(pippin, knight);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, oathbreaker, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(knight);
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorManWhoIsAnAllyIsAGondorManToSpotAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToHand(pippin, merchant);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(merchant);
		assertInZone(Zone.SUPPORT, merchant);
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingFourCardsWoundsAFortifiedMinionHeIsSkirmishingTwiceAndKillsAnExhaustedOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		var secondlevel = scn.GetFreepsCard("secondlevel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowcard = scn.GetFreepsCard("shadowcard");
		var deftness = scn.GetFreepsCard("deftness");
		var sword = scn.GetFreepsCard("sword");
		var filler5 = scn.GetFreepsCard("filler5");
		var filler6 = scn.GetFreepsCard("filler6");
		var filler7 = scn.GetFreepsCard("filler7");
		var filler8 = scn.GetFreepsCard("filler8");
		var filler1 = scn.GetFreepsCard("filler1");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(aragorn, shadowcard, deftness, sword, filler5, filler6, filler7, filler8);
		assertInZone(Zone.HAND, aragorn, shadowcard, deftness, sword, filler5, filler6, filler7, filler8);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, runner);
		// Fortifications borne by a [moria] Orc and an [isengard] Uruk-hai; the Goblin Runner bears none.
		scn.AttachCardsTo(troop, firstlevel);
		scn.AttachCardsTo(savage, secondlevel);
		// The Uruk Savage, vitality 3, is exhausted.
		scn.AddWoundsToChar(savage, 2);
		assertAttachedTo(firstlevel, troop);
		assertAttachedTo(secondlevel, savage);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, troop);
		// The minions left over go to Pippin as well.
		scn.ShadowAssignToMinions(pippin, savage, runner);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.IsCharSkirmishing(troop));
		assertTrue(scn.IsCharSkirmishing(savage));
		assertTrue(scn.IsCharSkirmishing(runner));
		assertEquals(8, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		// Cards of every kind and both sides are offered from the hand; a companion in play and a card of the
		// deck are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, shadowcard, deftness, sword, filler5, filler6, filler7, filler8));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, filler1));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, shadowcard, deftness, sword);
		assertInZone(Zone.DISCARD, aragorn, shadowcard, deftness, sword);
		assertEquals(4, scn.GetFreepsHandCount());

		// The two that bear a fortification, the exhausted one among them; not the one that bears none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, pippin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Again, with the last four cards, which go unasked: the exhausted Uruk-hai is wounded, and killed.
		scn.ShadowPass();
		scn.FreepsUseCardAction(pippin);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);
		assertInZone(Zone.DISCARD, savage);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		var secondlevel = scn.GetFreepsCard("secondlevel");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		var filler4 = scn.GetFreepsCard("filler4");
		var filler5 = scn.GetFreepsCard("filler5");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin, knight);
		scn.MoveCardsToHand(filler1, filler2, filler3, filler4, filler5);
		assertInZone(Zone.HAND, filler1, filler2, filler3, filler4, filler5);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.AttachCardsTo(troop, firstlevel);
		scn.AttachCardsTo(savage, secondlevel);
		assertAttachedTo(firstlevel, troop);
		assertAttachedTo(secondlevel, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop}, new PhysicalCardImpl[]{knight, savage});
		// The Knight's skirmish first: the Troop, assigned to Pippin, waits for his.
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharSkirmishing(troop));
		assertTrue(scn.IsCharAssigned(troop));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		scn.FreepsChooseCards(filler1, filler2, filler3, filler4);
		assertInZone(Zone.DISCARD, filler1, filler2, filler3, filler4);
		// No minion Pippin is skirmishing: neither the one in this skirmish nor the one still to fight him.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, firstlevel);
		assertAttachedTo(firstlevel, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, troop);
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
	}
}
