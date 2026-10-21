package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "10_5");
					put("gimli", "1_13");        // Gimli, Son of Gloin: [dwarven] companion; a skirmish ability used only on purpose

					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("scout", "1_191");       // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("soldier", "1_271");     // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("gollum", "7_59");       // Gollum, Vile Creature: [gollum] minion, strength 5; a skirmish ability used only on purpose
					put("controlled", "15_41");  // Controlled By The Ring: on Gollum; Skirmish: discard it to have him replace a minion skirmishing a companion

					// Cards for the hand, one of each kind; only their being discarded is read.
					put("elf", "1_53");          // Lorien Elf: an [elven] companion
					put("armor", "1_8");         // Dwarven Armor: a [dwarven] possession
					put("axe", "1_3");           // Axe Strike: a [dwarven] event
					put("runner", "1_178");      // Goblin Runner: a [moria] minion

					// Cards laid at the bottom and top of the draw deck; only their culture is read.
					put("count", "4_69");        // Final Count: an [elven] condition
					put("elfcard", "1_53");      // Lorien Elf: an [elven] companion
					put("armorcard", "1_8");     // Dwarven Armor: a [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Arwen, Echo of Lúthien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Skirmish:</b> Discard a card from hand to discard the bottom card of your draw deck. If the bottom card was an [elven] card, each minion skirmishing Arwen is strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Echo of Lúthien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void DiscardsACardFromHandAndAnElvenBottomCardMakesEachMinionSkirmishingHerStrengthMinus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		var runner = scn.GetFreepsCard("runner");
		var count = scn.GetFreepsCard("count");
		var armorcard = scn.GetFreepsCard("armorcard");
		var shadowCount = scn.GetShadowCard("count");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(arwen, gimli);

		scn.StartGame();
		scn.MoveCardsToHand(elf, armor, axe, runner);
		scn.MoveCardsToBottomOfDeck(count);
		scn.MoveCardsToTopOfDeck(armorcard);
		scn.MoveCardsToBottomOfDeck(shadowCount);
		assertInHand(elf, armor, axe, runner);
		assertSame(count, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, count);
		assertSame(armorcard, scn.GetFreepsTopOfDeck());
		assertSame(shadowCount, scn.GetShadowBottomOfDeck());
		int deck = scn.GetFreepsDeckCount();

		scn.MoveMinionsToTable(savage, scout, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage},
				new PhysicalCardImpl[]{gimli, soldier}
		);
		scn.ShadowAssignToMinions(arwen, scout);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arwen));

		scn.FreepsUseCardAction(arwen);
		// Any card from hand: a companion, a possession, an event and a Shadow card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, armor, axe, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertInDiscard(axe);
		assertInHand(elf, armor, runner);
		// The bottom card, an [elven] condition, is discarded; the top card and the Shadow player's deck stay.
		assertInDiscard(count);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(armorcard, scn.GetFreepsTopOfDeck());
		assertSame(shadowCount, scn.GetShadowBottomOfDeck());
		// Both minions skirmishing her are -3; the Soldier, assigned to Gimli and waiting, is not.
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(3, scn.GetStrength(scout));
		assertEquals(7, scn.GetStrength(soldier));
		// Read again at the next decision of the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(3, scn.GetStrength(scout));

		scn.PassSkirmishActions();
		// Arwen (6) beats 2 + 3; each minion takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(scout));
		scn.FreepsResolveSkirmish(gimli);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItWeakensNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var elfcard = scn.GetFreepsCard("elfcard");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(arwen, gimli);

		scn.StartGame();
		scn.MoveCardsToHand(armor);
		scn.MoveCardsToBottomOfDeck(elfcard);
		assertInHand(armor);
		assertSame(elfcard, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, elfcard);

		scn.MoveMinionsToTable(savage, scout, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage},
				new PhysicalCardImpl[]{gimli, soldier}
		);
		scn.ShadowAssignToMinions(arwen, scout);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(arwen);
		// The one card in hand is discarded unasked, and the bottom card is an [elven] companion.
		assertInDiscard(armor, elfcard);
		// The Soldier skirmishes Gimli, not Arwen; the two minions assigned to her are not skirmishing yet.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ABottomCardThatIsNotElvenWeakensNoMinionThoughTheTopCardIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		var elfcard = scn.GetFreepsCard("elfcard");
		var armorcard = scn.GetFreepsCard("armorcard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.MoveCardsToHand(elf);
		scn.MoveCardsToBottomOfDeck(armorcard);
		scn.MoveCardsToTopOfDeck(elfcard);
		assertInHand(elf);
		assertSame(armorcard, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, armorcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsUseCardAction(arwen);

		assertInDiscard(elf, armorcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheCardFromHandIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.MoveCardsToHand(elf);
		assertInHand(elf);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetFreepsDeckCount());
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(arwen));

		scn.FreepsUseCardAction(arwen);
		assertInDiscard(elf);
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithAnEmptyHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var count = scn.GetFreepsCard("count");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.MoveCardsToBottomOfDeck(count);
		assertSame(count, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, count);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arwen));
	}

	@Test
	public void AMinionThatComesIntoHerSkirmishAfterTheUseIsNotMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		var count = scn.GetFreepsCard("count");
		var savage = scn.GetShadowCard("savage");
		var gollum = scn.GetShadowCard("gollum");
		var controlled = scn.GetShadowCard("controlled");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.MoveCardsToHand(elf);
		scn.MoveCardsToBottomOfDeck(count);
		assertInHand(elf);
		assertSame(count, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, count);
		scn.MoveMinionsToTable(savage, gollum);
		scn.AttachCardsTo(gollum, controlled);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		// Gollum is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.IsCharSkirmishingAgainst(arwen, savage));
		assertFalse(scn.IsCharAssigned(gollum));
		scn.FreepsUseCardAction(arwen);
		assertInDiscard(count);
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(gollum));

		// Gollum takes the Savage's place in her skirmish.
		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, gollum));
		assertFalse(scn.IsCharSkirmishing(savage));

		// The minions it reached were the ones skirmishing her when it was used: Gollum keeps his 5, and the
		// Savage keeps its -3 to the end of the skirmish.
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(2, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
