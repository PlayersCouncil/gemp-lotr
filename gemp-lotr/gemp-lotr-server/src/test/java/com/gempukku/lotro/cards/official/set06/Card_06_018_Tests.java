package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "6_18");
					// Celeborn, Lord of Lorien: [elven] Elf ally, home site 6, vitality 3; a fellowship ability used only on purpose
					put("celeborn", "1_34");
					// Lorien Elf: [elven] Elf companion, strength 4, vitality 2; "To play, spot an Elf", so it is cheated in
					put("elf", "1_53");
					// Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3, strength 5, vitality 2; has the text of
					// site 3 (Rivendell Valley: a fellowship action) while that site is on the path
					put("silinde", "1_60");
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

					// Cards laid on top of the draw deck; only their side is read.  The Free Peoples player's own copy of
					// the Troop is a Shadow minion there.
					put("beckoning", "12_83");  // The Beckoning Shadow: a Shadow condition
					put("savagery", "1_139");   // Savagery to Match Their Numbers: a Shadow event
					put("elfcard", "1_53");     // Lorien Elf: a Free Peoples companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Galadriel, Keeper of Nenya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: At the start of each turn, heal Galadriel.
		 * <b>Skirmish:</b> Exert Galadriel to discard the top card of your draw deck. If it is a Shadow card, make a
		 * minion skirmishing an Elf strength -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", card.getBlueprint().getTitle());
		assertEquals("Keeper of Nenya", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void HealsGaladrielAtTheStartOfEachOfHerOwnersTurnsAndNoOtherAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(galadriel, celeborn);

		scn.StartGame();
		// Wounded during the owner's first turn, after its start.
		scn.AddWoundsToChar(galadriel, 2);
		scn.AddWoundsToChar(celeborn, 1);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		// Player 2's turn: Galadriel is not active on it.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		// The owner's next turn: Galadriel is healed once, and the other wounded Elf ally is not.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetWoundsOn(galadriel));

		// His next turn begins at site 3, a sanctuary, whose start-of-turn heal is ordered with Galadriel's.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsResolveActionOrder("Required trigger from");
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsGaladrielToDiscardTheTopCardAndAShadowCardWeakensAMinionSkirmishingAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var elf = scn.GetFreepsCard("elf");
		var boromir = scn.GetFreepsCard("boromir");
		var troopcard = scn.GetFreepsCard("troop");
		var beckoning = scn.GetFreepsCard("beckoning");
		var shadowElfcard = scn.GetShadowCard("elfcard");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(elf, boromir);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		// From the top: the Free Peoples player's own Goblin Patrol Troop (a Shadow minion), then The Beckoning
		// Shadow (a Shadow condition).  The Shadow player's deck has a card on top too.
		scn.MoveCardsToTopOfDeck(beckoning, troopcard, shadowElfcard);
		assertSame(troopcard, scn.GetFreepsTopOfDeck());
		assertSame(shadowElfcard, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, troopcard, beckoning);

		scn.MoveMinionsToTable(savage, footman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, savage},
				new PhysicalCardImpl[]{boromir, troop}
		);
		scn.ShadowAssignToMinions(elf, footman);
		scn.FreepsResolveSkirmish(boromir);

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(galadriel));

		scn.FreepsUseCardAction(galadriel);

		// The Shadow minion on top is discarded, and nothing else of either deck is touched.
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertInDiscard(troopcard);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(beckoning, scn.GetFreepsTopOfDeck());
		assertSame(shadowElfcard, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		// In Boromir's skirmish the Troop fights a companion who is no Elf, and the two minions assigned to the
		// Elf are not skirmishing yet: no minion is made weaker, and nothing is asked.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(elf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(galadriel);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertInDiscard(beckoning);

		// Both minions in the Elf's skirmish; not the Troop, whose skirmish is over.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		assertEquals(4, scn.GetStrength(footman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AFreePeoplesCardDiscardedWeakensNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var elf = scn.GetFreepsCard("elf");
		var elfcard = scn.GetFreepsCard("elfcard");
		var beckoning = scn.GetFreepsCard("beckoning");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		// The Lorien Elf card on top, a Shadow card beneath it.
		scn.MoveCardsToTopOfDeck(beckoning, elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, elfcard, beckoning);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		scn.FreepsUseCardAction(galadriel);

		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertInDiscard(elfcard);
		assertSame(beckoning, scn.GetFreepsTopOfDeck());
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheAbilityExertsGaladrielForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var elf = scn.GetFreepsCard("elf");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(0, scn.GetFreepsDeckCount());
		int discard = scn.GetFreepsDiscardCount();
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(galadriel));

		scn.FreepsUseCardAction(galadriel);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileGaladrielIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var elf = scn.GetFreepsCard("elf");
		var beckoning = scn.GetFreepsCard("beckoning");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Galadriel cannot exert.
		scn.AddWoundsToChar(galadriel, 2);
		scn.MoveCardsToTopOfDeck(beckoning);
		assertSame(beckoning, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, beckoning);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(galadriel));
	}

	@Test
	public void AMinionSkirmishingAnElfAllyIsMadeWeakerForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savagery = scn.GetFreepsCard("savagery");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(galadriel, silinde);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(savagery);
		assertSame(savagery, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, savagery);
		scn.MoveMinionsToTable(footman, troop);
		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{silinde, footman},
				new PhysicalCardImpl[]{aragorn, troop}
		);
		scn.FreepsResolveSkirmish(silinde);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(galadriel);
		// The Shadow event on top is discarded; the Footman is the one minion skirmishing an Elf, and is chosen
		// unasked.
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertInDiscard(savagery);
		// Read at the next decision of the same skirmish: 7 - 3 = 4.
		assertEquals(4, scn.GetStrength(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Silinde (5) beats the Footman (4) without overwhelming it; it takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(footman));
		scn.FreepsResolveSkirmish(aragorn);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
