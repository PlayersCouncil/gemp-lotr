package com.gempukku.lotro.mechanics.costs;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * An event played "ignoring all costs" is not asked for the cost in its own text.
 *
 * Comprehensive Rules 4.2, "cost": "The costs for an action are usually listed before the word 'to' (so the
 * action takes the form of 'pay X to do Y,' with X being the cost and Y the effect)", and "playing a card": "Pay
 * costs to play The Card. This includes both twilight costs and other costs included in game text."  So the
 * exertion in "Exert your [moria] Orc to draw 2 cards" is one of the costs of playing that event, and a card that
 * plays it "ignoring all costs" ignores it.  Ketura, 2026-10-09, on F-0316: "well-reasoned, I approve fixing
 * this."
 *
 * The one card that plays anything so is Race Text 92_29 (Race to Mount Doom): "Shadow: Open and reveal a sealed
 * booster pack (limit once per turn). You may play one shadow card revealed, ignoring all costs."  It ignored an
 * event's twilight cost and went on to ask for the costs in the event's text: Threat of the Unknown exerted an
 * Orc before it drew, and with no Orc on the table it was chosen, discarded, and drew nothing (F-0316).
 *
 * Two kinds of cost are still carried out, because the rest of the event could not be without them.  One is a
 * cost the text reads back: "spot X burdens to draw X cards" still asks for X, since with nothing spotted there
 * is no X to draw.  The other is a "cost" that holds an effect of the card: an event with a text for each of two
 * phases is defined so (We Must Have It 5_30), and its text for the Shadow phase has no cost in it to ignore.
 */
public class IgnoringAllCostsTests
{
	// Race to Mount Doom, with the Shadow player holding Race Text 92_29.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("threat", "1_197");     // Threat of the Unknown: [moria] Shadow event, cost 0. "Exert your [moria] Orc to draw 2 cards."
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability that is never used here
					put("gollum", "5_25");      // Gollum, Stinker: cost 2, site 3; his texts need a skirmish
					put("runner", "1_178");     // Goblin Runner: so that there are cards to draw; never played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_29"
		);
	}

	@Test
	public void AnEventPlayedIgnoringAllCostsDoesNotPayTheCostInItsOwnText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var troop = scn.GetShadowCard("troop");

		// A pack that holds Threat of the Unknown and nothing else.
		scn.UseBoosterOf("1_197");
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		// The [moria] Orc is there and could exert.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetShadowHandCount());
		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection("1_197");
		var threat = scn.GetLastCreatedCard();
		scn.StopCardCapture();

		// The 2 cards are drawn, and no Orc was exerted for them.
		assertEquals("Threat of the Unknown", threat.getBlueprint().getTitle());
		assertInZone(Zone.DISCARD, threat);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventWhoseOwnCostCannotBePaidIsPlayedIgnoringAllCostsAndDoesWhatItSays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseBoosterOf("1_197");
		scn.StartGame();
		scn.FreepsPass();

		// No minion on the table at all: nobody to exert.
		assertEquals(0, scn.GetShadowHandCount());

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_197"));
		scn.ShadowChooseCardBPFromSelection("1_197");
		var threat = scn.GetLastCreatedCard();
		scn.StopCardCapture();

		assertInZone(Zone.DISCARD, threat);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSameEventPlayedFromHandIsWithheldWithoutItsCostAndPaysIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threat = scn.GetShadowCard("threat");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToHand(threat);
		scn.FreepsPass();

		// With no [moria] Orc to exert the event cannot be played from hand.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(threat));

		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(threat));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowPlayCard(threat);

		// The cost is real when the event is played in the ordinary way: the Orc exerts for the 2 cards.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, threat);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACostTheTextReadsBackIsStillAskedForAndTheCostBesideItIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var soldier = scn.GetShadowCard("soldier");

		// The Eye of Sauron: [sauron] Shadow event, cost 1. "Exert a [sauron] Orc and spot X burdens to draw X
		// cards (limit 5)."
		scn.UseBoosterOf("2_87");
		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AddBurdens(2);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetShadowHandCount());
		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection("2_87");

		// X is still the player's to name, from the burdens there are to spot.
		assertTrue(scn.ShadowDecisionAvailable("Choose how many burdens to spot"));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// X cards are drawn; the [sauron] Orc did not exert, and the event's cost of 1 was not taken either.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventDefinedWithItsEffectInsideACostStillDoesWhatItSays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var gollum = scn.GetShadowCard("gollum");

		// We Must Have It: [gollum] event, cost 0. "Shadow: Play Gollum from your draw deck or discard pile.
		// Skirmish: Discard a card at random from hand to make Gollum strength +3."
		scn.UseBoosterOf("5_30");
		scn.StartGame();
		scn.MoveCardsToDiscard(gollum);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for the site
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DISCARD, gollum);

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection("5_30");

		// Its Shadow text has no cost to ignore, and is carried out: Gollum is played from the discard pile, the
		// only pile that holds him.  He is played by the event and not by the pack, so he is paid for: 2, and 2
		// more for roaming at site 2.
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
