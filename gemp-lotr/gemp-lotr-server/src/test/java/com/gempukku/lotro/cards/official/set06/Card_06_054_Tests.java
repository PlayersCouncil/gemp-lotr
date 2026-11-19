package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ventures", "6_54");
					// Ranger of Osgiliath: [gondor] Man ranger companion, vitality 3, not unique; a regroup ability
					// (discard 2 cards from hand) used only on purpose
					put("ranger1", "7_116");
					put("ranger2", "7_116");
					// Arwen, Daughter of Elrond: an [elven] Elf ranger companion, vitality 3; +3 only against a Nazgûl
					put("arwen", "1_30");
					// Gondorian Knight: a [gondor] Man companion who is no ranger, vitality 3; Knight is its whole text
					put("knight", "5_35");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop", "1_177");
					// Morgul Hound and Morgul Mongrel: [wraith] Orcs; their texts need wounds on the Ring-bearer
					put("hound", "7_192");
					put("mongrel", "7_194");
					// Úlairë Lemenya: a [wraith] Nazgûl; her text reaches only a bearer of a [wraith] condition
					put("lemenya", "1_232");
					// Black Breath: a [wraith] support-area condition, no minion; its skirmish ability is used only on
					// purpose
					put("breath", "1_207");
					// Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples
					// event that spots or exerts a ranger gains the cost "wound a companion"
					put("wrath", "4_261");
					// Easterling Axeman: [raider] Man, strength 6, vitality 2; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PerilousVenturesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Perilous Ventures
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert 2 rangers to discard a minion (or 2 [wraith] minions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ventures");

		assertEquals("Perilous Ventures", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoRangersToDiscardAMinionOfAnyKindWhereNoTwoWraithMinionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ventures = scn.GetFreepsCard("ventures");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(ranger1, ranger2, arwen, knight);
		scn.MoveCardsToHand(ventures);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var hound = scn.GetShadowCard("hound");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToSupportArea(breath);
		assertInZone(Zone.SUPPORT, breath);

		scn.StartGame();
		assertInZone(Zone.HAND, ventures);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, troop, hound);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(ranger1));
		assertEquals(0, scn.GetWoundsOn(ranger2));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(ventures));

		scn.FreepsPlayCard(ventures);
		// Rangers of two cultures and races; not a companion who is no ranger.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger1, ranger2, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(ranger1, arwen);
		assertEquals(1, scn.GetWoundsOn(ranger1));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(ranger2));
		assertEquals(0, scn.GetWoundsOn(knight));

		// One [wraith] minion alone does not open the parenthesis: one minion of any culture and race; not a
		// [wraith] card that is no minion, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop, hound));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(breath, knight));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(hound);
		assertInDiscard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.SUPPORT, breath);
		assertInDiscard(ventures);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithThreeWraithMinionsTwoOfThemAreChosenAndDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ventures = scn.GetFreepsCard("ventures");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger1, ranger2);
		scn.MoveCardsToHand(ventures);

		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToSupportArea(breath);
		assertInZone(Zone.SUPPORT, breath);

		scn.StartGame();
		assertInZone(Zone.HAND, ventures);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, hound, mongrel, lemenya);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertInZone(Zone.SHADOW_CHARACTERS, mongrel);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPlayCard(ventures);
		// The two rangers are the only ones, and exert unasked.
		assertEquals(1, scn.GetWoundsOn(ranger1));
		assertEquals(1, scn.GetWoundsOn(ranger2));

		// Two [wraith] minions can be spotted, so the parenthesis is what is done (Q-0035, F-0554): two of the
		// three, Orcs and a Nazgûl; not the [isengard] Uruk-hai, nor a [wraith] card that is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hound, mongrel, lemenya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, breath));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(hound, lemenya);
		assertInDiscard(hound);
		assertInDiscard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, mongrel);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, breath);
		assertInDiscard(ventures);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithExactlyTwoWraithMinionsBothAreDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ventures = scn.GetFreepsCard("ventures");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger1, ranger2);
		scn.MoveCardsToHand(ventures);

		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		assertInZone(Zone.HAND, ventures);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, hound, lemenya);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPlayCard(ventures);
		// Exactly two [wraith] minions: both go, with nothing asked, and the Uruk-hai stays.
		assertEquals(1, scn.GetWoundsOn(ranger1));
		assertEquals(1, scn.GetWoundsOn(ranger2));
		assertInDiscard(hound);
		assertInDiscard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInDiscard(ventures);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneOfTwoRangersExhaustedItIsNotPlayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ventures = scn.GetFreepsCard("ventures");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(ranger1, ranger2, knight);
		scn.MoveCardsToHand(ventures);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.HAND, ventures);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(ranger2, 2);
		assertEquals(2, scn.GetWoundsOn(ranger2));
		assertEquals(0, scn.GetWoundsOn(ranger1));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		// One ranger can exert, and so can a companion who is no ranger: the cost cannot be paid.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ventures));
	}

	@Test
	public void WithNoMinionInTheRegroupPhaseItIsPlayedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ventures = scn.GetFreepsCard("ventures");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger1, ranger2);
		scn.MoveCardsToHand(ventures);

		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToSupportArea(breath);
		assertInZone(Zone.SUPPORT, breath);

		scn.StartGame();
		assertInZone(Zone.HAND, ventures);
		// No minion: the game goes straight from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(ranger1));
		assertEquals(0, scn.GetWoundsOn(ranger2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(ventures));

		scn.FreepsPlayCard(ventures);
		assertEquals(1, scn.GetWoundsOn(ranger1));
		assertEquals(1, scn.GetWoundsOn(ranger2));
		assertInZone(Zone.SUPPORT, breath);
		assertInDiscard(ventures);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ventures = scn.GetFreepsCard("ventures");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(ranger1, ranger2, knight);
		scn.MoveCardsToHand(ventures);

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);
		assertInZone(Zone.SUPPORT, wrath);

		scn.StartGame();
		assertInZone(Zone.HAND, ventures);
		scn.FreepsPass();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(ranger1));
		assertEquals(0, scn.GetWoundsOn(ranger2));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(ventures));

		scn.FreepsPlayCard(ventures);
		// The event exerts rangers (the two there are, unasked), so Wrath of Harad, with a [raider] Man spotted,
		// adds "wound a companion" to its cost: a cost of the Free Peoples player's event, which he pays and so
		// chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger1, ranger2, knight));
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(ranger1));
		assertEquals(1, scn.GetWoundsOn(ranger2));
		// The only minion is discarded unasked.
		assertInDiscard(axeman);
		assertInDiscard(ventures);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
