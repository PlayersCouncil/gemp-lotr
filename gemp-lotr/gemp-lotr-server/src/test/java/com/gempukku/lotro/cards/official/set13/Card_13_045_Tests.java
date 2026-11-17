package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hidden", "13_45");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, no race, strength 5; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5; his texts are used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; "When you play" only, and he is cheated in
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, a maneuver ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability and a response when Frodo dies

					// [gollum] cards for the discard pile, of each kind, where none of them does anything
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: Shadow minion (also the [gollum] minion who is no Gollum)
					put("poison", "8_28");      // Spider Poison: Shadow possession
					put("fens", "5_22");        // Evil-smelling Fens: Shadow condition
					put("dontlook", "6_39");    // Don't Look at Them: Free Peoples condition
					put("smeagol", "6_45");     // Sméagol, Poor Creature: Free Peoples companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CunninglyHiddenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Cunningly Hidden
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 [gollum] cards in your discard pile instead of paying the twilight cost for this card.<br>Spot Gollum to make a minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hidden");

		assertEquals("Cunningly Hidden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotGollumToMakeAnyMinionStrengthPlus2UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var hidden = scn.GetShadowCard("hidden");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hidden);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop, runner);
		scn.SkipToAssignments();
		// Gollum in the first skirmish, the Troop in a later one, the Runner assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{sam, troop});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// No [gollum] card in the discard pile: the twilight cost is paid, with nothing asked.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(hidden));
		scn.ShadowPlayCard(hidden);
		assertEquals(0, scn.GetTwilight());

		// Any minion: in this skirmish, in a later one, or in none; of either culture and race.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, troop, runner));
		// Not a Free Peoples character, in this skirmish or in the next.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, sam));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(13, scn.GetStrength(troop));
		scn.ShadowChooseCard(troop);

		assertEquals(15, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(5, scn.GetStrength(runner));
		assertInDiscard(hidden);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn beats Gollum; in the Troop's own skirmish, a phase of its own, the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTheTwilightAndFourGollumCardsBothWaysToPayAreAskedAndTheCardsAreRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsHidden = scn.GetFreepsCard("hidden");
		scn.MoveCompanionsToTable(aragorn);

		var hidden = scn.GetShadowCard("hidden");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var poison = scn.GetShadowCard("poison");
		var fens = scn.GetShadowCard("fens");
		var dontlook = scn.GetShadowCard("dontlook");
		var smeagol = scn.GetShadowCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hidden);
		scn.MoveCardsToDiscard(shelob, poison, fens, dontlook, smeagol, runner);
		// The other player's [gollum] card, in his own discard pile: a title the Shadow player's pile does not hold,
		// since a choice from a pile is read by blueprint.
		scn.MoveCardsToDiscard(freepsHidden);

		scn.StartGame();
		assertInDiscard(shelob, poison, fens, dontlook, smeagol, runner, freepsHidden);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertEquals(5, scn.GetStrength(gollum));
		scn.ShadowPlayCard(hidden);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseYes();

		// Exactly four [gollum] cards, of either side and any type; not the [moria] Orc, nor the other player's card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, poison, fens, dontlook, smeagol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, freepsHidden));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(shelob, poison, dontlook, smeagol);

		assertInZone(Zone.REMOVED, shelob, poison, dontlook, smeagol);
		assertInDiscard(fens, runner, freepsHidden);
		assertEquals(2, scn.GetTwilight());

		// The lone minion is chosen unasked.
		assertEquals(7, scn.GetStrength(gollum));
		assertInDiscard(hidden);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheRemovalPaysTheTwilightCostAndAWraithGollumIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hidden = scn.GetShadowCard("hidden");
		var gollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var poison = scn.GetShadowCard("poison");
		var fens = scn.GetShadowCard("fens");
		var dontlook = scn.GetShadowCard("dontlook");
		scn.MoveCardsToHand(hidden);
		scn.MoveCardsToDiscard(shelob, poison, fens, dontlook);

		scn.StartGame();
		assertInDiscard(shelob, poison, fens, dontlook);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(hidden));
		assertEquals(5, scn.GetStrength(gollum));
		scn.ShadowPlayCard(hidden);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseNo();

		// The twilight cost is paid in place of the removal; the lone minion is chosen unasked.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(shelob, poison, fens, dontlook, hidden);
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolItIsPlayedOnlyWithFourGollumCardsToRemoveAndTheyAreRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hidden = scn.GetShadowCard("hidden");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var poison = scn.GetShadowCard("poison");
		var fens = scn.GetShadowCard("fens");
		var dontlook = scn.GetShadowCard("dontlook");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(hidden);
		// Four cards, three of them [gollum].
		scn.MoveCardsToDiscard(shelob, poison, fens, runner);

		scn.StartGame();
		assertInDiscard(shelob, poison, fens, runner);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hidden));

		// A fourth [gollum] card: the removal is the only way to pay, and it is taken with no question.
		scn.MoveCardsToDiscard(dontlook);
		assertInDiscard(dontlook);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(hidden));
		assertEquals(5, scn.GetStrength(gollum));
		scn.ShadowPlayCard(hidden);

		assertFalse(scn.ShadowDecisionAvailable("instead of paying twilight cost"));
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to remove"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, poison, fens, dontlook));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCards(shelob, poison, fens, dontlook);
		assertInZone(Zone.REMOVED, shelob, poison, fens, dontlook);
		assertInDiscard(runner, hidden);
		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWithAGollumMinionWhoIsNoGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hidden = scn.GetShadowCard("hidden");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(hidden);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// The twilight cost can be paid, and Shelob is a [gollum] minion; but no Gollum can be spotted.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hidden));
	}
}
