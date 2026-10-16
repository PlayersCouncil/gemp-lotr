package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale", "3_9");
					// Lorien Elf: [elven] Elf companion, strength 4, vitality 2; "To play, spot an Elf", so it is cheated in
					put("elf", "1_53");
					// Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3, strength 5, vitality 2; has the text of
					// site 3 (Rivendell Valley: a fellowship action) while that site is on the path
					put("silinde", "1_60");
					// Orophin, Lorien Bowman: [elven] Elf ally, home site 6, vitality 3; an archery ability on Uruk-hai
					put("orophin", "1_56");
					// Thrarin, Dwarven Smith: [dwarven] Dwarf ally, home site 3, vitality 2; a maneuver ability
					put("thrarin", "1_27");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7; a skirmish ability used only on purpose
					put("boromir", "1_97");
					// Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Footman of Dunland: [men] Man, strength 7, vitality 3; the site gains plains
					put("footman", "11_82");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop", "1_177");

					// Cards laid at the top and bottom of the draw deck; only their culture is read.
					put("elfcard", "1_53");     // Lorien Elf: an [elven] companion
					put("count", "4_69");       // Final Count: an [elven] condition
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerenAndLuthienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Beren and Lúthien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Plays to your support area.
		 * <b>Skirmish:</b> Exert an [elven] ally to discard the bottom card of your draw deck. If that card is an
		 * [elven] card, make a minion skirmishing an Elf strength -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tale");

		assertEquals("Beren and Lúthien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityExertsAnElvenAllyToDiscardTheBottomCardAndAnElvenCardWeakensAMinionSkirmishingAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var elf = scn.GetFreepsCard("elf");
		var boromir = scn.GetFreepsCard("boromir");
		var silinde = scn.GetFreepsCard("silinde");
		var orophin = scn.GetFreepsCard("orophin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elfcard = scn.GetFreepsCard("elfcard");
		var count = scn.GetFreepsCard("count");
		var armor = scn.GetFreepsCard("armor");
		var shadowCount = scn.GetShadowCard("count");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elf, boromir);
		scn.MoveAlliesToTable(silinde, orophin, thrarin);
		scn.MoveCardsToSupportArea(tale);

		scn.StartGame();
		// Final Count at the bottom, the Lorien Elf above it, and the Dwarven Armor on top.
		scn.MoveCardsToBottomOfDeck(elfcard, count);
		scn.MoveCardsToTopOfDeck(armor);
		scn.MoveCardsToBottomOfDeck(shadowCount);
		assertSame(count, scn.GetFreepsBottomOfDeck());
		assertSame(elfcard, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertSame(shadowCount, scn.GetShadowBottomOfDeck());
		assertInZone(Zone.DECK, elfcard, count, armor);
		assertInZone(Zone.SUPPORT, tale);
		int deck = scn.GetFreepsDeckCount();

		scn.MoveMinionsToTable(savage, footman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, savage},
				new PhysicalCardImpl[]{boromir, troop}
		);
		scn.ShadowAssignToMinions(elf, footman);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(tale));

		scn.FreepsUseCardAction(tale);
		// The two [elven] allies, of different homes, and not an [elven] companion or an ally of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elf, thrarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(orophin);

		// The bottom card, an [elven] condition, is discarded, and the rest of the deck, the top card and the
		// Shadow player's deck are as they were.
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertInDiscard(count);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(elfcard, scn.GetFreepsBottomOfDeck());
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertSame(shadowCount, scn.GetShadowBottomOfDeck());
		// In Boromir's skirmish the Troop fights a companion who is no Elf, and the two minions assigned to the
		// Elf are not skirmishing yet: no minion is made weaker.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(elf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(tale);
		scn.FreepsChooseCard(silinde);
		assertEquals(1, scn.GetWoundsOn(silinde));
		// The bottom card is now the Lorien Elf, an [elven] companion.
		assertInDiscard(elfcard);

		// Both minions in the Elf's skirmish; not the Troop, whose skirmish is over.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		assertEquals(6, scn.GetStrength(footman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.SUPPORT, tale);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ABottomCardThatIsNotElvenWeakensNoMinionThoughTheTopCardIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var elf = scn.GetFreepsCard("elf");
		var silinde = scn.GetFreepsCard("silinde");
		var elfcard = scn.GetFreepsCard("elfcard");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(tale);

		scn.StartGame();
		scn.MoveCardsToBottomOfDeck(armor);
		scn.MoveCardsToTopOfDeck(elfcard);
		assertSame(armor, scn.GetFreepsBottomOfDeck());
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, armor, elfcard);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(silinde));
		scn.FreepsUseCardAction(tale);

		// Silinde is the one [elven] ally and exerts unasked; the [dwarven] bottom card is discarded and the
		// minion keeps its strength, though the card on top is an [elven] one.
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertInDiscard(armor);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheAbilityExertsTheAllyForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var elf = scn.GetFreepsCard("elf");
		var silinde = scn.GetFreepsCard("silinde");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToSupportArea(tale);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(0, scn.GetFreepsDeckCount());
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(tale));

		scn.FreepsUseCardAction(tale);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheOnlyElvenAllyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var elf = scn.GetFreepsCard("elf");
		var silinde = scn.GetFreepsCard("silinde");
		var thrarin = scn.GetFreepsCard("thrarin");
		var count = scn.GetFreepsCard("count");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(silinde, thrarin);
		scn.MoveCardsToSupportArea(tale);
		// Vitality 2 with 1 wound: Silinde cannot exert.  The Lorien Elf and Thrarin can, and are of no use.
		scn.AddWoundsToChar(silinde, 1);

		scn.StartGame();
		scn.MoveCardsToBottomOfDeck(count);
		assertSame(count, scn.GetFreepsBottomOfDeck());
		assertEquals(1, scn.GetWoundsOn(silinde));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(elf));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tale));
	}

	@Test
	public void AMinionSkirmishingAnElfAllyIsMadeWeakerForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var silinde = scn.GetFreepsCard("silinde");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var count = scn.GetFreepsCard("count");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde, orophin);
		scn.MoveCardsToSupportArea(tale);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToBottomOfDeck(count);
		assertSame(count, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, count);
		scn.MoveMinionsToTable(savage, troop);
		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{silinde, savage},
				new PhysicalCardImpl[]{aragorn, troop}
		);
		scn.FreepsResolveSkirmish(silinde);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(tale);
		scn.FreepsChooseCard(orophin);
		// The [elven] condition at the bottom is discarded; the Savage is the one minion skirmishing an Elf, and
		// is chosen unasked.
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertInDiscard(count);
		// Read at the next decision of the same skirmish.
		assertEquals(4, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Silinde (5) beats the Savage (4) without overwhelming it; it takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(aragorn);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
