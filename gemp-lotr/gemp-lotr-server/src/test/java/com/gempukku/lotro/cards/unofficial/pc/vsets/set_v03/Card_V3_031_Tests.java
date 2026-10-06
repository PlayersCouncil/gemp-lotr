package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_031_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "103_31");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability never used here
					put("oathbreaker", "8_41");   // Oathbreaker: a [gondor] companion who is a Wraith; a skirmish ability that needs 3 cards in hand
					put("knight", "5_35");        // Gondorian Knight: a [gondor] Man to play from hand for (2), with no requirement and no text but "Knight."
					put("merchant", "7_97");      // Gondorian Merchant: the one [gondor] ally; a maneuver ability that needs initiative
					put("guard", "1_7");          // Dwarf Guard: a [dwarven] companion with no text once in play

					// Uruk Savage: [isengard], strength 5, vitality 3; "Damage +1." is its whole text
					put("savage1", "1_151");
					put("savage2", "1_151");
					put("soldier", "4_254");      // Southron Soldier: [raider], strength 4; "Southron. Ambush (2)." is its whole text
					// Let Her Deal With Them: a Shadow condition, "Bearer must be a minion. Bearer is damage +1." Its other
					// text is about being played.
					put("deal", "7_63");
					put("pillager", "10_92");     // Mordor Pillager: [sauron], strength 9, vitality 2; "Besieger. Fierce." and a Shadow ability that needs another [sauron] minion
					// Easterling Blademaster: [raider], strength 8, vitality 2; "Easterling. Skirmish: Exert this minion to make him damage +1."
					put("blademaster1", "7_142");
					put("blademaster2", "7_142");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pippin, Trollslayer
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: Knight. Enduring. To play, spot a [gondor] companion.
		 * 	Skirmish: Exert Pippin twice to make a minion he is skirmishing lose all keywords until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Trollslayer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForTwoToTheFellowshipWhenAGondorWraithIsTheCompanionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		scn.MoveCompanionsToTable(oathbreaker);
		scn.MoveCardsToHand(pippin);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedBesideAGondorAllyAndADwarvenCompanionUntilAGondorManIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		// A [gondor] card that is no companion, a companion that is not [gondor], and Frodo.
		scn.MoveAlliesToTable(merchant);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(pippin, knight);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(knight);

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsPlayCard(pippin);

		// 2 for the Knight + 2 for Pippin
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsPippinTwiceToMakeOneOfTheMinionsHeIsSkirmishingLoseItsKeywords() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var soldier = scn.GetShadowCard("soldier");
		var deal = scn.GetShadowCard("deal");
		scn.MoveCompanionsToTable(pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, soldier);
		// This Savage has a damage +1 printed on it and a second from the condition it bears.
		scn.AttachCardsTo(savage1, deal);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// A skirmish ability: not on offer in the phase before.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { pippin, savage1 },
				new PhysicalCardImpl[] { aragorn, savage2 }
		);
		// The Shadow player puts the Soldier on Pippin too, so Pippin skirmishes two minions of different cultures.
		scn.ShadowAssignToMinions(pippin, soldier);
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(2, scn.GetKeywordCount(savage1, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(soldier, Keyword.SOUTHRON));
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(savage2, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);

		// The cost is paid before the minion is chosen.
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier));
		// The other Savage is assigned to Aragorn, whose skirmish has not begun.
		assertFalse(scn.FreepsHasCardChoiceAvailable(savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(savage1);

		// The printed keyword and the one the condition gives are both lost. The condition stays where it is,
		// and the Savage is still an Uruk-hai.
		assertFalse(scn.HasKeyword(savage1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(savage1, Keyword.DAMAGE));
		assertAttachedTo(deal, savage1);
		assertTrue(scn.IsRace(savage1, Race.URUK_HAI));
		// The minion not chosen and the minion in the other skirmish keep theirs.
		assertTrue(scn.HasKeyword(soldier, Keyword.SOUTHRON));
		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(savage2, Keyword.DAMAGE));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionStrippedOfFierceFightsNoFierceSkirmishAndHasItsKeywordsBackAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pillager = scn.GetShadowCard("pillager");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager, savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { pippin, pillager },
				new PhysicalCardImpl[] { aragorn, savage1 }
		);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.HasKeyword(pillager, Keyword.BESIEGER));
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));

		// The Pillager is the only minion Pippin is skirmishing, so there is nothing to choose.
		scn.FreepsUseCardAction(pippin);

		assertEquals(2, scn.GetWoundsOn(pippin));
		assertFalse(scn.HasKeyword(pillager, Keyword.BESIEGER));
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();

		// Pippin: 3 + 4 for two wounds (enduring) = 7 against 9. He loses and takes one wound.
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(pillager));

		scn.FreepsResolveSkirmish(aragorn);

		// A skirmish later the Pillager is still without them, and the Savage never lost anything.
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.HasKeyword(pillager, Keyword.BESIEGER));
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage1, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();

		// Aragorn's 8 beats the Savage's 5. The Pillager is in play and was fierce, but there is no fierce
		// skirmish to assign: the game goes on to the regroup phase, where its keywords are back.
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(pillager, Keyword.BESIEGER));
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishPippinExertsTwiceAndNoMinionLosesAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, savage1 },
				new PhysicalCardImpl[] { pippin, savage2 }
		);
		scn.FreepsResolveSkirmish(aragorn);

		// Pippin is assigned to a Savage but is not yet skirmishing it; the skirmish going on is Aragorn's.
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertTrue(scn.IsCharAssigned(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		// The cost can be paid, so the ability is on offer.
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);

		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetKeywordCount(savage1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBeUsedWithOnlyOneExertionLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(pippin, aragorn);
		// Vitality 4 with 1 wound: two exertions are exactly what he has left.
		scn.AddWoundsToChar(pippin, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, savage1);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.FreepsActionAvailable(pippin));

		// The fellowship is at Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2."
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(pippin);
		scn.ShadowPass();

		// Vitality 4 with 2 wounds: he could exert once more, but not twice.
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		assertFalse(scn.FreepsActionAvailable(pippin));
	}

	@Test
	public void AMinionThatHasLostItsKeywordsCannotBeGivenOneBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var blademaster1 = scn.GetShadowCard("blademaster1");
		var blademaster2 = scn.GetShadowCard("blademaster2");
		scn.MoveCompanionsToTable(pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(blademaster1, blademaster2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { pippin, blademaster1 },
				new PhysicalCardImpl[] { aragorn, blademaster2 }
		);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.HasKeyword(blademaster1, Keyword.EASTERLING));
		assertFalse(scn.HasKeyword(blademaster1, Keyword.DAMAGE));

		scn.FreepsUseCardAction(pippin);

		assertFalse(scn.HasKeyword(blademaster1, Keyword.EASTERLING));
		assertEquals(0, scn.GetWoundsOn(blademaster1));
		assertTrue(scn.ShadowActionAvailable(blademaster1));

		// "Exert this minion to make him damage +1": the exertion is made and the keyword is not gained.
		scn.ShadowUseCardAction(blademaster1);

		assertEquals(1, scn.GetWoundsOn(blademaster1));
		assertFalse(scn.HasKeyword(blademaster1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(blademaster1, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();

		// Pippin: 3 + 4 for two wounds (enduring) = 7 against 8. He loses and takes one wound, not the two a
		// damage +1 would have made it: 3 wounds on vitality 4, and he lives.
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowUseCardAction(blademaster2);

		// The other Blademaster, who lost nothing, gains it in the ordinary way.
		assertEquals(1, scn.GetWoundsOn(blademaster2));
		assertEquals(1, scn.GetKeywordCount(blademaster2, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(blademaster1, Keyword.EASTERLING));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
