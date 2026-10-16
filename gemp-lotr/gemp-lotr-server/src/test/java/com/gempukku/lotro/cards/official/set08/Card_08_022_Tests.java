package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_022_Tests
{

	// The Fellowship path: the Shadow phase of the first turn is at Ettenmoors, site 2, where every Gollum (cost 2,
	// site number 3) is roaming and costs 2 + 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hidden", "8_22");
					put("hidden2", "8_22");     // a second copy: the condition is not unique

					// Gollums, each a unique minion of cost 2 and site number 3
					put("villain", "6_40");     // Gollum, Old Villain: [gollum]; a skirmish ability only
					put("wraith", "103_66");    // Gollum, Half a Wraith Himself: [wraith]; answers a Nazgul played, and an assignment ability
					put("nasty", "5_24");       // Gollum, Nasty Treacherous Creature: [gollum]; a regroup ability only
					put("vile", "7_59");        // Gollum, Vile Creature: [gollum]; a skirmish ability only
					// Shelob, Eater of Light: a [gollum] minion with another title, cost 6 and site number 8 (8 at site 2)
					put("shelob", "8_25");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, no fierce; its text is about being played

					// Five companions besides Frodo, of four races
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an archery ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man, Valiant is its whole text
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; a fellowship ability used only on purpose
					// Smeagol, Slippery Sneak: a [gollum] companion, the character of Gollum's culture with another title
					put("smeagol", "8_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HiddenEvenFromHerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Hidden Even From Her
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Gollum is <b>fierce</b>.<br><b>Shadow:</b> Spot 6 companions to play Gollum from your discard pile and exert a companion. Place this condition on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hidden");

		assertEquals("Hidden Even From Her", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnceTheConditionIsPlayedGollumIsFierceAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var villain = scn.GetShadowCard("villain");
		var runner = scn.GetShadowCard("runner");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(hidden);

		scn.StartGame();
		scn.MoveMinionsToTable(villain, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(villain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(smeagol, Keyword.FIERCE));

		scn.ShadowPlayCard(hidden);
		assertInZone(Zone.SUPPORT, hidden);

		assertTrue(scn.HasKeyword(villain, Keyword.FIERCE));
		// A minion of another title and culture, and the [gollum] character of another title
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(smeagol, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheWraithGollumIsFierceToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var wraith = scn.GetShadowCard("wraith");

		scn.StartGame();
		scn.MoveMinionsToTable(wraith);
		assertFalse(scn.HasKeyword(wraith, Keyword.FIERCE));

		scn.MoveCardsToSupportArea(hidden);
		// Half a Wraith Himself is a [wraith] Gollum: the text names the title alone.
		assertTrue(scn.HasKeyword(wraith, Keyword.FIERCE));
	}

	@Test
	public void WithSixCompanionsItPlaysAGollumFromTheDiscardPileExertsACompanionAndGoesOnTopOfTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var hidden2 = scn.GetShadowCard("hidden2");
		var villain = scn.GetShadowCard("villain");
		var wraith = scn.GetShadowCard("wraith");
		var vile = scn.GetShadowCard("vile");
		var shelob = scn.GetShadowCard("shelob");
		var freepsNasty = scn.GetFreepsCard("nasty");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		// Frodo and five more: six companions, and an ally.
		scn.MoveCompanionsToTable(aragorn, legolas, boromir, sam, guard);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(hidden, hidden2);
		// Two Gollums of two cultures and Shelob in the discard pile; a Gollum in hand; the opponent's Gollum in
		// his own discard pile.
		scn.MoveCardsToDiscard(villain, wraith, shelob);
		scn.MoveCardsToHand(vile);
		scn.MoveCardsToDiscard(freepsNasty);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 6 companions + 2 for Ettenmoors: Shelob (6 + 2 roaming) could be paid for.
		assertEquals(18, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(hidden));

		scn.ShadowUseCardAction(hidden);

		// Gollums of two cultures; not a [gollum] minion of another title, the Gollum in hand, or the opponent's.
		assertTrue(scn.ShadowHasCardChoiceAvailable(villain, wraith));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob, vile, freepsNasty));
		scn.ShadowChooseCard(wraith);
		assertInZone(Zone.SHADOW_CHARACTERS, wraith);
		assertInZone(Zone.DISCARD, villain, shelob);
		assertInZone(Zone.HAND, vile);

		// A companion, of any race, the Ring-bearer among them; not the ally, nor the Gollum just played.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, legolas, boromir, sam, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grimir, wraith));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The copy used is the top card of its owner's draw deck; the other copy is still in play.
		assertInZone(Zone.DECK, hidden);
		assertEquals(hidden, scn.GetShadowTopOfDeck());
		assertInZone(Zone.SUPPORT, hidden2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var villain = scn.GetShadowCard("villain");
		// Frodo and four more, and an ally beside them.
		scn.MoveCompanionsToTable("aragorn", "legolas", "boromir", "sam");
		scn.MoveAlliesToTable("grimir");
		scn.MoveCardsToSupportArea(hidden);
		scn.MoveCardsToDiscard(villain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 + 5 + 2: Gollum could be paid for.
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.DISCARD, villain);
		assertFalse(scn.ShadowActionAvailable(hidden));
	}

	@Test
	public void WithThePoolOneShortOfGollumItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var villain = scn.GetShadowCard("villain");
		scn.MoveCompanionsToTable("aragorn", "legolas", "boromir", "sam", "guard");
		scn.MoveCardsToSupportArea(hidden);
		scn.MoveCardsToDiscard(villain);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Gollum costs 2 + 2 roaming at site 2.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(hidden));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(hidden));
	}

	@Test
	public void WithNoGollumInTheDiscardPileItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var villain = scn.GetShadowCard("villain");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable("aragorn", "legolas", "boromir", "sam", "guard");
		scn.MoveCardsToSupportArea(hidden);
		// Shelob, who could be paid for, is in the discard pile; one Gollum is in hand, the others in the draw deck.
		scn.MoveCardsToDiscard(shelob);
		scn.MoveCardsToHand(villain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(18, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(hidden));
	}
}
