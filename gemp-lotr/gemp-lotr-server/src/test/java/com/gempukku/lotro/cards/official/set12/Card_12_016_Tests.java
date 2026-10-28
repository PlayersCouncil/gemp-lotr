package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_12_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attunement", "12_16");

					put("legolas", "11_21");      // Legolas, Companion of the Ring: Elf companion who is no archer, strength 6; +2 at a battleground or forest only
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, strength 6; +3 against a Nazgul only
					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: Elf ally who is no archer, strength 6; a fellowship ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6; a skirmish ability used only on purpose

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, damage +1
					put("savage2", "1_151");

					// The cards laid in the draw deck, each of a blueprint of its own.
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Free Peoples companion
					put("armor", "1_8");          // Dwarven Armor: a Free Peoples possession
					put("fury", "1_4");           // Battle Fury: a Free Peoples event
					put("armory", "1_173");       // Goblin Armory: a Shadow condition
					put("troop", "1_177");        // Goblin Patrol Troop: a Shadow minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AttunementStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Attunement
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot an Elf to reveal cards from the top of your draw deck until you reveal a Shadow card. Make an Elf strength +2 for each card revealed. Place the revealed cards in any order on the bottom of your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attunement");

		assertEquals("Attunement", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RevealsPastFreePeoplesCardsToAShadowConditionMakesAnElfStrengthPlus8AndPlacesThemOnTheBottomInTheOrderChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attunement = scn.GetFreepsCard("attunement");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var fury = scn.GetFreepsCard("fury");
		var armory = scn.GetFreepsCard("armory");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(legolas, arwen, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(attunement);

		scn.StartGame();
		// A companion, a possession and an event of the Free Peoples, then a Shadow condition, then a Shadow
		// minion below it.
		scn.SetFreepsDrawDeck(aragorn, armor, fury, armory, troop);
		scn.MoveMinionsToTable(savage, savage2);
		assertInZone(Zone.DECK, aragorn, armor, fury, armory, troop);
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertSame(troop, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.HAND, attunement);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, savage}, new PhysicalCardImpl[]{arwen, savage2});
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(celeborn));

		assertTrue(scn.FreepsPlayAvailable(attunement));
		scn.FreepsPlayCard(attunement);

		// Four cards are shown to both players; the minion below the Armory is not.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Any Elf: the one skirmishing, one assigned to a skirmish to come, and an ally; not a Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		// The four revealed cards go beneath the deck in the order the player gives, the first highest.
		scn.FreepsChooseCardBPFromSelection(armory);
		scn.FreepsChooseCardBPFromSelection(fury);
		scn.FreepsChooseCardBPFromSelection(armor);

		// 6 + 2 for each of the four cards revealed
		assertEquals(14, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(celeborn));
		assertEquals(5, scn.GetFreepsDeckCount());
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertSame(armory, scn.GetFromBottomOfFreepsDeck(4));
		assertSame(fury, scn.GetFromBottomOfFreepsDeck(3));
		assertSame(armor, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(aragorn, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DISCARD, attunement);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus lasts the skirmish it was made in, and is gone in Arwen's own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoElfAndAnElfAllyIsEnoughToSpotAShadowCardOnTopIsTheOneCardRevealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attunement = scn.GetFreepsCard("attunement");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetFreepsCard("troop");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(attunement);

		scn.StartGame();
		// A Shadow minion on top, a possession below it.
		scn.SetFreepsDrawDeck(troop, armor);
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.DECK, troop, armor);
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.HAND, attunement);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		// Gimli and Frodo: no Elf to spot.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(attunement));

		scn.MoveAlliesToTable(celeborn);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, celeborn);
		assertEquals(6, scn.GetStrength(celeborn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(attunement));
		scn.FreepsPlayCard(attunement);

		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(armor, false));
		scn.DismissRevealedCards();

		// Celeborn is the one Elf, and is made strength +2 with no question; the minion goes beneath the deck.
		assertEquals(8, scn.GetStrength(celeborn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertSame(troop, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DISCARD, attunement);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoShadowCardInTheDeckEveryCardIsRevealedAndCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attunement = scn.GetFreepsCard("attunement");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(attunement);

		scn.StartGame();
		scn.SetFreepsDrawDeck(aragorn, armor);
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.DECK, aragorn, armor);
		assertInZone(Zone.HAND, attunement);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, savage);
		assertEquals(6, scn.GetStrength(legolas));

		assertTrue(scn.FreepsPlayAvailable(attunement));
		scn.FreepsPlayCard(attunement);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(aragorn);

		// 6 + 2 for each of the two cards revealed
		assertEquals(10, scn.GetStrength(legolas));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertSame(armor, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DISCARD, attunement);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyDeckItIsPlayedAndRevealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attunement = scn.GetFreepsCard("attunement");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(attunement);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.MoveMinionsToTable(savage);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, attunement);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(legolas, savage);
		assertEquals(6, scn.GetStrength(legolas));

		assertTrue(scn.FreepsPlayAvailable(attunement));
		scn.FreepsPlayCard(attunement);

		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, attunement);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
