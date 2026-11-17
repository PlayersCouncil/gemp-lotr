package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "9_6");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "9_6");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, printed damage +1
					// Farin, Dwarven Emissary: Dwarf companion with no damage bonus; his strength text is for Orcs
					put("farin", "1_11");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion; to play, spot a Dwarf
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; his fellowship ability is not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf
					// The cards of the draw deck the ability reveals; none is played.
					put("elf", "1_53");         // Lórien Elf: an [elven] companion
					put("axestrike", "1_3");    // Axe Strike: a [dwarven] Skirmish event
					put("armor", "1_92");       // Armor: a [gondor] possession
					put("runner", "1_178");     // Goblin Runner: a [moria] minion
					put("armory", "1_173");     // Goblin Armory: a [moria] condition
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofAccretionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ring of Accretion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Strength: 1
		 * Game Text: Bearer must be a Dwarf.<br>Bearer is <b>damage +1</b>.<br><b>Maneuver:</b> For each Dwarf you spot, reveal a card from the top of your draw deck. Take all Free Peoples cards revealed into hand and discard the rest. Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Accretion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnADwarfCompanionOrAllyAndMakesThatBearerAloneDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(ring);

		scn.StartGame();
		assertInHand(ring);
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));

		scn.FreepsPlayCard(ring);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);

		assertAttachedTo(ring, farin);
		// The bearer alone: Gimli keeps his printed +1, Grimir has none.
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionRevealsACardForEachOfFourDwarvesTakesTheFreePeoplesCardsAndDiscardsTheRestAndTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf = scn.GetFreepsCard("elf");
		var axestrike = scn.GetFreepsCard("axestrike");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		var armory = scn.GetFreepsCard("armory");
		scn.MoveCompanionsToTable(farin, gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(farin, ring);
		assertAttachedTo(ring, farin);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// From the top: the Elf, Axe Strike, Armor, the Runner, then Goblin Armory.
		scn.MoveCardsToTopOfDeck(armory, runner, armor, axestrike, elf);
		assertInZone(Zone.DECK, elf, axestrike, armor, runner, armory);
		scn.MoveMinionsToTable(troop);
		assertSame(elf, scn.GetFreepsTopOfDeck());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.FreepsUseCardAction(ring);
		// Four Dwarves, a companion bearing it, two other companions and an ally; Aragorn is no Dwarf.  The
		// player names how many to spot, from none, the box filled in with all of them.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(4), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("4");

		// The top 4 are shown to each player, and the fifth card is not.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(axestrike, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(elf, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(armory, false));
		scn.DismissRevealedCards();

		// A companion, an event and a possession of three Free Peoples cultures are taken with nothing asked; the
		// Shadow minion is discarded.
		assertInHand(elf, axestrike, armor);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInDiscard(runner);
		assertSame(armory, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		// A look at the top of the deck is no search of it: no shuffle follows (Q-0013).
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(ring);
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NamingOneOfTwoDwarvesRevealsOnlyTheTopCardAndAShadowCardRevealedIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var axestrike = scn.GetFreepsCard("axestrike");
		var armory = scn.GetFreepsCard("armory");
		scn.MoveCompanionsToTable(farin);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(farin, ring);
		assertAttachedTo(ring, farin);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// From the top: Goblin Armory, then Axe Strike.
		scn.MoveCardsToTopOfDeck(axestrike, armory);
		assertInZone(Zone.DECK, armory, axestrike);
		scn.MoveMinionsToTable(troop);
		assertSame(armory, scn.GetFreepsTopOfDeck());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsUseCardAction(ring);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// One card revealed for the one Dwarf spotted: a Shadow condition, which is discarded.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(axestrike, false));
		scn.DismissRevealedCards();

		assertInDiscard(armory);
		assertEquals(0, scn.GetFreepsHandCount());
		assertSame(axestrike, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(ring);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NamingNoDwarfRevealsNothingAndTheRingIsStillDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var farin = scn.GetFreepsCard("farin");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(farin);
		scn.AttachCardsTo(farin, ring);
		assertAttachedTo(ring, farin);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(axestrike);
		scn.MoveMinionsToTable(troop);
		assertSame(axestrike, scn.GetFreepsTopOfDeck());
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(ring);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		// Nothing is revealed or taken, and the last sentence is carried out all the same.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertSame(axestrike, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(ring);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheActionIsOfferedInTheManeuverPhaseAndNotInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);
		scn.AttachCardsTo(farin, ring);
		assertAttachedTo(ring, farin);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		assertAttachedTo(ring, farin);
	}
}
